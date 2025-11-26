package cn.sweater.domain.trade.service.factory;

import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.sweater.domain.trade.model.entity.TradeRuleCommandEntity;
import cn.sweater.domain.trade.model.entity.TradeRuleFilterBackEntity;
import cn.sweater.domain.trade.service.filter.ActivityUsabilityRuleFilter;
import cn.sweater.domain.trade.service.filter.UserTakeLimitRuleFilter;
import cn.sweater.types.design.framework.link.model2.LinkArmory;
import cn.sweater.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class TradeRuleFilterFactory {
    @Bean("tradeRuleFilter")
    BusinessLinkedList<TradeRuleCommandEntity,TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity> tradeRuleFilter
            (ActivityUsabilityRuleFilter activityUsabilityRuleFilter, UserTakeLimitRuleFilter userTakeLimitRuleFilter) {
        LinkArmory<TradeRuleCommandEntity,TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity> linkArmory
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
