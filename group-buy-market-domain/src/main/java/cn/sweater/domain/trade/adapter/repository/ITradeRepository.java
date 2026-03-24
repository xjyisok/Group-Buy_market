package cn.sweater.domain.trade.adapter.repository;

import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderListDetailEntity;
import cn.sweater.domain.trade.model.aggergate.GroupBuyOrderAggregate;
import cn.sweater.domain.trade.model.aggergate.GroupBuyRefundAggregate;
import cn.sweater.domain.trade.model.aggergate.GroupBuyTeamSettlementAggregate;
import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.sweater.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.sweater.domain.trade.model.valobj.TeamRefundSuccess;

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

    int updateNotifyTaskStatusSuccess(String uuid);

    int updateNotifyTaskStatusError(String uuid);

    int updateNotifyTaskStatusRetry(String uuid);

    boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime);

    void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime);

    NotifyTaskEntity unpaid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    NotifyTaskEntity paid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    NotifyTaskEntity paidTeam2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate);

    void refund2Recovery(String recoveryTeamStockKey, TeamRefundSuccess teamRefundSuccess);

    List<UserGroupBuyOrderListDetailEntity> queryTimeOutUnpaidOrder();

    /**
     * 临界并发时，将本地订单状态同步为已完成（status 0→1）
     * @param outTradeNo 外部交易单号
     */
    void syncOrderStatus2Complete(String outTradeNo);

    /**
     * 关闭超时未成功的拼团（只关 PROGRESS 状态）
     * @param teamId 团ID
     * @return 影响行数，0 表示团已被结算，跳过退款
     */
    int closeTimeOutTeam(String teamId);

    /**
     * 查询超时且仍为 PROGRESS 状态的团列表
     */
    List<String> queryTimeOutProgressTeams();

    /**
     * 查询指定团内所有未退款的用户订单（status in 0,1）
     */
    List<UserGroupBuyOrderListDetailEntity> queryUnRefundedOrdersByTeamId(String teamId);
}
