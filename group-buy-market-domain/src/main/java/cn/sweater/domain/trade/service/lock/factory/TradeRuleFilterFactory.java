package cn.sweater.domain.trade.service.lock.factory;

import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.sweater.domain.trade.model.entity.TradeLockRuleCommandEntity;
import cn.sweater.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import cn.sweater.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import cn.sweater.domain.trade.service.lock.filter.TeamStockOccupyRuleFilter;
import cn.sweater.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;
import cn.sweater.types.design.framework.link.model2.LinkArmory;
import cn.sweater.types.design.framework.link.model2.chain.BusinessLinkedList;
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
public class TradeRuleFilterFactory {
    @Bean("tradeRuleFilter")
    BusinessLinkedList<TradeLockRuleCommandEntity,TradeRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter
            (ActivityUsabilityRuleFilter activityUsabilityRuleFilter, UserTakeLimitRuleFilter userTakeLimitRuleFilter, TeamStockOccupyRuleFilter teamStockOccupyRuleFilter) {
        LinkArmory<TradeLockRuleCommandEntity,TradeRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> linkArmory
        =new LinkArmory<>("交易规则过滤链",activityUsabilityRuleFilter,userTakeLimitRuleFilter,teamStockOccupyRuleFilter);
        return linkArmory.getLogicLink();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DynamicContext {
        private String teamStockKey = "group_buy_market_team_stock_key_";
        private GroupBuyActivityEntity groupBuyActivity;
        private Integer userTakeOrderCount;
        public String generateTeamStockKey(String teamId) {
            if (StringUtils.isBlank(teamId)) return null;
            return teamStockKey + groupBuyActivity.getActivityId() + "_" + teamId;
        }

        public String generateRecoveryTeamStockKey(String teamId) {
            if (StringUtils.isBlank(teamId)) return null;
            return teamStockKey + groupBuyActivity.getActivityId() + "_" + teamId + "_recovery";
        }


    }

}
