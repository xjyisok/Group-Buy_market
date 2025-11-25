package cn.sweater.domain.trade.service;

import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.entity.PayActivityEntity;
import cn.sweater.domain.trade.model.entity.PayDiscountEntity;
import cn.sweater.domain.trade.model.entity.UserEntity;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;

public interface ITradeOrderService {
    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId,String outTradeNo);
    GroupBuyProgressVO queryGroupBuyProgress(String teamId);
    MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity,PayDiscountEntity payDiscountEntity);
}
