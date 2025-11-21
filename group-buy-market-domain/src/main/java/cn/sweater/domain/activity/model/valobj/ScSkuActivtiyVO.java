package cn.sweater.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScSkuActivtiyVO {
    private String  source;
    private String  channel;
    private Long  activityId;
    private String  goodsId;
}
