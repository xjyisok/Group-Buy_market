package cn.sweater.domain.trade.service.lock.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.sweater.domain.trade.model.entity.TradeLockRuleCommandEntity;
import cn.sweater.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import cn.sweater.domain.trade.service.lock.factory.TradeRuleFilterFactory;

import cn.sweater.types.enums.ActivityStatusEnumVO;
import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class ActivityUsabilityRuleFilter implements ILogicHandler<TradeLockRuleCommandEntity, TradeRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {
    @Resource
    ITradeRepository tradeRepository;
    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("交易规则过滤-活动的可用性校验userId:{} activityId:{}", requestParameter.getUserId(), requestParameter.getActivityId());
        GroupBuyActivityEntity groupBuyActivityEntity=tradeRepository.queryGroupBuyActivityEntityByActivityId(requestParameter.getActivityId());
        if(!groupBuyActivityEntity.getStatus().equals(ActivityStatusEnumVO.EFFECTIVE)){
            log.info("活动的可用性校验，非生效状态 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0101.getCode());
        }
        Date currentTime = new Date();
        if (currentTime.before(groupBuyActivityEntity.getStartTime()) || currentTime.after(groupBuyActivityEntity.getEndTime())) {
            log.info("活动的可用性校验，非可参与时间范围 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0102.getCode());
        }

        // 写入动态上下文
        dynamicContext.setGroupBuyActivity(groupBuyActivityEntity);

        // 走到下一个责任链节点
        return next(requestParameter, dynamicContext);

    }
}
