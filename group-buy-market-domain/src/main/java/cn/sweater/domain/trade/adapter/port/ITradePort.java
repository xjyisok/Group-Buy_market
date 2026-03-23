package cn.sweater.domain.trade.adapter.port;

import cn.sweater.domain.trade.model.entity.NotifyTaskEntity;
import cn.sweater.domain.trade.model.valobj.TradeOrderStatusEnumVO;

public interface ITradePort {
    String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception;

    /**
     * 关闭第三方支付订单，阻止临界窗口内的用户付款
     * @param outTradeNo 外部交易单号
     * @return true=关单成功(订单未支付), false=关单失败(订单已支付)
     */
    boolean closePayOrder(String outTradeNo) throws Exception;
}
