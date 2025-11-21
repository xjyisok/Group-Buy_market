package cn.sweater.domain.activity.service.discount.impl;

import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.service.discount.AbstractDiscountPreCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service("N")
@Slf4j
public class NDiscountPreCalculate extends AbstractDiscountPreCalculateService {
    @Override
    public BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscountVO) {
        log.info("扣减策略：{}", groupBuyDiscountVO.getDiscountType());
        BigDecimal discountRate=new BigDecimal(groupBuyDiscountVO.getMarketExpr());
        BigDecimal deductedPrice=originalPrice.multiply(discountRate);
        BigDecimal threshPrice=new BigDecimal("0.01");
        if(deductedPrice.compareTo(threshPrice)<=0){
            return threshPrice;
        }else {
            return deductedPrice;
        }
    }
}
