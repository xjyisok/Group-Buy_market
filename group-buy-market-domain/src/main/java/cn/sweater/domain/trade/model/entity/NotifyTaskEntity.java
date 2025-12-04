package cn.sweater.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotifyTaskEntity {
    /** 拼单组队ID */
    private String teamId;
    /** 回调接口 */
    private String notifyUrl;
    /** 回调配置 */
    private String notifyType;
    /** 回调配置 */
    private String notifyMQ;
    /** 回调次数 */
    private Integer notifyCount;
    /** 回调状态【0初始、1完成、2重试、3失败】 */
    private Integer notifyStatus;
    /** 参数对象 */
    private String parameterJson;

    public String lockKey() {
        return "notify_job_lock_key_" + this.teamId;
    }


}
