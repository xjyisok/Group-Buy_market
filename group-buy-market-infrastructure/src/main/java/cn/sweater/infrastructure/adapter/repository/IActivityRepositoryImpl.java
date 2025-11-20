package cn.sweater.infrastructure.adapter.repository;

import cn.sweater.domain.activity.adapter.repository.IActivityRepository;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;
import cn.sweater.infrastructure.dao.IGroupBuyActivityDao;
import cn.sweater.infrastructure.dao.IGroupBuyDiscountDao;
import cn.sweater.infrastructure.dao.ISkuDao;
import cn.sweater.infrastructure.dao.po.GroupBuyActivity;
import cn.sweater.infrastructure.dao.po.GroupBuyDiscount;
import cn.sweater.infrastructure.dao.po.Sku;
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
    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(String source, String channel) {
        GroupBuyActivity groupBuyActivityRes =new GroupBuyActivity();
        groupBuyActivityRes.setSource(source);
        groupBuyActivityRes.setChannel(channel);
        GroupBuyActivity groupBuyActivity=groupBuyActivityDao.queryValidGroupBuyActivity(groupBuyActivityRes);
        String disCountId=groupBuyActivity.getDiscountId();
        GroupBuyDiscount groupBuyDiscount =groupBuyDiscountDao.queryGroupBuyActivityDiscountByDiscountId(disCountId);
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
                .activityId(groupBuyActivityRes.getActivityId())
                .activityName(groupBuyActivityRes.getActivityName())
                .source(groupBuyActivityRes.getSource())
                .channel(groupBuyActivityRes.getChannel())
                .goodsId(groupBuyActivityRes.getGoodsId())
                .groupBuyDiscount(groupBuyDiscountVG)
                .groupType(groupBuyActivityRes.getGroupType())
                .takeLimitCount(groupBuyActivityRes.getTakeLimitCount())
                .target(groupBuyActivityRes.getTarget())
                .validTime(groupBuyActivityRes.getValidTime())
                .status(groupBuyActivityRes.getStatus())
                .startTime(groupBuyActivityRes.getStartTime())
                .endTime(groupBuyActivityRes.getEndTime())
                .tagId(groupBuyActivityRes.getTagId())
                .tagScope(groupBuyActivityRes.getTagScope())
                .build();

    }

    @Override
    public SkuVO querySkuByGoodsId(String goodsId) {
        Sku sku = skuDao.querySkuByGoodsId(goodsId);
        return SkuVO.builder()
                .goodsId(sku.getGoodsId())
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .build();

    }
}
