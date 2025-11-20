package cn.sweater.domain.activity.adapter.repository;

import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;

public interface IActivityRepository {
    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(String source, String channel);

    SkuVO querySkuByGoodsId(String goodsId);

}
