package cn.sweater.domain.trade.service.refund.business;

import cn.sweater.domain.trade.model.entity.TradeRefundOrderEntity;

public interface IRefundOrderStrategy {
    void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity);
}
