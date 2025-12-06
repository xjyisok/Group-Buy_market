package cn.sweater.domain.trade.service.settlement.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import cn.sweater.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import cn.sweater.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;

import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SCRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Resource
    private ITradeRepository tradeRepository;
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("结算规则过滤-渠道黑名单校验{} outTradeNO:{}",requestParameter.getSource(),requestParameter.getOutTradeNo());
        boolean isIntercept=tradeRepository.isSCIntercept(requestParameter.getSource(),requestParameter.getChannel());
        if(isIntercept){
            throw new AppException(ResponseCode.E0105.getCode(),ResponseCode.E0105.getInfo());
        }
        return next(requestParameter,dynamicContext);
    }
}
