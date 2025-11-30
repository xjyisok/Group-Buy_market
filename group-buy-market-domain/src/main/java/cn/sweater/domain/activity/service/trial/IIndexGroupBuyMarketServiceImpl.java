package cn.sweater.domain.activity.service.trial;

import cn.sweater.domain.activity.adapter.repository.IActivityRepository;
import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;
import cn.sweater.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import cn.sweater.domain.activity.model.valobj.TeamStatisticVO;
import cn.sweater.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import cn.sweater.types.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class IIndexGroupBuyMarketServiceImpl implements IIndexGroupBuyMarketService {
    @Resource
    DefaultActivityStrategyFactory activityStrategyFactory;
    @Resource
    IActivityRepository activityRepository;
    @Override
    public TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception {
        StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity>strategyHandler
                =activityStrategyFactory.strategyHandler();
        TrialBalanceEntity trialBalanceEntity=strategyHandler.apply(marketProductEntity,new DefaultActivityStrategyFactory.DynamicContext());
        return trialBalanceEntity;
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailList(Long activityId, String userId, int userSelfGroupNo, int otherGroupNo) {
        List<UserGroupBuyOrderDetailEntity>unionAllTeam=new ArrayList<>();
        if(userSelfGroupNo!=0){
            List<UserGroupBuyOrderDetailEntity>userSelfList=activityRepository.queryInProgressUserGroupBuyOrderDetailListByOwner(activityId,userId,userSelfGroupNo);
            //System.out.println(JSON.toJSONString(userSelfList));
            if(null!=userSelfList && !userSelfList.isEmpty()) {
                unionAllTeam.addAll(userSelfList);
            }
        }
        if(otherGroupNo!=0){
            List<UserGroupBuyOrderDetailEntity>randomTeamList=activityRepository.queryInProgressUserGroupBuyOrderDetailListByRandom(unionAllTeam,activityId,userId,otherGroupNo);
            //System.out.println(JSON.toJSONString(randomTeamList));
            if(null!=randomTeamList && !randomTeamList.isEmpty()) {
                unionAllTeam.addAll(randomTeamList);
            }
        }
        return unionAllTeam;
    }

    @Override
    public TeamStatisticVO quertTeamStatisticByActivtiyId(Long activityId) {
        return activityRepository.quertTeamStatisticByActivtiyId(activityId);
    }
}
