package cn.sweater.domain.trade.service.lock.factory;

import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.sweater.domain.trade.model.entity.TradeLockRuleCommandEntity;
import cn.sweater.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import cn.sweater.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import cn.sweater.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;
import cn.sweater.types.design.framework.link.model2.LinkArmory;
import cn.sweater.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TradeRuleFilterFactory {
    @Bean("tradeRuleFilter")
    BusinessLinkedList<TradeLockRuleCommandEntity,TradeRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter
            (ActivityUsabilityRuleFilter activityUsabilityRuleFilter, UserTakeLimitRuleFilter userTakeLimitRuleFilter) {
        LinkArmory<TradeLockRuleCommandEntity,TradeRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> linkArmory
        =new LinkArmory<>("交易规则过滤链",activityUsabilityRuleFilter,userTakeLimitRuleFilter);
        return linkArmory.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {

        private GroupBuyActivityEntity groupBuyActivity;

    }

}
