package cn.sweater.domain.trade.service.refund;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.model.valobj.RefundTypeEnumVO;
import cn.sweater.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.sweater.domain.trade.service.ITradeRefundOrderService;
import cn.sweater.domain.trade.service.refund.business.IRefundOrderStrategy;
import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class TradeRefundOrderService implements ITradeRefundOrderService {
    private final ITradeRepository tradeRepository;
    private final Map<String, IRefundOrderStrategy> strategyMap;

    public TradeRefundOrderService(ITradeRepository tradeRepository, Map<String, IRefundOrderStrategy> strategyMap) {
        this.tradeRepository = tradeRepository;
        this.strategyMap = strategyMap;
    }
    @Override
    public TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity tradeRefundCommandEntity) {
        log.info("逆向流程，退单操作 userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());
        String userId = tradeRefundCommandEntity.getUserId();
        String outTradeNo = tradeRefundCommandEntity.getOutTradeNo();
        MarketPayOrderEntity marketPayOrderEntity = tradeRepository.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
        String teamId = marketPayOrderEntity.getTeamId();
        String orderId = marketPayOrderEntity.getOrderId();
        TradeOrderStatusEnumVO tradeOrderStatusEnumVO=marketPayOrderEntity.getTradeOrderStatusEnumVO();
        //幂等，已经完成的退单，或者已经超时被关闭的订单
        if (TradeOrderStatusEnumVO.CLOSE.equals(tradeOrderStatusEnumVO)||TradeOrderStatusEnumVO.REFUND.equals(tradeOrderStatusEnumVO)) {
            log.info("逆向流程，退单操作(幂等-重复退单) userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());
            return TradeRefundBehaviorEntity.builder()
                    .userId(tradeRefundCommandEntity.getUserId())
                    .orderId(orderId)
                    .teamId(teamId)
                    .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.REPEAT)
                    .build();
        }
        // 2. 查询拼团状态
        GroupBuyTeamEntity groupBuyTeamEntity = tradeRepository.queryGroupBuyTeamByTeamId(teamId);
        GroupBuyOrderEnumVO groupBuyOrderEnumVO = groupBuyTeamEntity.getStatus();
        RefundTypeEnumVO refundTypeEnumVO=RefundTypeEnumVO.getStrategy(groupBuyOrderEnumVO, tradeOrderStatusEnumVO);
        IRefundOrderStrategy refundOrderStrategy = strategyMap.get(refundTypeEnumVO.getStrategy());
        //3. 策略模式匹配对应策略
        refundOrderStrategy.refundOrder(TradeRefundOrderEntity.builder()
                        .userId(tradeRefundCommandEntity.getUserId())
                        .orderId(orderId)
                .teamId(teamId)
                .build());
        return  TradeRefundBehaviorEntity.builder()
                .userId(tradeRefundCommandEntity.getUserId())
                .orderId(orderId)
                .teamId(teamId)
                .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.SUCCESS)
                .build();
    }
}
