package cn.sweater.domain.activity.service.trial.node;

import cn.sweater.domain.activity.adapter.repository.IActivityRepository;
import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import cn.sweater.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.sweater.types.design.framework.tree.StrategyHandler;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;

@Service
public class TagNode  extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Resource
    private EndNode endNode;
    @Resource
    private IActivityRepository activityRepository;
    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return endNode;
    }

    @Override
    protected TrialBalanceEntity doapply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        String tagId = groupBuyActivityDiscountVO.getTagId();
        boolean isVisible = groupBuyActivityDiscountVO.isVisible();
        boolean isEnable = groupBuyActivityDiscountVO.isEnable();
        //未配置就是人人可见
        if(StringUtils.isBlank(tagId)){
            dynamicContext.setIsVisible(true);
            dynamicContext.setIsEnable(true);
            return router(requestParameter, dynamicContext);
        }
        System.out.println(isVisible);
        System.out.println(isEnable);
        System.out.println(tagId+":"+requestParameter.getUserId());
        boolean isWithin=activityRepository.isWithinRange(requestParameter.getUserId(),tagId);
        //NOTE暂时全部设置为true
        //dynamicContext.setIsVisible(isVisible||isWithin);
        //dynamicContext.setIsEnable(isEnable||isWithin);
        dynamicContext.setIsVisible(true);
        dynamicContext.setIsEnable(true);
        return router(requestParameter, dynamicContext);
    }
}
