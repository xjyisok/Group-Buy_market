package cn.sweater.domain.activity.service.trial;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import cn.sweater.domain.activity.adapter.repository.IActivityRepository;


import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractGroupBuyMarketSupport<MarketProductEntity,DynamicContext,TrialBalanceEntity>extends AbstractMultiThreadStrategyRouter<MarketProductEntity,DynamicContext,TrialBalanceEntity> {
    protected int timeout=500;
    @Resource
    protected IActivityRepository activityRepository;
    @Override
    protected void multiThread(MarketProductEntity requestParameter, DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }
}
