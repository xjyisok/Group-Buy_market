package cn.sweater.domain.trade.service.refund;

import cn.bugstack.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderListDetailEntity;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.model.valobj.RefundTypeEnumVO;
import cn.sweater.domain.trade.model.valobj.TeamRefundSuccess;
import cn.sweater.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.sweater.domain.trade.service.ITradeRefundOrderService;
import cn.sweater.domain.trade.service.refund.business.IRefundOrderStrategy;
import cn.sweater.domain.trade.service.refund.factory.RefundRuleFilterFactory;
import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TradeRefundOrderService implements ITradeRefundOrderService {
    private final ITradeRepository tradeRepository;
    private final Map<String, IRefundOrderStrategy> strategyMap;
    @Resource
    private BusinessLinkedList<TradeRefundCommandEntity, RefundRuleFilterFactory.DynamicContext,TradeRefundBehaviorEntity> tradeRefundRuleFilter;
    public TradeRefundOrderService(ITradeRepository tradeRepository, Map<String, IRefundOrderStrategy> strategyMap) {
        this.tradeRepository = tradeRepository;
        this.strategyMap = strategyMap;
    }
    @Override
    public TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity tradeRefundCommandEntity) throws Exception {
        log.info("逆向流程，退单操作 userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());
        return tradeRefundRuleFilter.apply(tradeRefundCommandEntity,new RefundRuleFilterFactory.DynamicContext());
//        String userId = tradeRefundCommandEntity.getUserId();
//        String outTradeNo = tradeRefundCommandEntity.getOutTradeNo();
//        MarketPayOrderEntity marketPayOrderEntity = tradeRepository.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
//        String teamId = marketPayOrderEntity.getTeamId();
//        String orderId = marketPayOrderEntity.getOrderId();
//        TradeOrderStatusEnumVO tradeOrderStatusEnumVO=marketPayOrderEntity.getTradeOrderStatusEnumVO();
//        //幂等，已经完成的退单，或者已经超时被关闭的订单
//        if (TradeOrderStatusEnumVO.CLOSE.equals(tradeOrderStatusEnumVO)||TradeOrderStatusEnumVO.REFUND.equals(tradeOrderStatusEnumVO)) {
//            log.info("逆向流程，退单操作(幂等-重复退单) userId:{} outTradeNo:{}", tradeRefundCommandEntity.getUserId(), tradeRefundCommandEntity.getOutTradeNo());
//            return TradeRefundBehaviorEntity.builder()
//                    .userId(tradeRefundCommandEntity.getUserId())
//                    .orderId(orderId)
//                    .teamId(teamId)
//                    .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.REPEAT)
//                    .build();
//        }
//        // 2. 查询拼团状态
//        GroupBuyTeamEntity groupBuyTeamEntity = tradeRepository.queryGroupBuyTeamByTeamId(teamId);
//        GroupBuyOrderEnumVO groupBuyOrderEnumVO = groupBuyTeamEntity.getStatus();
//        RefundTypeEnumVO refundTypeEnumVO=RefundTypeEnumVO.getStrategy(groupBuyOrderEnumVO, tradeOrderStatusEnumVO);
//        IRefundOrderStrategy refundOrderStrategy = strategyMap.get(refundTypeEnumVO.getStrategy());
//        //3. 策略模式匹配对应策略
//        refundOrderStrategy.refundOrder(TradeRefundOrderEntity.builder()
//                        .userId(tradeRefundCommandEntity.getUserId())
//                        .activityId(groupBuyTeamEntity.getActivityId())
//                        .orderId(orderId)
//                .teamId(teamId)
//                .build());
//        return  TradeRefundBehaviorEntity.builder()
//                .userId(tradeRefundCommandEntity.getUserId())
//                .orderId(orderId)
//                .teamId(teamId)
//                .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.SUCCESS)
//                .build();
    }

    @Override
    public void restoreTeamStockLock(TeamRefundSuccess teamRefundSuccess) {
        log.info("逆向流程,恢复锁单库存 userId:{} activityId:{} teamId:{}",teamRefundSuccess.getUserId(),teamRefundSuccess.getActivityId(),teamRefundSuccess.getTeamId());
        String type = teamRefundSuccess.getType();
        RefundTypeEnumVO refundTypeEnumVO=RefundTypeEnumVO.getRefundTypeEnumVOByCode(type);
        IRefundOrderStrategy refundOrderStrategy = strategyMap.get(refundTypeEnumVO.getStrategy());
        refundOrderStrategy.restoreTeamStockLock(teamRefundSuccess);
    }

    @Override
    public List<UserGroupBuyOrderListDetailEntity> queryTimeOutUnpaidOrder() {
        return tradeRepository.queryTimeOutUnpaidOrder();
    }

    @Override
    public void compensateRefundOrder(TradeRefundCommandEntity command, Integer orderStatus) throws Exception {
        log.info("补偿退单 userId:{} outTradeNo:{} orderStatus:{}", command.getUserId(), command.getOutTradeNo(), orderStatus);
        MarketPayOrderEntity marketPayOrderEntity = tradeRepository.queryNoPayMarketPayOrderByOutTradeNo(
                command.getUserId(), command.getOutTradeNo());
        if (marketPayOrderEntity == null) {
            log.warn("补偿退单：订单不存在 userId:{} outTradeNo:{}", command.getUserId(), command.getOutTradeNo());
            return;
        }
        // 幂等：已退款的直接跳过
        if (TradeOrderStatusEnumVO.CLOSE.equals(marketPayOrderEntity.getTradeOrderStatusEnumVO())
                || TradeOrderStatusEnumVO.REFUND.equals(marketPayOrderEntity.getTradeOrderStatusEnumVO())) {
            log.info("补偿退单：订单已退款，跳过 userId:{} outTradeNo:{}", command.getUserId(), command.getOutTradeNo());
            return;
        }
        TradeRefundOrderEntity tradeRefundOrderEntity = TradeRefundOrderEntity.builder()
                .userId(command.getUserId())
                .outTradeNo(command.getOutTradeNo())
                .orderId(marketPayOrderEntity.getOrderId())
                .teamId(marketPayOrderEntity.getTeamId())
                .build();
        // 查询 activityId
        GroupBuyTeamEntity team = tradeRepository.queryGroupBuyTeamByTeamId(marketPayOrderEntity.getTeamId());
        tradeRefundOrderEntity.setActivityId(team.getActivityId());

        // 按支付单状态路由：status=0 未支付 → unpaid2Refund；status=1 已支付未成团 → paid2Refund
        IRefundOrderStrategy strategy;
        if (orderStatus == 0) {
            strategy = strategyMap.get(RefundTypeEnumVO.UNPAID_UNLOCK.getStrategy());
        } else {
            strategy = strategyMap.get(RefundTypeEnumVO.PAID_UNFORMED.getStrategy());
        }
        strategy.refundOrder(tradeRefundOrderEntity);
        log.info("补偿退单完成 userId:{} outTradeNo:{}", command.getUserId(), command.getOutTradeNo());
    }
}
