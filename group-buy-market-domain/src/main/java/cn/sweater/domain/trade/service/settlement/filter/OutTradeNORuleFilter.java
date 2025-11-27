package cn.sweater.domain.trade.service.settlement.filter;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import cn.sweater.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import cn.sweater.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import cn.sweater.types.design.framework.link.model2.handler.ILogicHandler;
import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class OutTradeNORuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {
    @Resource
    ITradeRepository tradeRepository;
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        log.info("结算规则过滤-外部单号校验{} outTradeNO:{}",requestParameter.getSource(),requestParameter.getOutTradeNo());
        //查询拼团信息
        MarketPayOrderEntity marketPayOrderEntity=tradeRepository.queryNoPayMarketPayOrderByOutTradeNo(requestParameter.getUserId(),requestParameter.getOutTradeNo());
        System.out.println(requestParameter.getUserId()+requestParameter.getOutTradeNo());
        if(marketPayOrderEntity==null){
            throw new AppException(ResponseCode.E0104.getCode(),ResponseCode.E0104.getInfo());
        }
        dynamicContext.setMarketPayOrderEntity(marketPayOrderEntity);
        return next(requestParameter,dynamicContext);
    }
}
