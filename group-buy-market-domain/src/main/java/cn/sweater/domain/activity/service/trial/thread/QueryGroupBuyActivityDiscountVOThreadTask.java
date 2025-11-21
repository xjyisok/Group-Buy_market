package cn.sweater.domain.activity.service.trial.thread;

import cn.sweater.domain.activity.adapter.repository.IActivityRepository;
import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.model.valobj.ScSkuActivtiyVO;

import java.util.concurrent.Callable;

public class QueryGroupBuyActivityDiscountVOThreadTask implements Callable<GroupBuyActivityDiscountVO> {
    private final String source;
    private final String channel;
    private final String goodsId;
    private final IActivityRepository activityRepository;

    public QueryGroupBuyActivityDiscountVOThreadTask(String source, String channel, String goodsId, IActivityRepository activityRepository) {
        this.source = source;
        this.channel = channel;
        this.goodsId = goodsId;
        this.activityRepository = activityRepository;
    }
    @Override
    public GroupBuyActivityDiscountVO call() throws Exception {
        ScSkuActivtiyVO scSkuActivtiyVO = activityRepository.queryScSkuActivtiyByScGoodsId(source,channel,goodsId);
        if (scSkuActivtiyVO == null) {
            return null;
        }
        return activityRepository.queryGroupBuyActivityDiscountVO(scSkuActivtiyVO.getActivityId());
    }
}
