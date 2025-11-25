package cn.sweater.domain.trade.service;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.aggergate.GroupBuyOrderAggregate;
import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.entity.PayActivityEntity;
import cn.sweater.domain.trade.model.entity.PayDiscountEntity;
import cn.sweater.domain.trade.model.entity.UserEntity;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class TradeOrderImpl implements ITradeOrderService{
    @Resource
    private ITradeRepository tradeRepository;
    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        return tradeRepository.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        return tradeRepository.queryGroupBuyProgress(teamId);
    }

    @Override
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) {
        GroupBuyOrderAggregate groupBuyOrderAggregate = new GroupBuyOrderAggregate();
        groupBuyOrderAggregate.setPayActivityEntity(payActivityEntity);
        groupBuyOrderAggregate.setPayDiscountEntity(payDiscountEntity);
        groupBuyOrderAggregate.setUserEntity(userEntity);
        return tradeRepository.lockMarketPayOrder(groupBuyOrderAggregate);
    }
}
