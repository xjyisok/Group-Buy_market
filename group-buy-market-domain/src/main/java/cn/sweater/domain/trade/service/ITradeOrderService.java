package cn.sweater.domain.trade.service;

import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;

public interface ITradeOrderService {
    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId,String outTradeNo);
    GroupBuyProgressVO queryGroupBuyProgress(String teamId);
    MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity,PayDiscountEntity payDiscountEntity) throws Exception;

}
