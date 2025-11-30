package cn.sweater.infrastructure.adapter.repository;

import cn.sweater.domain.activity.adapter.repository.IActivityRepository;
import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.ScSkuActivtiyVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;
import cn.sweater.domain.activity.model.valobj.TeamStatisticVO;
import cn.sweater.infrastructure.dao.*;
import cn.sweater.infrastructure.dao.po.*;
import cn.sweater.infrastructure.dcc.DCCService;
import cn.sweater.infrastructure.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class IActivityRepositoryImpl implements IActivityRepository {
    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;
    @Resource
    private ISkuDao skuDao;
    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private IScSkuActivityDao scSkuActivtiyDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private DCCService dccService;
    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;
    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId) {
//        GroupBuyActivity groupBuyActivityRes =new GroupBuyActivity();
//        groupBuyActivityRes.setSource(source);
//        groupBuyActivityRes.setChannel(channel);
        //查拼团活动
        GroupBuyActivity groupBuyActivity = groupBuyActivityDao.queryValidGroupBuyActivityId(activityId);
        if (groupBuyActivity == null) {
            return null;
        }
        String disCountId = groupBuyActivity.getDiscountId();
        GroupBuyDiscount groupBuyDiscount = groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(disCountId);
        if (groupBuyDiscount == null) {
            return null;
        }
        //查商品Id

        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscountVG = GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
                .discountId(groupBuyDiscount.getDiscountId())
                .discountName(groupBuyDiscount.getDiscountName())
                .discountDesc(groupBuyDiscount.getDiscountDesc())
                .discountType(groupBuyDiscount.getDiscountType())
                .tagId(groupBuyDiscount.getTagId())
                .marketPlan(groupBuyDiscount.getMarketPlan())
                .marketExpr(groupBuyDiscount.getMarketExpr())
                .build();
        return GroupBuyActivityDiscountVO.builder()
                .activityId(groupBuyActivity.getActivityId())
                .activityName(groupBuyActivity.getActivityName())
                //.source(groupBuyActivity.getSource())
                //.channel(groupBuyActivity.getChannel())
                //.goodsId(scSkuActivity.getGoodsId())
                .groupBuyDiscount(groupBuyDiscountVG)
                .groupType(groupBuyActivity.getGroupType())
                .takeLimitCount(groupBuyActivity.getTakeLimitCount())
                .target(groupBuyActivity.getTarget())
                .validTime(groupBuyActivity.getValidTime())
                .status(groupBuyActivity.getStatus())
                .startTime(groupBuyActivity.getStartTime())
                .endTime(groupBuyActivity.getEndTime())
                .tagId(groupBuyActivity.getTagId())
                .tagScope(groupBuyActivity.getTagScope())
                .build();

    }

    @Override
    public SkuVO querySkuByGoodsId(String goodsId) {
        Sku sku = skuDao.querySkuByGoodsId(goodsId);
        if (sku == null) {
            return null;
        }
        return SkuVO.builder()
                .goodsId(sku.getGoodsId())
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .build();

    }

    @Override
    public ScSkuActivtiyVO queryScSkuActivtiyByScGoodsId(String source, String channel, String goodsId) {
        ScSkuActivity scSkuActivtiy = scSkuActivtiyDao.querySCSkuActivityBySCGoodsId(ScSkuActivity.builder()
                .source(source)
                .channel(channel)
                .goodsId(goodsId)
                .build());
        if (scSkuActivtiy == null) {
            return null;
        }
        ScSkuActivtiyVO scSkuActivtiyVO = new ScSkuActivtiyVO();
        scSkuActivtiyVO.setGoodsId(goodsId);
        scSkuActivtiyVO.setChannel(channel);
        scSkuActivtiyVO.setSource(source);
        scSkuActivtiyVO.setActivityId(scSkuActivtiy.getActivityId());
        return scSkuActivtiyVO;
    }

    @Override
    public Boolean isWithinRange(String userId, String tagId) {
        RBitSet bitSet = redisService.getBitSet(tagId);
        if (!bitSet.isExists()) {
            return true;
        }
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }

    @Override
    public boolean isDowngradeSwitch() {
        return dccService.isDowngradeSwitch();
    }

    @Override
    public boolean isCutRange(String userId) {
        return dccService.isCutRange(userId);
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByOwner(Long activityId, String userId, int userSelfGroupNo) {
        //根据活动id查询用户参与的拼团Id
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setCount(userSelfGroupNo);
        List<GroupBuyOrderList> userGroupBuyOrderList = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByUserId(groupBuyOrderListReq);
        //System.out.println(userGroupBuyOrderList);
        if (null == userGroupBuyOrderList || userGroupBuyOrderList.isEmpty()) return null;
        //NOTE提前将用户参与的teamId和OutTradeNo匹配好
        Map<String,String> teamOutTradeNoMap=new HashMap<>();
        for(GroupBuyOrderList groupBuyOrderList : userGroupBuyOrderList) {
            teamOutTradeNoMap.put(groupBuyOrderList.getTeamId(),groupBuyOrderList.getOutTradeNo());
        }
        //过滤队伍获取Id
        Set<String> teamIds = userGroupBuyOrderList.stream().map(GroupBuyOrderList::getTeamId).
                filter(teamId -> teamId != null && !teamId.isEmpty()).collect(Collectors.toSet());
        System.out.println(teamIds);
        //根据teamId查询用户参与的拼团
        List<GroupBuyOrder> userGroupBuyOrder = groupBuyOrderDao.queryInProgressGroupByTeamIdsUserSelf(teamIds);
        if (null == userGroupBuyOrder || userGroupBuyOrder.isEmpty()) return null;
        //System.out.println("NoTTTTTTTTTTTTTTNULL");
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        //teamId和拼团订单映射
        Map<String, GroupBuyOrder> groupBuyOrderMap = userGroupBuyOrder.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, order -> order));
        //将用户自己参与的拼团信息组合
        for (String teamId : teamIds) {
            List<String>userIdList=groupBuyOrderListDao.queryUserIdsByTeamId(teamId);
//            System.out.println("_--------------------------------------");
//            System.out.println(userIdList);
//            System.out.println("----------------------------------------");
            GroupBuyOrder groupBuyOrder = groupBuyOrderMap.get(teamId);
            if (null == groupBuyOrder) continue;
            UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity = new UserGroupBuyOrderDetailEntity();
            userGroupBuyOrderDetailEntity.setUserIds(userIdList);
            userGroupBuyOrderDetailEntity.setTeamId(groupBuyOrder.getTeamId());
            userGroupBuyOrderDetailEntity.setTargetCount(groupBuyOrder.getTargetCount());
            userGroupBuyOrderDetailEntity.setActivityId(groupBuyOrder.getActivityId());
            userGroupBuyOrderDetailEntity.setCompleteCount(groupBuyOrder.getCompleteCount());
            userGroupBuyOrderDetailEntity.setLockCount(groupBuyOrder.getLockCount());
            userGroupBuyOrderDetailEntity.setValidEndTime(groupBuyOrder.getValidEndTime());
            userGroupBuyOrderDetailEntity.setValidStartTime(groupBuyOrder.getValidStartTime());
            userGroupBuyOrderDetailEntity.setOutTradeNo(teamOutTradeNoMap.get(teamId));
            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }
        //System.out.println(userGroupBuyOrderDetailEntities);
        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByRandom(List<UserGroupBuyOrderDetailEntity>userGroupBuyOrderDetailEntityList,Long activityId,
                                                                                                  String userId, int randomGroupNo) {
        GroupBuyOrderList groupBuyOrderListReq = new GroupBuyOrderList();
        groupBuyOrderListReq.setActivityId(activityId);
        groupBuyOrderListReq.setUserId(userId);
        groupBuyOrderListReq.setCount(randomGroupNo*2);
        List<GroupBuyOrderList> randomGroupBuyOrderList = groupBuyOrderListDao.queryInProgressRandomGroupBuyOrderDetailListByUserId(groupBuyOrderListReq);
        //NOTE获取当前用户自己参与的teamID防止重复
        Set<String> teamIdsSelf = userGroupBuyOrderDetailEntityList.stream().map(UserGroupBuyOrderDetailEntity::getTeamId).
                filter(teamId -> teamId != null && !teamId.isEmpty()).collect(Collectors.toSet());
        //NOTE这里过滤掉用户拼团订单下的orderlist是防止打乱的时候由于用户拼团订单下的orderlist比较多，非用户拼团订单一个也没保留
        randomGroupBuyOrderList = randomGroupBuyOrderList.stream()
                .filter(item -> !teamIdsSelf.contains(item.getTeamId()))
                .collect(Collectors.toList());
        if (randomGroupBuyOrderList.isEmpty()) return null;
        //判断总量是否大于
        if (randomGroupNo < randomGroupBuyOrderList.size()) {
            // 随机打乱列表
            Collections.shuffle(randomGroupBuyOrderList);
            // 获取前 randomCount 个元素
            randomGroupBuyOrderList = randomGroupBuyOrderList.subList(0, randomGroupNo);
        }

        //过滤队伍获取teamId
        Set<String> teamIds = randomGroupBuyOrderList.stream().map(GroupBuyOrderList::getTeamId).
                filter(teamId -> teamId != null && !teamId.isEmpty()&&!teamIdsSelf.contains(teamId)).collect(Collectors.toSet());
        //NOTE如果除了用户自己以外没有别的拼团了那么直接返回空防止sql语句异常
        if(teamIds.isEmpty()) return null;
        //根据teamId查询存在的拼团
        List<GroupBuyOrder> randomGroupBuyOrder = groupBuyOrderDao.queryInProgressGroupByTeamIds(teamIds);
        if (null == randomGroupBuyOrder || randomGroupBuyOrder.isEmpty()) return null;
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = new ArrayList<>();
        //teamId和拼团订单映射
        Map<String, GroupBuyOrder> groupBuyOrderMap = randomGroupBuyOrder.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, order -> order));
        //将存在的拼团信息组合
        for (String teamId : teamIds) {
            GroupBuyOrder groupBuyOrder = groupBuyOrderMap.get(teamId);
            List<String>userIdList=groupBuyOrderListDao.queryUserIdsByTeamId(teamId);
            if (null == groupBuyOrder) continue;
            UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity = new UserGroupBuyOrderDetailEntity();
            userGroupBuyOrderDetailEntity.setUserIds(userIdList);
            userGroupBuyOrderDetailEntity.setTeamId(teamId);
            userGroupBuyOrderDetailEntity.setTargetCount(groupBuyOrder.getTargetCount());
            userGroupBuyOrderDetailEntity.setActivityId(groupBuyOrder.getActivityId());
            userGroupBuyOrderDetailEntity.setCompleteCount(groupBuyOrder.getCompleteCount());
            userGroupBuyOrderDetailEntity.setLockCount(groupBuyOrder.getLockCount());
            userGroupBuyOrderDetailEntity.setValidEndTime(groupBuyOrder.getValidEndTime());
            userGroupBuyOrderDetailEntity.setValidStartTime(groupBuyOrder.getValidStartTime());
            userGroupBuyOrderDetailEntities.add(userGroupBuyOrderDetailEntity);
        }
        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public TeamStatisticVO quertTeamStatisticByActivtiyId(Long activityId) {
        // 1. 根据活动ID查询拼团队伍
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.queryInProgressUserGroupBuyOrderDetailListByActivityId(activityId);

        if (null == groupBuyOrderLists || groupBuyOrderLists.isEmpty()) {
            return new TeamStatisticVO(0, 0, 0);
        }

        // 2. 过滤队伍获取 TeamId
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .filter(teamId -> teamId != null && !teamId.isEmpty()) // 过滤非空和非空字符串
                .collect(Collectors.toSet());

        // 3. 统计数据
        Integer allTeamCount = groupBuyOrderDao.queryAllTeamCount(teamIds);
        Integer allTeamCompleteCount = groupBuyOrderDao.queryAllTeamCompleteCount(teamIds);
        Integer allTeamUserCount = groupBuyOrderDao.queryAllUserCount(teamIds);

        // 4. 构建对象
        return TeamStatisticVO.builder()
                .allTeamCount(allTeamCount)
                .allTeamCompleteCount(allTeamCompleteCount)
                .allTeamUserCount(allTeamUserCount)
                .build();
    }
}
