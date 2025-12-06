package cn.sweater.domain.activity.service.trial.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import cn.sweater.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;

import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
@Service
@Slf4j
public class SwitchNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Resource
    private MarketNode marketNode;
    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        if(activityRepository.isDowngradeSwitch()){
            log.info("拼团活动降级拦截 {}", requestParameter.getUserId());
            throw new AppException(ResponseCode.E0003.getCode(), ResponseCode.E0003.getInfo());

        }
        if (!activityRepository.isCutRange(requestParameter.getUserId())){
            log.info("拼团活动切量拦截 {}", requestParameter.getUserId());
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());

        }
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return marketNode;
    }
}
