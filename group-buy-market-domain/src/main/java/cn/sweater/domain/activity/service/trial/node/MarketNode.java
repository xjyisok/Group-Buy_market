package cn.sweater.domain.activity.service.trial.node;

import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;
import cn.sweater.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import cn.sweater.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.sweater.domain.activity.service.trial.thread.QueryGroupBuyActivityDiscountVOThreadTask;
import cn.sweater.domain.activity.service.trial.thread.QuerySkuVOFromDBThreadTask;
import cn.sweater.types.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.*;
@Slf4j
@Service
public class MarketNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Resource
    private EndNode endNode;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Override
    public TrialBalanceEntity doapply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务-MarketNode userId:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return endNode;
    }

    @Override
    protected void multithread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

        QueryGroupBuyActivityDiscountVOThreadTask queryGroupBuyActivityDiscountVOThreadTask=new QueryGroupBuyActivityDiscountVOThreadTask(requestParameter.getSource()
                ,requestParameter.getChannel(),activityRepository);
        FutureTask<GroupBuyActivityDiscountVO>groupBuyActivityDiscountVOFutureTask=
                new FutureTask<>(queryGroupBuyActivityDiscountVOThreadTask);
        QuerySkuVOFromDBThreadTask querySkuVOFromDBThreadTask=new QuerySkuVOFromDBThreadTask(requestParameter.getGoodsId(), activityRepository);
        FutureTask<SkuVO>skuVOFutureTask=new FutureTask<>(querySkuVOFromDBThreadTask);
        threadPoolExecutor.execute(skuVOFutureTask);
        threadPoolExecutor.execute(groupBuyActivityDiscountVOFutureTask);

        dynamicContext.setGroupBuyActivityDiscountVO(groupBuyActivityDiscountVOFutureTask.get(timeout, TimeUnit.MILLISECONDS));
        dynamicContext.setSkuVO(skuVOFutureTask.get(timeout, TimeUnit.MILLISECONDS));
        log.info("拼团商品查询试算服务-MarketNode userId:{} 异步线程加载数据「GroupBuyActivityDiscountVO、SkuVO」完成", requestParameter.getUserId());
    }
}
