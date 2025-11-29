package cn.sweater.infrastructure.dao;

import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import cn.sweater.infrastructure.dao.po.NotifyTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 回调任务
 * @create 2025-01-26 18:23
 */
@Mapper
public interface INotifyTaskDao {

    void insert(NotifyTask notifyTask);

    List<NotifyTask> queryUnExecutedNotifyTaskList();
    List<NotifyTask> queryUnExecutedNotifyTaskList(String teamId);
    int updateNotifyTaskStatusSuccess(String teamId);

    int updateNotifyTaskStatusRetry(String teamId);

    int updateNotifyTaskStatusError(String teamId);
}
