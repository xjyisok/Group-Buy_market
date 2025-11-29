package cn.sweater.domain.activity.service.trial;

import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.sweater.domain.activity.model.valobj.TeamStatisticVO;

import java.util.List;

public interface IIndexGroupBuyMarketService {
    TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity)throws Exception;

    List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailList(Long activityId, String userId, int i, int i1);

    TeamStatisticVO quertTeamStatisticByActivtiyId(Long activityId);
}
