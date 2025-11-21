package cn.sweater.domain.activity.service.discount.impl;

import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.service.discount.AbstractDiscountPreCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service("ZJ")
@Slf4j
public class ZJDiscountPreCalculate extends AbstractDiscountPreCalculateService {
    @Override
    public BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscountVO) {
        log.info("扣减策略：{}", groupBuyDiscountVO.getDiscountType());
        String marketExpr=groupBuyDiscountVO.getMarketExpr();
        BigDecimal discountPrice=new BigDecimal(marketExpr);
        BigDecimal deductedPrice=originalPrice.subtract(discountPrice);
        BigDecimal threshPrice=new BigDecimal("0.01");
        if(deductedPrice.compareTo(threshPrice)<=0){
            return threshPrice;
        }else{
            return deductedPrice;
        }
    }
}
