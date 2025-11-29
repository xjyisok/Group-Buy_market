package cn.sweater.infrastructure.adapter.repository;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.aggergate.GroupBuyOrderAggregate;
import cn.sweater.domain.trade.model.aggergate.GroupBuyTeamSettlementAggregate;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.sweater.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.sweater.infrastructure.dao.IGroupBuyActivityDao;
import cn.sweater.infrastructure.dao.IGroupBuyOrderDao;
import cn.sweater.infrastructure.dao.IGroupBuyOrderListDao;
import cn.sweater.infrastructure.dao.INotifyTaskDao;
import cn.sweater.infrastructure.dao.po.GroupBuyActivity;
import cn.sweater.infrastructure.dao.po.GroupBuyOrder;
import cn.sweater.infrastructure.dao.po.GroupBuyOrderList;
import cn.sweater.infrastructure.dao.po.NotifyTask;
import cn.sweater.infrastructure.dcc.DCCService;
import cn.sweater.types.common.Constants;
import cn.sweater.types.enums.ActivityStatusEnumVO;
import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Repository
public class ITradeRepositoryImpl implements ITradeRepository {
    @Resource
    IGroupBuyOrderListDao groupBuyOrderListDao;
    @Resource
    IGroupBuyOrderDao groupBuyOrderDao;
    @Resource
    IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    INotifyTaskDao notifyTaskDao;
    @Resource
    DCCService dccService;
    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList groupBuyOrderList = new GroupBuyOrderList();
        groupBuyOrderList.setOutTradeNo(outTradeNo);
        groupBuyOrderList.setUserId(userId);
        GroupBuyOrderList groupBuyOrderListRes=groupBuyOrderListDao.queryGroupBuyOrderRecordByOutTradeNo(groupBuyOrderList);
        if(groupBuyOrderListRes==null){
            return null;
        }
        return MarketPayOrderEntity.builder()
                .teamId(groupBuyOrderListRes.getTeamId())
                .orderId(groupBuyOrderListRes.getOrderId())
                .deductionPrice(groupBuyOrderListRes.getDeductionPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.valueOf(groupBuyOrderListRes.getStatus()))
                .build();
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyProgress(teamId);
        if(groupBuyOrder==null){
            return null;
        }
        return GroupBuyProgressVO.builder()
                .targetCount(groupBuyOrder.getTargetCount())
                .lockCount(groupBuyOrder.getLockCount())
                .completeCount(groupBuyOrder.getCompleteCount())
                .build();
    }
    @Transactional(timeout = 500)
    @Override
    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate) {
        UserEntity userEntity = groupBuyOrderAggregate.getUserEntity();
        PayActivityEntity payActivityEntity = groupBuyOrderAggregate.getPayActivityEntity();
        PayDiscountEntity payDiscountEntity = groupBuyOrderAggregate.getPayDiscountEntity();
        Integer userTakeOrderCount=groupBuyOrderAggregate.getUserTakeOrderCount();
        String teamId= payActivityEntity.getTeamId();
        if(teamId==null){
            //NOTE:拼团发起人需要初始化一个平团号teamId
            //NOTE:GroupBuyOrder是平团订单的总单号，GroupBuyOrderList是拼团订单中每一条订单的明细一个拼团订单可能有多个人参与组成
            Date currentTime=new Date();
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(currentTime);
            calendar.add(Calendar.MINUTE,payActivityEntity.getValidTime());
            teamId= RandomStringUtils.randomAlphanumeric(8);
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder()
                    .teamId(teamId)
                    .activityId(payActivityEntity.getActivityId())
                    .source(payDiscountEntity.getSource())
                    .channel(payDiscountEntity.getChannel())
                    .originalPrice(payDiscountEntity.getOriginalPrice())
                    .deductionPrice(payDiscountEntity.getDeductionPrice())
                    .payPrice(payDiscountEntity.getOriginalPrice().subtract(payDiscountEntity.getDeductionPrice()))
                    .targetCount(payActivityEntity.getTargetCount())
                    .completeCount(0)
                    .validStartTime(currentTime)//NOTE:当前拼团单开始时间
                    .validEndTime(calendar.getTime())// NOTE:当前拼团单结束时间
                    .lockCount(1)
                    .notifyUrl(payDiscountEntity.getNotifyUrl())
                    .status(0)
                    .build();
            groupBuyOrderDao.insert(groupBuyOrder);
        }
        else{
            int updateNum=groupBuyOrderDao.updateAddLockCount(teamId);
            if(1!=updateNum){
                throw new AppException(ResponseCode.E0005.getCode());
            }
        }
        String orderId=RandomStringUtils.randomAlphanumeric(12);
        GroupBuyOrderList groupBuyOrderList=GroupBuyOrderList.builder()
                .orderId(orderId)
                .userId(userEntity.getUserId())
                .teamId(teamId)
                .activityId(payActivityEntity.getActivityId())
                .startTime(payActivityEntity.getStartTime())
                .endTime(payActivityEntity.getEndTime())
                .goodsId(payDiscountEntity.getGoodsId())
                .source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .status(TradeOrderStatusEnumVO.CREATE.getCode())
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                .bizId(payActivityEntity.getActivityId()+ Constants.UNDERLINE+userEntity.getUserId()+Constants.UNDERLINE+(userTakeOrderCount+1))
                .build();
        try{
            groupBuyOrderListDao.insert(groupBuyOrderList);
        } catch (Exception e) {
            throw new RuntimeException(ResponseCode.INDEX_EXCEPTION.getCode(),e);
        }
        return MarketPayOrderEntity.builder()
                .teamId(teamId)
                .orderId(orderId)
                .deductionPrice(payDiscountEntity.getOriginalPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE)
                .build();
    }

    @Override
    public Integer queryOrderCountByActivityId(Long activityId, String userId) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);
        return groupBuyOrderListDao.queryOrderCountByActivityId(groupBuyOrderListReq);

    }

    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityEntityByActivityId(Long activityId) {
        GroupBuyActivity groupBuyActivity = groupBuyActivityDao.queryGroupBuyActivityByActivityId(activityId);
        return GroupBuyActivityEntity.builder()
                .activityId(groupBuyActivity.getActivityId())
                .activityName(groupBuyActivity.getActivityName())
                .discountId(groupBuyActivity.getDiscountId())
                .groupType(groupBuyActivity.getGroupType())
                .takeLimitCount(groupBuyActivity.getTakeLimitCount())
                .target(groupBuyActivity.getTarget())
                .validTime(groupBuyActivity.getValidTime())
                .status(ActivityStatusEnumVO.valueOf(groupBuyActivity.getStatus()))
                .startTime(groupBuyActivity.getStartTime())
                .endTime(groupBuyActivity.getEndTime())
                .tagId(groupBuyActivity.getTagId())
                .tagScope(groupBuyActivity.getTagScope())
                .build();

    }

    @Override
    public GroupBuyTeamEntity queryGroupBuyTeamByTeamId(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.queryGroupBuyTeamByTeamId(teamId);
        //System.out.println(teamId);
        return GroupBuyTeamEntity.builder()
                .teamId(groupBuyOrder.getTeamId())
                .activityId(groupBuyOrder.getActivityId())
                .targetCount(groupBuyOrder.getTargetCount())
                .completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount())
                .status(GroupBuyOrderEnumVO.valueOf(groupBuyOrder.getStatus()))
                .validStartTime(groupBuyOrder.getValidStartTime())
                .validEndTime(groupBuyOrder.getValidEndTime())
                .notifyUrl(groupBuyOrder.getNotifyUrl())
                .build();

    }
    @Transactional(timeout = 500)
    @Override
    public void settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate) {
        UserEntity userEntity = groupBuyTeamSettlementAggregate.getUserEntity();
        TradePaySuccessEntity tradePaySuccessEntity = groupBuyTeamSettlementAggregate.getTradePaySuccessEntity();
        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamSettlementAggregate.getGroupBuyTeamEntity();
        // 1. 更新拼团订单明细状态
        GroupBuyOrderList groupBuyOrderListreq = new GroupBuyOrderList();
        groupBuyOrderListreq.setUserId(userEntity.getUserId());
        groupBuyOrderListreq.setOutTradeNo(tradePaySuccessEntity.getOutTradeNo());
        groupBuyOrderListreq.setOutTradeTime(tradePaySuccessEntity.getOutTradeTime());
        int updateNum=groupBuyOrderListDao.updateOrderStatus2Complete(groupBuyOrderListreq);
        if(1!=updateNum){
            throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
        }
        // 2. 更新拼团达成数量
        int updateCompleteNum=groupBuyOrderDao.updateAddCompleteCount(groupBuyTeamEntity.getTeamId());
        if(1!=updateCompleteNum){
            throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
        }
        // 3. 更新拼团完成状态
        if(groupBuyTeamEntity.getTargetCount()-groupBuyTeamEntity.getCompleteCount()==1){
            int updateOrderStatusNum=groupBuyOrderDao.updateOrderStatus2Complete(groupBuyTeamEntity.getTeamId());
            if(1!=updateOrderStatusNum){
                throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
            }
            // 查询拼团交易完成外部单号列表
            List<String> outTradeNoList = groupBuyOrderListDao.queryGroupBuyCompleteOrderOutTradeNoListByTeamId(groupBuyTeamEntity.getTeamId());

            // 拼团完成写入回调任务记录
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeamEntity.getActivityId());
            notifyTask.setTeamId(groupBuyTeamEntity.getTeamId());
            notifyTask.setNotifyUrl(groupBuyTeamEntity.getNotifyUrl());
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
                put("teamId", groupBuyTeamEntity.getTeamId());
                put("outTradeNoList", outTradeNoList);
            }}));

            notifyTaskDao.insert(notifyTask);
        }
    }

    @Override
    public boolean isSCIntercept(String source, String channel) {
        return dccService.isScBlackList(source, channel);
    }

    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList() {
        List<NotifyTask> notifyTaskList = notifyTaskDao.queryUnExecutedNotifyTaskList();
        //System.out.println(notifyTaskList.size());
        if (notifyTaskList.isEmpty()) return new ArrayList<>();

        List<NotifyTaskEntity> notifyTaskEntities = new ArrayList<>();
        for (NotifyTask notifyTask : notifyTaskList) {

            NotifyTaskEntity notifyTaskEntity = NotifyTaskEntity.builder()
                    .teamId(notifyTask.getTeamId())
                    .notifyUrl(notifyTask.getNotifyUrl())
                    .notifyCount(notifyTask.getNotifyCount())
                    .parameterJson(notifyTask.getParameterJson())
                    .build();

            notifyTaskEntities.add(notifyTaskEntity);
        }

        return notifyTaskEntities;

    }

    @Override
    public int updateNotifyTaskStatusSuccess(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusSuccess(teamId);
    }

    @Override
    public int updateNotifyTaskStatusRetry(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusRetry(teamId);
    }

    @Override
    public int updateNotifyTaskStatusError(String teamId) {
        return notifyTaskDao.updateNotifyTaskStatusError(teamId);
    }

    @Override
    public List<NotifyTaskEntity> queryUnExecutedNotifyTaskList(String teamId) {
        List<NotifyTask> notifyTaskList = notifyTaskDao.queryUnExecutedNotifyTaskList(teamId);
        if (notifyTaskList.isEmpty()) return new ArrayList<>();

        List<NotifyTaskEntity> notifyTaskEntities = new ArrayList<>();
        for (NotifyTask notifyTask : notifyTaskList) {

            NotifyTaskEntity notifyTaskEntity = NotifyTaskEntity.builder()
                    .teamId(notifyTask.getTeamId())
                    .notifyUrl(notifyTask.getNotifyUrl())
                    .notifyCount(notifyTask.getNotifyCount())
                    .parameterJson(notifyTask.getParameterJson())
                    .build();

            notifyTaskEntities.add(notifyTaskEntity);
        }

        return notifyTaskEntities;
    }
}
