package cn.sweater.domain.activity.adapter.repository;

import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.ScSkuActivtiyVO;
import cn.sweater.domain.activity.model.valobj.SkuVO;
import cn.sweater.domain.activity.model.valobj.TeamStatisticVO;
import cn.sweater.domain.trade.model.entity.GroupBuyActivityEntity;

import java.util.List;

public interface IActivityRepository {
    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId);

    SkuVO querySkuByGoodsId(String goodsId);

    ScSkuActivtiyVO queryScSkuActivtiyByScGoodsId(String source, String channel, String goodsId);
    public Boolean isWithinRange(String userId,String tagId);

    boolean isDowngradeSwitch();

    boolean isCutRange(String userId);

    List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByOwner(Long activityId, String userId, int userSelfGroupNo);

    List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailListByRandom(Long activityId, String userId, int userSelfGroupNo);

    TeamStatisticVO quertTeamStatisticByActivtiyId(Long activityId);
}
