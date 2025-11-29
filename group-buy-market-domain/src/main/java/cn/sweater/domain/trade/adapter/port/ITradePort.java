package cn.sweater.domain.trade.adapter.port;

import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;

public interface ITradePort {
    String groupBuyNotify(NotifyTaskEntity notifyTask)throws Exception;
}
