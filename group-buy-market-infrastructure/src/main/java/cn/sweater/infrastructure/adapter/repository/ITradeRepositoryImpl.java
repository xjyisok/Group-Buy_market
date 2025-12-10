package cn.sweater.infrastructure.adapter.repository;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.aggergate.GroupBuyOrderAggregate;
import cn.sweater.domain.trade.model.aggergate.GroupBuyRefundAggregate;
import cn.sweater.domain.trade.model.aggergate.GroupBuyTeamSettlementAggregate;
import cn.sweater.domain.trade.model.entity.*;
import cn.sweater.domain.trade.model.valobj.*;
import cn.sweater.infrastructure.dao.IGroupBuyActivityDao;
import cn.sweater.infrastructure.dao.IGroupBuyOrderDao;
import cn.sweater.infrastructure.dao.IGroupBuyOrderListDao;
import cn.sweater.infrastructure.dao.INotifyTaskDao;
import cn.sweater.infrastructure.dao.po.GroupBuyActivity;
import cn.sweater.infrastructure.dao.po.GroupBuyOrder;
import cn.sweater.infrastructure.dao.po.GroupBuyOrderList;
import cn.sweater.infrastructure.dao.po.NotifyTask;
import cn.sweater.infrastructure.dcc.DCCService;
import cn.sweater.infrastructure.redis.IRedisService;
import cn.sweater.types.common.Constants;
import cn.sweater.types.enums.ActivityStatusEnumVO;
import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private IRedisService redisService;
    @Resource
    DCCService dccService;
    @Value("${spring.rabbitmq.config.producer.topic_team_success.routing_key}")
    private String topic_team_success;
    @Value("${spring.rabbitmq.config.producer.topic_team_refund.routing_key}")
    private String topic_team_refund;
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
                .payPrice(groupBuyOrderListRes.getPayPrice())
                .originalPrice(groupBuyOrderListRes.getOriginalPrice())
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
                    .notifyUrl(payDiscountEntity.getNotifyConfigVO().getNotifyUrl())
                    .status(0)
                    .notifyType(payDiscountEntity.getNotifyConfigVO().getNotifyType().getCode())
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
                .payPrice(payDiscountEntity.getPayPrice())
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
                .payPrice(payDiscountEntity.getPayPrice())
                .originalPrice(payDiscountEntity.getOriginalPrice())
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
                .notifyConfig(NotifyConfigVO.builder()
                        .notifyUrl(groupBuyOrder.getNotifyUrl())
                        .notifyType(NotifyTypeEnumVO.valueOf(groupBuyOrder.getNotifyType()))
                        .notifyMQ(topic_team_success)
                        .build())
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
            notifyTask.setNotifyUrl(NotifyTypeEnumVO.HTTP.equals(groupBuyTeamEntity.getNotifyConfig().getNotifyType()) ?
                    groupBuyTeamEntity.getNotifyConfig().getNotifyUrl() : null);
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setNotifyType(groupBuyTeamEntity.getNotifyConfig().getNotifyType().getCode());
            notifyTask.setNotifyMQ(NotifyTypeEnumVO.MQ.equals(groupBuyTeamEntity.getNotifyConfig().getNotifyType()) ?
                    groupBuyTeamEntity.getNotifyConfig().getNotifyMQ() : null);
            notifyTask.setUuid(groupBuyTeamEntity.getTeamId()+Constants.UNDERLINE+TaskNotifyCategoryEnumVO.TRADE_SETTLEMENT.getCode()
                    +Constants.UNDERLINE+tradePaySuccessEntity.getOutTradeNo());
            notifyTask.setNotifyCategory(TaskNotifyCategoryEnumVO.TRADE_SETTLEMENT.getCode());
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
                    .notifyMQ(notifyTask.getNotifyMQ())
                    .notifyStatus(notifyTask.getNotifyStatus())
                    .parameterJson(notifyTask.getParameterJson())
                    .notifyType(notifyTask.getNotifyType())
                    .uuid(notifyTask.getUuid())
                    .build();

            notifyTaskEntities.add(notifyTaskEntity);
        }

        return notifyTaskEntities;

    }

    @Override
    public int updateNotifyTaskStatusSuccess(String uuid) {
        return notifyTaskDao.updateNotifyTaskStatusSuccess(uuid);
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
                    .notifyMQ(notifyTask.getNotifyMQ())
                    .notifyType(notifyTask.getNotifyType())
                    .build();

            notifyTaskEntities.add(notifyTaskEntity);
        }

        return notifyTaskEntities;
    }

    @Override
    public boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer target, Integer validTime) {
        // 失败恢复量
        Long recoveryCount = redisService.getAtomicLong(recoveryTeamStockKey);
        recoveryCount = null == recoveryCount ? 0 : recoveryCount;

        // 1. incr 得到值，与总量和恢复量做对比。恢复量为系统失败时候记录的量。
        // 2. 从有组队量开始，相当于已经有了一个占用量，所以要 +1
        System.out.println(redisService.getAtomicLong(teamStockKey));
        long occupy = redisService.incr(teamStockKey) + 1;

        if (occupy > target + recoveryCount) {
            redisService.setAtomicLong(teamStockKey, target);
            return false;
        }

        // 1. 给每个产生的值加锁为兜底设计，虽然incr操作是原子的，基本不会产生一样的值。但在实际生产中，遇到过集群的运维配置问题，以及业务运营配置数据问题，导致incr得到的值相同。
        // 2. validTime + 60分钟，是一个延后时间的设计，让数据保留时间稍微长一些，便于排查问题。
        String lockKey = teamStockKey + Constants.UNDERLINE + occupy;
        Boolean lock = redisService.setNx(lockKey, validTime + 60, TimeUnit.MINUTES);

        if (!lock) {
            log.info("组队库存加锁失败 {}", lockKey);
        }

        return lock;

    }

    @Override
    public void recoveryTeamStock(String recoveryTeamStockKey, Integer validTime) {
        // 首次组队拼团，是没有 teamId 的，所以不需要这个做处理。
        if (StringUtils.isBlank(recoveryTeamStockKey)) return;
        redisService.incr(recoveryTeamStockKey);
    }

    @Override
    @Transactional(timeout = 5000)
    public NotifyTaskEntity unpaid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();
        GroupBuyProgressVO groupBuyProgressVO = groupBuyRefundAggregate.getGroupBuyProgressVO();
        GroupBuyOrderList groupBuyOrderListReq=new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(tradeRefundOrderEntity.getUserId());
        groupBuyOrderListReq.setOrderId(tradeRefundOrderEntity.getOrderId());
        int updateUnpaid2RefundCount=groupBuyOrderListDao.unpaid2Refund(groupBuyOrderListReq);
        if(updateUnpaid2RefundCount!=1){
            log.error("更新订单记录-unpaid(未支付退单)失败{}{}",tradeRefundOrderEntity.getUserId(),tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
        }
        GroupBuyOrder groupBuyOrderReq=new GroupBuyOrder();
        groupBuyOrderReq.setTeamId(tradeRefundOrderEntity.getTeamId());
        groupBuyOrderReq.setLockCount(groupBuyProgressVO.getLockCount());
        //System.out.println(groupBuyOrderReq.getTeamId()+groupBuyOrderReq.getLockCount());
        int updateUnpaid2RefundCountOrder=groupBuyOrderDao.unpaid2Refund(groupBuyOrderReq);
        if(updateUnpaid2RefundCountOrder!=1){
            log.error("更新组队记录-unpaid(未支付退单)失败{}{}",tradeRefundOrderEntity.getUserId(),tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
        }
        NotifyTask notifyTask=new NotifyTask();
        notifyTask.setActivityId(tradeRefundOrderEntity.getActivityId());
        notifyTask.setTeamId(tradeRefundOrderEntity.getTeamId());
        notifyTask.setNotifyType(NotifyTypeEnumVO.MQ.getCode());
        notifyTask.setNotifyMQ(topic_team_refund);
        notifyTask.setNotifyCount(0);
        notifyTask.setNotifyStatus(0);
        notifyTask.setNotifyCategory(TaskNotifyCategoryEnumVO.TRADE_UNPAID2REFUND.getCode());
        notifyTask.setUuid(tradeRefundOrderEntity.getTeamId()+Constants.UNDERLINE+TaskNotifyCategoryEnumVO.TRADE_UNPAID2REFUND.getCode()
        +Constants.UNDERLINE+tradeRefundOrderEntity.getOrderId());
        notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
            put("type", RefundTypeEnumVO.UNPAID_UNLOCK.getCode());
            put("teamId", tradeRefundOrderEntity.getTeamId());
            put("userId", tradeRefundOrderEntity.getUserId());
            put("orderId", tradeRefundOrderEntity.getOrderId());
            put("activityId", tradeRefundOrderEntity.getActivityId());
        }}));
        notifyTaskDao.insert(notifyTask);
        return NotifyTaskEntity.builder()
                .teamId(tradeRefundOrderEntity.getTeamId())
                .notifyUrl(notifyTask.getNotifyUrl())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyCount(notifyTask.getNotifyCount())
                .notifyStatus(notifyTask.getNotifyStatus())
                .parameterJson(JSON.toJSONString(notifyTask.getParameterJson()))
                .uuid(notifyTask.getUuid())
                .build();
    }

    @Override
    @Transactional(timeout = 5000)
    public NotifyTaskEntity paid2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();
        GroupBuyProgressVO groupBuyProgressVO = groupBuyRefundAggregate.getGroupBuyProgressVO();
        GroupBuyOrderList groupBuyOrderListReq=new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(tradeRefundOrderEntity.getUserId());
        groupBuyOrderListReq.setOrderId(tradeRefundOrderEntity.getOrderId());
        int updatePaid2RefundCount=groupBuyOrderListDao.paid2Refund(groupBuyOrderListReq);
        if(updatePaid2RefundCount!=1){
            log.error("更新订单记录-paid(已经支付退单)失败{}{}",tradeRefundOrderEntity.getUserId(),tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
        }
        GroupBuyOrder groupBuyOrderReq=new GroupBuyOrder();
        groupBuyOrderReq.setTeamId(tradeRefundOrderEntity.getTeamId());
        groupBuyOrderReq.setLockCount(groupBuyProgressVO.getLockCount());
        groupBuyOrderReq.setCompleteCount(groupBuyProgressVO.getCompleteCount());
        //System.out.println(groupBuyOrderReq.getTeamId()+groupBuyOrderReq.getLockCount());
        int updatePaid2RefundCountOrder=groupBuyOrderDao.paid2Refund(groupBuyOrderReq);
        if(updatePaid2RefundCountOrder!=1){
            log.error("更新组队记录-paid(已支付退单)失败{}{}",tradeRefundOrderEntity.getUserId(),tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
        }
        NotifyTask notifyTask=new NotifyTask();
        notifyTask.setActivityId(tradeRefundOrderEntity.getActivityId());
        notifyTask.setTeamId(tradeRefundOrderEntity.getTeamId());
        notifyTask.setNotifyType(NotifyTypeEnumVO.MQ.getCode());
        notifyTask.setNotifyMQ(topic_team_refund);
        notifyTask.setNotifyCount(0);
        notifyTask.setNotifyStatus(0);
        notifyTask.setNotifyCategory(TaskNotifyCategoryEnumVO.TRADE_PAID2REFUND.getCode());
        notifyTask.setUuid(tradeRefundOrderEntity.getTeamId()+Constants.UNDERLINE+TaskNotifyCategoryEnumVO.TRADE_PAID2REFUND.getCode()
                +Constants.UNDERLINE+tradeRefundOrderEntity.getOrderId());
        notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
            put("type", RefundTypeEnumVO.PAID_UNFORMED.getCode());
            put("teamId", tradeRefundOrderEntity.getTeamId());
            put("userId", tradeRefundOrderEntity.getUserId());
            put("orderId", tradeRefundOrderEntity.getOrderId());
            put("activityId", tradeRefundOrderEntity.getActivityId());
        }}));
        notifyTaskDao.insert(notifyTask);
        return NotifyTaskEntity.builder()
                .teamId(tradeRefundOrderEntity.getTeamId())
                .notifyUrl(notifyTask.getNotifyUrl())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyCount(notifyTask.getNotifyCount())
                .notifyStatus(notifyTask.getNotifyStatus())
                .parameterJson(JSON.toJSONString(notifyTask.getParameterJson()))
                .uuid(notifyTask.getUuid())
                .build();
    }

    @Override
    @Transactional(timeout = 5000)
    public NotifyTaskEntity paidTeam2Refund(GroupBuyRefundAggregate groupBuyRefundAggregate) {
        TradeRefundOrderEntity tradeRefundOrderEntity = groupBuyRefundAggregate.getTradeRefundOrderEntity();
        GroupBuyProgressVO groupBuyProgressVO = groupBuyRefundAggregate.getGroupBuyProgressVO();
        GroupBuyOrderEnumVO groupBuyOrderEnumVO = groupBuyRefundAggregate.getGroupBuyOrderEnumVO();
        GroupBuyOrderList groupBuyOrderListReq=new GroupBuyOrderList();
        groupBuyOrderListReq.setUserId(tradeRefundOrderEntity.getUserId());
        groupBuyOrderListReq.setOrderId(tradeRefundOrderEntity.getOrderId());
        int updatePaidTeam2RefundCount=groupBuyOrderListDao.paidTeam2Refund(groupBuyOrderListReq);
        if(updatePaidTeam2RefundCount!=1){
            log.error("更新订单记录-paidTeam(已支付-已成团退单)失败{}{}",tradeRefundOrderEntity.getUserId(),tradeRefundOrderEntity.getOrderId());
            throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
        }
        GroupBuyOrder groupBuyOrderReq=new GroupBuyOrder();
        groupBuyOrderReq.setTeamId(tradeRefundOrderEntity.getTeamId());
        groupBuyOrderReq.setLockCount(groupBuyProgressVO.getLockCount());
        groupBuyOrderReq.setCompleteCount(groupBuyProgressVO.getCompleteCount());
        //System.out.println(groupBuyOrderReq.getTeamId()+groupBuyOrderReq.getLockCount());
        if(GroupBuyOrderEnumVO.COMPLETE_FAIL.equals(groupBuyOrderEnumVO)){
            int updatePaidTeam2RefundCountOrder=groupBuyOrderDao.paidTeam2Refund(groupBuyOrderReq);
            if(updatePaidTeam2RefundCountOrder!=1){
                log.error("更新组队记录-unpaid(已支付-已成团退单)失败{}{}",tradeRefundOrderEntity.getUserId(),tradeRefundOrderEntity.getOrderId());
                throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
            }
        } else if (GroupBuyOrderEnumVO.FAIL.equals(groupBuyOrderEnumVO)) {
            int updatePaidTeam2RefundCountOrder=groupBuyOrderDao.paidTeam2RefundFail(groupBuyOrderReq);
            if(updatePaidTeam2RefundCountOrder!=1){
                log.error("更新组队记录-unpaid(已支付-已成团退单)失败{}{}",tradeRefundOrderEntity.getUserId(),tradeRefundOrderEntity.getOrderId());
                throw new AppException(ResponseCode.UPDATE_ZERO.getCode());
            }
        }
        NotifyTask notifyTask=new NotifyTask();
        notifyTask.setActivityId(tradeRefundOrderEntity.getActivityId());
        notifyTask.setTeamId(tradeRefundOrderEntity.getTeamId());
        notifyTask.setNotifyType(NotifyTypeEnumVO.MQ.getCode());
        notifyTask.setNotifyMQ(topic_team_refund);
        notifyTask.setNotifyCount(0);
        notifyTask.setNotifyStatus(0);
        notifyTask.setNotifyCategory(TaskNotifyCategoryEnumVO.TRADE_PAID_TEAM2REFUND.getCode());
        notifyTask.setUuid(tradeRefundOrderEntity.getTeamId()+Constants.UNDERLINE+TaskNotifyCategoryEnumVO.TRADE_PAID_TEAM2REFUND.getCode()
                +Constants.UNDERLINE+tradeRefundOrderEntity.getOrderId());
        notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
            put("type", RefundTypeEnumVO.PAID_FORMED.getCode());
            put("teamId", tradeRefundOrderEntity.getTeamId());
            put("userId", tradeRefundOrderEntity.getUserId());
            put("orderId", tradeRefundOrderEntity.getOrderId());
            put("activityId", tradeRefundOrderEntity.getActivityId());
        }}));
        notifyTaskDao.insert(notifyTask);
        return NotifyTaskEntity.builder()
                .teamId(tradeRefundOrderEntity.getTeamId())
                .notifyUrl(notifyTask.getNotifyUrl())
                .notifyType(notifyTask.getNotifyType())
                .notifyMQ(notifyTask.getNotifyMQ())
                .notifyCount(notifyTask.getNotifyCount())
                .notifyStatus(notifyTask.getNotifyStatus())
                .parameterJson(JSON.toJSONString(notifyTask.getParameterJson()))
                .uuid(notifyTask.getUuid())
                .build();
    }
}
