package cn.sweater.domain.activity.service.trial.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;
import cn.sweater.domain.activity.service.discount.IDiscountPreCalculateService;
import cn.sweater.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import cn.sweater.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.sweater.domain.activity.service.trial.thread.QueryGroupBuyActivityDiscountVOThreadTask;
import cn.sweater.domain.activity.service.trial.thread.QuerySkuVOFromDBThreadTask;

import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.*;
@Slf4j
@Service
public class MarketNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Resource
    private TagNode tagNode;
    @Resource
    private ErrorNode errorNode;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private Map<String,IDiscountPreCalculateService> discountPreCalculateServiceMap;
    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务-MarketNode userId:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));
        if(dynamicContext.getGroupBuyActivityDiscountVO()==null){
            return router(requestParameter,dynamicContext);
        }
        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount=dynamicContext.getGroupBuyActivityDiscountVO().getGroupBuyDiscount();
        String userId=requestParameter.getUserId();
        SkuVO sku=dynamicContext.getSkuVO();
        if(null==groupBuyDiscount||null==sku){
            return router(requestParameter,dynamicContext);
        }
        IDiscountPreCalculateService discountPreCalculateService=discountPreCalculateServiceMap.get(groupBuyDiscount.getMarketPlan());
        if (null == discountPreCalculateService) {
            log.info("不存在{}类型的折扣计算服务，支持类型为:{}", groupBuyDiscount.getMarketPlan(), JSON.toJSONString(discountPreCalculateServiceMap.keySet()));
            throw new AppException(ResponseCode.E0001.getCode(), ResponseCode.E0001.getInfo());
        }


        BigDecimal payPrice=discountPreCalculateService.calculate(userId,sku.getOriginalPrice(),groupBuyDiscount);
        dynamicContext.setDeductedPrice(sku.getOriginalPrice().subtract(payPrice));
        dynamicContext.setPayPrice(payPrice);
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        if (null == dynamicContext.getGroupBuyActivityDiscountVO() || null == dynamicContext.getSkuVO() || null == dynamicContext.getDeductedPrice()) {
            return errorNode;
        }

        return tagNode;

    }

    @Override
    protected void multiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

        QueryGroupBuyActivityDiscountVOThreadTask queryGroupBuyActivityDiscountVOThreadTask=new QueryGroupBuyActivityDiscountVOThreadTask(requestParameter.getSource()
                ,requestParameter.getChannel(),requestParameter.getGoodsId(),activityRepository);
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
