package cn.sweater.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeLockRuleCommandEntity {
    /** 用户ID */
    private String userId;
    /** 活动ID */
    private Long activityId;
    /**拼团id*/
    private String teamId;

}
