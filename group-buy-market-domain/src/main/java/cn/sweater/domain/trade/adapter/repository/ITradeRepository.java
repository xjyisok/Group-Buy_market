package cn.sweater.domain.trade.adapter.repository;

import cn.sweater.domain.trade.model.aggergate.GroupBuyOrderAggregate;
import cn.sweater.domain.trade.model.aggergate.GroupBuyRefundAggregate;
import cn.sweater.domain.trade.model.aggergate.GroupBuyTeamSettlementAggregate;
import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.sweater.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;

import java.util.List;

public interface ITradeRepository {

    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    public GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    Integer queryOrderCountByActivityId(Long activityId, String userId);

    GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId);

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

    void settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    boolean isSCIntercept(String source, String channel);

    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList();
    List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId);

    int updateNotifyTaskStatusSuccess(String teamId);

    int updateNotifyTaskStatusError(String teamId);

    int updateNotifyTaskStatusRetry(String teamId);

    boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime);

    void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime);

    void unpaid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    NotifyTaskEntity paid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);
}
