package cn.sweater.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public enum NotifyTypeEnumVO {
    HTTP("HTTP", "HTTP 回调"),
    MQ("MQ", "MQ 消息通知"),
    ;

    private String code;
    private String info;


}
