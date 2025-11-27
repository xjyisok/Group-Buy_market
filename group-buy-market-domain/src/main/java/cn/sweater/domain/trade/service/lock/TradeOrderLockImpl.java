package cn.sweater.domain.trade.service.lock;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.aggergate.GroupBuyOrderAggregate;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.sweater.domain.trade.service.ITradeOrderService;
import cn.sweater.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import cn.sweater.types.design.framework.link.model2.chain.BusinessLinkedList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class TradeOrderLockImpl implements ITradeOrderService {
    @Resource
    private ITradeRepository tradeRepository;
    @Resource
    TradeRuleFilterFactory tradeRuleFilterFactory;
    @Resource
    private BusinessLinkedList<TradeLockRuleCommandEntity, TradeRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeRuleFilter;

    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        log.info("拼团交易查询未支付订单：{},outTradeNo:{}", userId, outTradeNo);
        return tradeRepository.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        log.info("拼团交易-查询拼单进度:{}",teamId);
        return tradeRepository.queryGroupBuyProgress(teamId);
    }

    @Override
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) throws Exception {
        TradeLockRuleFilterBackEntity tradeLockRuleFilterBackEntity =tradeRuleFilter.apply(TradeLockRuleCommandEntity.builder()
                        .activityId(payActivityEntity.getActivityId())
                        .userId(userEntity.getUserId())
                .build(),new TradeRuleFilterFactory.DynamicContext());
        Integer userTakeOrderCount= tradeLockRuleFilterBackEntity.getUserTakeOrderCount();
        GroupBuyOrderAggregate groupBuyOrderAggregate = new GroupBuyOrderAggregate();
        groupBuyOrderAggregate.setPayActivityEntity(payActivityEntity);
        groupBuyOrderAggregate.setPayDiscountEntity(payDiscountEntity);
        groupBuyOrderAggregate.setUserEntity(userEntity);
        groupBuyOrderAggregate.setUserTakeOrderCount(userTakeOrderCount);
        return tradeRepository.lockMarketPayOrder(groupBuyOrderAggregate);
    }
}
