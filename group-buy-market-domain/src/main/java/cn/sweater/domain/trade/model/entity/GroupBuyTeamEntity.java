package cn.sweater.domain.trade.model.entity;

import cn.sweater.domain.trade.model.valobj.NotifyConfigVO;
import cn.sweater.types.enums.GroupBuyOrderEnumVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyTeamEntity {

    /** 拼单组队ID */
    private String teamId;
    /** 活动ID */
    private Long activityId;
    /** 目标数量 */
    private Integer targetCount;
    /** 完成数量 */
    private Integer completeCount;
    /** 锁单数量 */
    private Integer lockCount;
    /**拼团开始有效时间*/
    private Date validStartTime;
    /**拼团结束有效时间*/
    private Date validEndTime;
    /** 状态（0-拼单中、1-完成、2-失败） */
    private GroupBuyOrderEnumVO status;
    /**回调配置*/
    private NotifyConfigVO notifyConfig;
}
