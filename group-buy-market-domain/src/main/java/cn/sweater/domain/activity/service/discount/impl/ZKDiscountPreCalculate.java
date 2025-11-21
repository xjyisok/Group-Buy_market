package cn.sweater.domain.activity.service.discount.impl;

import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.service.discount.AbstractDiscountPreCalculateService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service("ZK")
public class ZKDiscountPreCalculate extends AbstractDiscountPreCalculateService {
    @Override
    public BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscountVO) {
        return null;
    }
}
