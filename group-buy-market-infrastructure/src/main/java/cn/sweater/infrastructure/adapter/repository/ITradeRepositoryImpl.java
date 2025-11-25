package cn.sweater.infrastructure.adapter.repository;

import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.aggergate.GroupBuyOrderAggregate;
import cn.sweater.domain.trade.model.entity.MarketPayOrderEntity;
import cn.sweater.domain.trade.model.entity.PayActivityEntity;
import cn.sweater.domain.trade.model.entity.PayDiscountEntity;
import cn.sweater.domain.trade.model.entity.UserEntity;
import cn.sweater.domain.trade.model.valobj.GroupBuyProgressVO;
import cn.sweater.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import cn.sweater.infrastructure.dao.IGroupBuyOrderDao;
import cn.sweater.infrastructure.dao.IGroupBuyOrderListDao;
import cn.sweater.infrastructure.dao.po.GroupBuyOrder;
import cn.sweater.infrastructure.dao.po.GroupBuyOrderList;
import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Repository
public class ITradeRepositoryImpl implements ITradeRepository {
    @Resource
    IGroupBuyOrderListDao groupBuyOrderListDao;
    @Resource
    IGroupBuyOrderDao groupBuyOrderDao;
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
        String teamId= payActivityEntity.getTeamId();
        if(teamId==null){
            //NOTE:拼团发起人需要初始化一个平团号teamId
            //NOTE:GroupBuyOrder是平团订单的总单号，GroupBuyOrderList是拼团订单中每一条订单的明细一个拼团订单可能有多个人参与组成
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
                    .lockCount(1)
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
                .status(0)
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                .build();
        try{
            groupBuyOrderListDao.insert(groupBuyOrderList);
        } catch (Exception e) {
            throw new RuntimeException(ResponseCode.INDEX_EXCEPTION.getCode(),e);
        }
        return MarketPayOrderEntity.builder()
                .orderId(orderId)
                .deductionPrice(payDiscountEntity.getOriginalPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.CREATE)
                .build();
    }
}
