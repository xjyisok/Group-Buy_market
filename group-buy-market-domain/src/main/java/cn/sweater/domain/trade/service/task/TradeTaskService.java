package cn.sweater.domain.trade.service.task;

import cn.sweater.domain.trade.adapter.port.ITradePort;
import cn.sweater.domain.trade.adapter.repository.ITradeRepository;
import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import cn.sweater.domain.trade.service.ITradeTaskService;
import cn.sweater.types.enums.NotifyTaskHttpEnumVO;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@Slf4j
public class TradeTaskService implements ITradeTaskService {
    @Resource
    private ITradeRepository tradeRepository;
    @Resource
    private ITradePort tradePort;
    @Override
    public Map<String, Integer> execNotifyJob() throws Exception {
        log.info("拼团交易-执行结算通知任务");

        // 查询未执行任务
        List<NotifyTaskEntity> notifyTaskEntityList = tradeRepository.queryUnExecutedNotifyTaskList();
        //System.out.println(notifyTaskEntityList.size());
        return getStringIntegerMap(notifyTaskEntityList);
    }

    private Map<String, Integer> getStringIntegerMap(List<NotifyTaskEntity> notifyTaskEntityList) throws Exception {
        int successCount = 0, errorCount = 0, retryCount = 0;
        for (NotifyTaskEntity notifyTask : notifyTaskEntityList) {
            // 回调处理 success 成功，error 失败
            String response = tradePort.groupBuyNotify(notifyTask);

            // 更新状态判断&变更数据库表回调任务状态
            if (NotifyTaskHttpEnumVO.SUCCESS.getCode().equals(response)) {
                int updateCount = tradeRepository.updateNotifyTaskStatusSuccess(notifyTask.getTeamId());
                if (1 == updateCount) {
                    successCount += 1;
                }
            } else if (NotifyTaskHttpEnumVO.ERROR.getCode().equals(response)) {
                if (notifyTask.getNotifyCount() < 5) {
                    int updateCount = tradeRepository.updateNotifyTaskStatusError(notifyTask.getTeamId());
                    if (1 == updateCount) {
                        errorCount += 1;
                    }
                } else {
                    int updateCount = tradeRepository.updateNotifyTaskStatusRetry(notifyTask.getTeamId());
                    if (1 == updateCount) {
                        retryCount += 1;
                    }
                }
            }
        }

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("waitCount", notifyTaskEntityList.size());
        resultMap.put("successCount", successCount);
        resultMap.put("errorCount", errorCount);
        resultMap.put("retryCount", retryCount);

        return resultMap;
    }

    @Override
    public Map<String, Integer> execNotifyJob(NotifyTaskEntity notifyTaskEntity) throws Exception {
        log.info("拼团交易-执行回调通知回调，指定 teamId:{} notifyTaskEntity:{}", notifyTaskEntity.getTeamId(), JSON.toJSONString(notifyTaskEntity));
        return getStringIntegerMap(Collections.singletonList(notifyTaskEntity));

    }

    @Override
    public Map<String, Integer> execNotifyJob(String teamId) throws Exception {
        log.info("拼团交易-执行结算通知任务teamId:{}", teamId);
        List<NotifyTaskEntity> notifyTaskEntityList = tradeRepository.queryUnExecutedNotifyTaskList(teamId);
        return getStringIntegerMap(notifyTaskEntityList);
    }
}
