package cn.sweater.domain.trade.service.refund.factory;

import cn.bugstack.wrench.design.framework.link.model2.LinkArmory;
import cn.bugstack.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import cn.sweater.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import cn.sweater.domain.trade.service.lock.filter.TeamStockOccupyRuleFilter;
import cn.sweater.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;
import cn.sweater.domain.trade.service.refund.filter.DataNodeFilter;
import cn.sweater.domain.trade.service.refund.filter.RefundOrderNodeFilter;
import cn.sweater.domain.trade.service.refund.filter.UniqueRefundNodeFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RefundRuleFilterFactory {
    @Bean("tradeRefundRuleFilter")
    BusinessLinkedList<TradeRefundCommandEntity,RefundRuleFilterFactory.DynamicContext,TradeRefundBehaviorEntity> tradeRuleFilter
            (DataNodeFilter dataNodeFilter, UniqueRefundNodeFilter uniqueRefundNodeFilter, RefundOrderNodeFilter refundOrderNodeFilter) {
        LinkArmory<TradeRefundCommandEntity, RefundRuleFilterFactory.DynamicContext, TradeRefundBehaviorEntity> linkArmory
                =new LinkArmory<>("交易规则过滤链",dataNodeFilter,uniqueRefundNodeFilter,refundOrderNodeFilter);
        return linkArmory.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        private MarketPayOrderEntity marketPayOrderEntity;
        private GroupBuyTeamEntity groupBuyTeamEntity;
    }
}
