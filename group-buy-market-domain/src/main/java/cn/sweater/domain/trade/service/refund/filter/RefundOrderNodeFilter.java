package cn.sweater.domain.trade.service.refund.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.model.valobj.RefundTypeEnumVO;
import cn.sweater.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.sweater.domain.trade.service.refund.business.IRefundOrderStrategy;
import cn.sweater.domain.trade.service.refund.factory.RefundRuleFilterFactory;
import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
@Slf4j
public class RefundOrderNodeFilter implements ILogicHandler<TradeRefundCommandEntity, RefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> {
    @Resource
    private ITradeRepository tradeRepository;

    private final Map<String, IRefundOrderStrategy> strategyMap;

    public RefundOrderNodeFilter(Map<String, IRefundOrderStrategy> strategyMap) {
        this.strategyMap = strategyMap;
    }

    @Override
    public TradeRefundBehaviorEntity apply(TradeRefundCommandEntity tradeRefundCommandEntity, RefundRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        MarketPayOrderEntity marketPayOrderEntity=dynamicContext.getMarketPayOrderEntity();
        GroupBuyTeamEntity groupBuyTeamEntity=dynamicContext.getGroupBuyTeamEntity();
        TradeOrderStatusEnumVO tradeOrderStatusEnumVO=marketPayOrderEntity.getTradeOrderStatusEnumVO();
        GroupBuyOrderEnumVO groupBuyOrderEnumVO = groupBuyTeamEntity.getStatus();
        RefundTypeEnumVO refundTypeEnumVO=RefundTypeEnumVO.getStrategy(groupBuyOrderEnumVO, tradeOrderStatusEnumVO);
        IRefundOrderStrategy refundOrderStrategy = strategyMap.get(refundTypeEnumVO.getStrategy());
        //3. 策略模式匹配对应策略
        refundOrderStrategy.refundOrder(TradeRefundOrderEntity.builder()
                .userId(tradeRefundCommandEntity.getUserId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .orderId(marketPayOrderEntity.getOrderId())
                .teamId(marketPayOrderEntity.getTeamId())
                .outTradeNo(tradeRefundCommandEntity.getOutTradeNo())
                .build());
        return  TradeRefundBehaviorEntity.builder()
                .userId(tradeRefundCommandEntity.getUserId())
                .orderId(marketPayOrderEntity.getOrderId())
                .teamId(marketPayOrderEntity.getTeamId())
                .tradeRefundBehaviorEnum(TradeRefundBehaviorEntity.TradeRefundBehaviorEnum.SUCCESS)
                .build();
    }
}
