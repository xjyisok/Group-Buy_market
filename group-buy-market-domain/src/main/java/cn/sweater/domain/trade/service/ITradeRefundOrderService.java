package cn.sweater.domain.trade.service;

import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderListDetailEntity;
import cn.sweater.domain.trade.model.entity.TradeRefundBehaviorEntity;
import cn.sweater.domain.trade.model.entity.TradeRefundCommandEntity;
import cn.sweater.domain.trade.model.valobj.TeamRefundSuccess;

import java.util.List;

public interface ITradeRefundOrderService {
    TradeRefundBehaviorEntity refundOrder(TradeRefundCommandEntity tradeRefundCommandEntity) throws Exception;

    void restoreTeamStockLock(TeamRefundSuccess teamRefundSuccess);

    List<UserGroupBuyOrderListDetailEntity> queryTimeOutUnpaidOrder();
}
