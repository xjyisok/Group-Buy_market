package cn.sweater.domain.trade.service.settlement.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.sweater.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import cn.sweater.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import cn.sweater.domain.trade.model.valobj.NotifyConfigVO;
import cn.sweater.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EndRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("结算规则过滤-组装{} outTradeNO:{}",requestParameter.getSource(),requestParameter.getOutTradeNo());
        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = dynamicContext.getGroupBuyTeamEntity();
        return  TradeSettlementRuleFilterBackEntity.builder()
                .teamId(dynamicContext.getGroupBuyTeamEntity().getTeamId())
                .activityId(groupBuyTeamEntity.getActivityId())
                .targetCount(groupBuyTeamEntity.getTargetCount())
                .completeCount(groupBuyTeamEntity.getCompleteCount())
                .lockCount(groupBuyTeamEntity.getLockCount())
                .status(groupBuyTeamEntity.getStatus())
                .validEndTime(groupBuyTeamEntity.getValidEndTime())
                .validStartTime(groupBuyTeamEntity.getValidStartTime())
                .notifyConfigVO(NotifyConfigVO.builder()
                        .notifyMQ(groupBuyTeamEntity.getNotifyConfig().getNotifyMQ())
                        .notifyUrl(groupBuyTeamEntity.getNotifyConfig().getNotifyUrl())
                        .notifyType(groupBuyTeamEntity.getNotifyConfig().getNotifyType())
                        .build())
                .build();
    }
}
