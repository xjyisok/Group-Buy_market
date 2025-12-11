package cn.sweater.domain.trade.service.lock.factory;

import cn.bugstack.wrench.design.framework.link.model2.LinkArmory;
import cn.bugstack.wrench.design.framework.link.model2.chain.BusinessLinkedList;
import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.sweater.domain.trade.model.entity.TradeLockRuleCommandEntity;
import cn.sweater.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import cn.sweater.domain.trade.service.lock.filter.ActivityUsabilityRuleFilter;
import cn.sweater.domain.trade.service.lock.filter.TeamStockOccupyRuleFilter;
import cn.sweater.domain.trade.service.lock.filter.UserTakeLimitRuleFilter;

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
    private static String teamStockKey = "group_buy_market_team_stock_key_";
    @Bean("tradeRuleFilter")
    BusinessLinkedList<TradeLockRuleCommandEntity, DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter
            (ActivityUsabilityRuleFilter activityUsabilityRuleFilter, UserTakeLimitRuleFilter userTakeLimitRuleFilter, TeamStockOccupyRuleFilter teamStockOccupyRuleFilter) {
        LinkArmory<TradeLockRuleCommandEntity, DynamicContext, TradeLockRuleFilterBackEntity> linkArmory
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
            return TradeRuleFilterFactory.generateTeamStockKey(teamId,groupBuyActivity.getActivityId());
        }

        public String generateRecoveryTeamStockKey(String teamId) {
            if (StringUtils.isBlank(teamId)) return null;
            return TradeRuleFilterFactory.generateRecoveryTeamStockKey(teamId,groupBuyActivity.getActivityId());
        }


    }
    public static String generateTeamStockKey(String teamId,Long activityId) {
        return teamStockKey + activityId + "_" + teamId;
    }
    public static String generateRecoveryTeamStockKey(String teamId,Long activityId) {
        return teamStockKey + activityId + "_" + teamId + "_recovery";
    }
}
