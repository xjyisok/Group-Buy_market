package cn.sweater.domain.activity.service.trial;

import cn.sweater.domain.activity.model.entity.MarketProductEntity;
import cn.sweater.domain.activity.model.entity.TrialBalanceEntity;

public interface IIndexGroupBuyMarketService {
    TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity)throws Exception;
}
