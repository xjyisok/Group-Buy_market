package cn.sweater.domain.trade.adapter.repository;

import cn.sweater.domain.trade.model.aggergate.GroupBuyOrderAggregate;
import cn.sweater.domain.trade.model.aggergate.GroupBuyTeamSettlementAggregate;
import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.sweater.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;

public interface ITradeRepository {

    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    public GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    Integer queryOrderCountByActivityId(Long activityId, String userId);

    GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId);

    GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId);

    void settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    boolean isSCIntercept(String source, String channel);
}
