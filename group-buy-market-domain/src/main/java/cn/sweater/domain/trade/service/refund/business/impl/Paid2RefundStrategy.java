package cn.sweater.domain.trade.service.refund.business.impl;

import cn.sweater.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.sweater.domain.trade.service.refund.business.IRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Service("paid2RefundStrategy")
@Slf4j
public class Paid2RefundStrategy implements IRefundOrderStrategy {
    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {

    }
}
