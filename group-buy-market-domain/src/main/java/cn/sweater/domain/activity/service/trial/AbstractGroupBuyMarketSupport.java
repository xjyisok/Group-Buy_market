package cn.sweater.domain.activity.service.trial;

import cn.sweater.domain.activity.adapter.repository.IActivityRepository;
import cn.sweater.types.design.framework.tree.AbstractStrategyMultiThreadRouter;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractGroupBuyMarketSupport<MarketProductEntity,DynamicContext,TrialBalanceEntity>extends AbstractStrategyMultiThreadRouter<MarketProductEntity,DynamicContext,TrialBalanceEntity> {
    protected int timeout=500;
    @Resource
    protected IActivityRepository activityRepository;
    @Override
    protected void multithread(MarketProductEntity requestParameter, DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }
}
