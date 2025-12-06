package cn.sweater.domain.trade.service.settlement.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import cn.sweater.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import cn.sweater.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;

import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service
public class SettableRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Resource
    private ITradeRepository tradeRepository;
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("结算规则过滤-交易有效时间校验{} outTradeNO:{}",requestParameter.getSource(),requestParameter.getOutTradeNo());
        MarketPayOrderEntity marketPayOrderEntity=dynamicContext.getMarketPayOrderEntity();
        GroupBuyTeamEntity groupBuyTeamEntity=tradeRepository.queryGroupBuyTeamByTeamId(marketPayOrderEntity.getTeamId());
        Date outTradeTime=requestParameter.getOutTradeTime();
        if(!outTradeTime.before(groupBuyTeamEntity.getValidEndTime())){
            throw new AppException(ResponseCode.E0106.getCode(),ResponseCode.E0106.getInfo());
        }
        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);
        return next(requestParameter, dynamicContext);
    }
}
