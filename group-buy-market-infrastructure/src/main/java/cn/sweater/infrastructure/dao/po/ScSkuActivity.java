package cn.sweater.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScSkuActivity {
    private String  id;
    private String  source;
    private String  channel;
    private Long  activityId;
    private String  goodsId;
    private Date createTime;
    private Date  updateTime;
}
