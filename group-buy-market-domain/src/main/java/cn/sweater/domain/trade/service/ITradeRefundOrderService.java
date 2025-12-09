package cn.sweater.domain.trade.service;

import cn.sweater.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.sweater.domain.trade.model.entity.TradeRefundCommandEntity;

public interface ITradeRefundOrderService {
    TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity tradeRefundCommandEntity);
}
