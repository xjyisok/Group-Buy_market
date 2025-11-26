package cn.sweater.infrastructure.adapter.repository;

import cn.sweater.domain.activity.adapter.repository.IActivityRepository;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.ScSkuActivtiyVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;
import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;
import cn.sweater.infrastructure.dao.IGroupBuyActivityDao;
import cn.sweater.infrastructure.dao.IGroupBuyDiscountDao;
import cn.sweater.infrastructure.dao.IScSkuActivityDao;
import cn.sweater.infrastructure.dao.ISkuDao;
import cn.sweater.infrastructure.dao.po.GroupBuyActivity;
import cn.sweater.infrastructure.dao.po.GroupBuyDiscount;
import cn.sweater.infrastructure.dao.po.ScSkuActivity;
import cn.sweater.infrastructure.dao.po.Sku;
import cn.sweater.infrastructure.dcc.DCCService;
import cn.sweater.infrastructure.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

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
    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId) {
//        GroupBuyActivity groupBuyActivityRes =new GroupBuyActivity();
//        groupBuyActivityRes.setSource(source);
//        groupBuyActivityRes.setChannel(channel);
        //查拼团活动
        GroupBuyActivity groupBuyActivity=groupBuyActivityDao.queryValidGroupBuyActivityId(activityId);
        if (groupBuyActivity==null){
            return null;
        }
        String disCountId=groupBuyActivity.getDiscountId();
        GroupBuyDiscount groupBuyDiscount =groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(disCountId);
        if (groupBuyDiscount==null){
            return null;
        }
        //查商品Id

        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscountVG=GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
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
        if (sku==null){
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
        ScSkuActivity scSkuActivtiy= scSkuActivtiyDao.querySCSkuActivityBySCGoodsId(ScSkuActivity.builder()
                        .source(source)
                        .channel(channel)
                        .goodsId(goodsId)
                .build());
        if (scSkuActivtiy==null){
            return null;
        }
        ScSkuActivtiyVO scSkuActivtiyVO=new ScSkuActivtiyVO();
        scSkuActivtiyVO.setGoodsId(goodsId);
        scSkuActivtiyVO.setChannel(channel);
        scSkuActivtiyVO.setSource(source);
        scSkuActivtiyVO.setActivityId(scSkuActivtiy.getActivityId());
        return scSkuActivtiyVO;
    }
    @Override
    public Boolean isWithinRange(String userId,String tagId) {
        RBitSet bitSet = redisService.getBitSet(tagId);
        if(!bitSet.isExists()){
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

}
