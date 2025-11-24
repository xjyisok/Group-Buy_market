package cn.sweater.domain.activity.adapter.repository;

import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.ScSkuActivtiyVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;

public interface IActivityRepository {
    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId);

    SkuVO querySkuByGoodsId(String goodsId);

    ScSkuActivtiyVO queryScSkuActivtiyByScGoodsId(String source, String channel, String goodsId);
    public Boolean isWithinRange(String userId,String tagId);

    boolean isDowngradeSwitch();

    boolean isCutRange(String userId);
}
