package cn.sweater.infrastructure.dcc;

import cn.sweater.types.annotations.DCCValue;
import org.springframework.stereotype.Service;

@Service
public class DCCService {
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;
    @DCCValue("cutRange:100")
    private String cutRange;
    public Boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }
    public Boolean isCutRange(String userId) {
        int hashCode = userId.hashCode();
        int lastTwoDigits = hashCode % 100;
        return lastTwoDigits<=Integer.parseInt(cutRange);
    }
}
