package cn.sweater.domain.trade.service;

import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;

import java.util.Map;

public interface ITradeTaskService {
    Map<String,Integer> execNotifyJob()throws Exception;
    Map<String,Integer> execNotifyJob(String teamId)throws Exception;//NOTE回调用于拼团订单人数满足要求
    Map<String,Integer> execNotifyJob(NotifyTaskEntity notifyTaskEntity)throws Exception;
}
