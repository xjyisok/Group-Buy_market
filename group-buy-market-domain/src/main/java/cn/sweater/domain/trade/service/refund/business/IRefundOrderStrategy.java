package cn.sweater.domain.trade.service.refund.business;

import cn.sweater.domain.trade.model.entity.TradeRefundOrderEntity;
import cn.sweater.domain.trade.model.valobj.TeamRefundSuccess;

public interface IRefundOrderStrategy {
    void refundOrder(TradeRefundOrderEntity tradeRefundOrderEntity);

    void restoreTeamStockLock(TeamRefundSuccess teamRefundSuccess);
}
