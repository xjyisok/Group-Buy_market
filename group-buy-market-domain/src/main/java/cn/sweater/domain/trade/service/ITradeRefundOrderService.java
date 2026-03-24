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

    /**
     * 补偿退单：不走责任链，直接按支付单状态路由
     * status=0(未支付) → unpaid2Refund；status=1(已支付未成团) → paid2Refund
     */
    void compensateRefundOrder(TradeRefundCommandEntity command, Integer orderStatus) throws Exception;
}
