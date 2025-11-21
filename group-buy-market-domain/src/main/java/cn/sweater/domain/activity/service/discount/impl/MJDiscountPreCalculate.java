package cn.sweater.domain.activity.service.discount.impl;

import cn.sweater.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import cn.sweater.domain.activity.service.discount.AbstractDiscountPreCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("MJ")
@Slf4j
public class MJDiscountPreCalculate extends AbstractDiscountPreCalculateService {
    @Override
    public BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscountVO) {
        log.info("扣减策略：{}", groupBuyDiscountVO.getDiscountType());
        String marketExpr=groupBuyDiscountVO.getMarketExpr();
        String[] marketExprs=marketExpr.split(",");
        BigDecimal requiredPrice=new BigDecimal(marketExprs[0]);
        BigDecimal discountPrice=new BigDecimal(marketExprs[1]);
        if(originalPrice.compareTo(requiredPrice)<0){
            return originalPrice;
        }else{
            BigDecimal deductedPrice=originalPrice.subtract(discountPrice);
            if (deductedPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return new BigDecimal("0.01");
            }
            return deductedPrice;
        }
    }
}
