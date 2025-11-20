package cn.sweater.domain.activity.service.trial.factory;

import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;
import cn.sweater.domain.activity.service.trial.node.RootNode;
import cn.sweater.types.design.framework.tree.StrategyHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.stereotype.Service;

@Service
public class DefaultActivityStrategyFactory {
    private final RootNode rootNode;

    public DefaultActivityStrategyFactory(RootNode rootNode) {
        this.rootNode = rootNode;
    }
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity>strategyHandler(){
        return rootNode;
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DynamicContext{
        private GroupBuyActivityDiscountVO groupBuyActivityDiscountVO;
        private SkuVO skuVO;
    }
}
