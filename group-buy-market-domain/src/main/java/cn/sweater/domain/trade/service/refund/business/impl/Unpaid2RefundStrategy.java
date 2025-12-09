package cn.sweater.domain.trade.service.refund.business.impl;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.aggergate.GroupBuyRefundAggregate;
import cn.sweater.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.sweater.domain.trade.service.refund.business.IRefundOrderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("unpaid2RefundStrategy")
@Slf4j
public class Unpaid2RefundStrategy implements IRefundOrderStrategy {
    @Resource
    private ITradeRepository tradeRepository;
    @Override
    public void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity) {
        tradeRepository.unpaid2Refund(GroupBuyRefundAggregate.buildUnpaid2RefundAggregate(tradeRefundOrderEntity,-1));
    }
}
