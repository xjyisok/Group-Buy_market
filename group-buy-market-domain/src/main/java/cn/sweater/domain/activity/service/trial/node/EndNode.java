package cn.sweater.domain.activity.service.trial.node;
import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;
import cn.sweater.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import cn.sweater.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.sweater.types.design.framework.tree.StrategyHandler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EndNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Override
    public TrialBalanceEntity doapply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        SkuVO skuVO = dynamicContext.getSkuVO();
        return TrialBalanceEntity.builder()
                .goodsId(skuVO.getGoodsId())
                .goodsName(skuVO.getGoodsName())
                .originalPrice(skuVO.getOriginalPrice())
                .deductionPrice(new BigDecimal("0.00"))
                .targetCount(groupBuyActivityDiscountVO.getTarget())
                .startTime(groupBuyActivityDiscountVO.getStartTime())
                .endTime(groupBuyActivityDiscountVO.getEndTime())
                .isVisible(false)
                .isEnable(false)

                .build();
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultstrategyHandler;
    }
}
