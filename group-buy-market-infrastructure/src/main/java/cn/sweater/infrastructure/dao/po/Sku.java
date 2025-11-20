package cn.sweater.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sku {
   private String  id;
   private String  source;
   private String  channel;
   private String  goodsId;
   private String  goodsName;
   private BigDecimal originalPrice;
   private Date createTime;
   private Date  updateTime;
}
