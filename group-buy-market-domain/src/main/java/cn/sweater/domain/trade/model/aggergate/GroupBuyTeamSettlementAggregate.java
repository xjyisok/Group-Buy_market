package cn.sweater.domain.trade.model.aggergate;

import cn.sweater.domain.trade.model.entity.GroupBuyTeamEntity;
import cn.sweater.domain.trade.model.entity.TradePaySuccessEntity;
import cn.sweater.domain.trade.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupBuyTeamSettlementAggregate {
    private UserEntity userEntity;
    private GroupBuyTeamEntity groupBuyTeamEntity;
    private TradePaySuccessEntity tradePaySuccessEntity;
}
