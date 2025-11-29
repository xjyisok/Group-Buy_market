package cn.sweater.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum NotifyTaskHttpEnumVO {
    SUCCESS("success","成功"),
    ERROR("error","失败"),
    NULL("null","未抢占到任务空执行");
    private String code;
    private String info;
}
