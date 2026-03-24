package cn.sweater.infrastructure.dao;

import cn.sweater.infrastructure.dao.po.GroupBuyOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @description 用户拼单
 * @create 2025-01-11 10:33
 */
@Mapper
public interface IGroupBuyOrderDao {

    List<GroupBuyOrder> queryInProgressGroupByTeamIds(@Param("teamIds") Set<String> teamIds);

    List<GroupBuyOrder> queryInProgressGroupByTeamIdsUserSelf(@Param("teamIds") Set<String> teamIds);

    int updateAddCompleteCount(String teamId);

    int updateOrderStatus2Complete(String teamId);

    void insert(GroupBuyOrder groupBuyOrder);

    int updateAddLockCount(String teamId);

    int updateSubtractionLockCount(String teamId);

    GroupBuyOrder queryGroupBuyProgress(String teamId);

    GroupBuyOrder queryGroupBuyTeamByTeamId(String teamId);

    Integer queryAllTeamCount(@Param("teamIds") Set<String> teamIds);

    Integer queryAllTeamCompleteCount(@Param("teamIds") Set<String> teamIds);

    Integer queryAllUserCount(@Param("teamIds") Set<String> teamIds);

    int unpaid2Refund(GroupBuyOrder groupBuyOrderReq);

    int paid2Refund(GroupBuyOrder groupBuyOrderReq);

    int paidTeam2Refund(GroupBuyOrder groupBuyOrderReq);

    int paidTeam2RefundFail(GroupBuyOrder groupBuyOrderReq);

    /**
     * 关闭超时未成功的拼团，只允许关 status=0 且 valid_end_time < now() 的团
     * @return 影响行数，0 表示团已被结算为 COMPLETE，无需退款
     */
    int closeTimeOutTeam(String teamId);

    /**
     * 查询超时且仍为 PROGRESS 状态的团 teamId 列表
     */
    List<String> queryTimeOutProgressTeams();
}
