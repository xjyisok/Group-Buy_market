package cn.sweater.domain.activity.service.trial;

import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.sweater.types.design.framework.tree.StrategyHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class IIndexGroupBuyMarketServiceImpl implements IIndexGroupBuyMarketService {
    @Resource
    DefaultActivityStrategyFactory activityStrategyFactory;
    @Override
    public TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception {
        StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity>strategyHandler
                =activityStrategyFactory.strategyHandler();
        TrialBalanceEntity trialBalanceEntity=strategyHandler.apply(marketProductEntity,new DefaultActivityStrategyFactory.DynamicContext());
        return trialBalanceEntity;
    }
}
