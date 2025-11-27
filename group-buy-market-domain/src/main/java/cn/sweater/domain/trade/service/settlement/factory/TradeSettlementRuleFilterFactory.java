package cn.sweater.domain.trade.service.settlement.factory;

import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import cn.sweater.domain.trade.service.settlement.filter.EndRuleFilter;
import cn.sweater.domain.trade.service.settlement.filter.OutTradeNORuleFilter;
import cn.sweater.domain.trade.service.settlement.filter.SCRuleFilter;
import cn.sweater.domain.trade.service.settlement.filter.SettableRuleFilter;
import cn.sweater.types.design.framework.link.model2.LinkArmory;
import cn.sweater.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TradeSettlementRuleFilterFactory {
    @Bean(name = "tradeSettlementRuleFilter")
    BusinessLinkedList<TradeSettlementRuleCommandEntity,TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity>
    tradeSettlementRuleFilterFactory(SCRuleFilter scRuleFilter, OutTradeNORuleFilter outTradeNORuleFilter
            , SettableRuleFilter settableRuleFilter, EndRuleFilter endRuleFilter) {
        LinkArmory<TradeSettlementRuleCommandEntity,TradeSettlementRuleFilterFactory.DynamicContext,TradeSettlementRuleFilterBackEntity>
                linkArmory=new LinkArmory<>("tradeSettlementRuleFilter",scRuleFilter,outTradeNORuleFilter,settableRuleFilter,endRuleFilter);
        return linkArmory.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        // 订单营销实体对象
        private MarketPayOrderEntity marketPayOrderEntity;
        // 拼团组队实体对象
        private GroupBuyTeamEntity groupBuyTeamEntity;


    }
}
