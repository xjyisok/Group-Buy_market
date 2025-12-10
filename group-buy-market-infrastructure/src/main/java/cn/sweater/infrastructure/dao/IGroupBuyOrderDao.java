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
}
