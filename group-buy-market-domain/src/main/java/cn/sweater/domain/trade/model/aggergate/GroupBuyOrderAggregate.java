package cn.sweater.domain.trade.model.aggergate;

import cn.sweater.domain.trade.model.entity.PayActivityEntity;
import cn.sweater.domain.trade.model.entity.PayDiscountEntity;
import cn.sweater.domain.trade.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupBuyOrderAggregate {
    /** 用户实体对象 */
    private UserEntity userEntity;
    /** 支付活动实体对象 */
    private PayActivityEntity payActivityEntity;
    /** 支付优惠实体对象 */
    private PayDiscountEntity payDiscountEntity;
    /** 用户拼团次数 */
    private Integer userTakeOrderCount;

}
