package cn.sweater.infrastructure.dcc;


import cn.bugstack.wrench.dynamic.config.center.types.annotations.DCCValue;
import cn.sweater.types.common.Constants;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DCCService {
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;
    @DCCValue("cutRange:100")
    private String cutRange;
    @DCCValue("scBlackList:s02c02")
    private String scBlackList;
    @DCCValue("cacheOpenSwitch:0")
    private String cacheOpenSwitch;
    public Boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }
    public Boolean isCutRange(String userId) {
        int hashCode = userId.hashCode();
        int lastTwoDigits = hashCode % 100;
        return lastTwoDigits<=Integer.parseInt(cutRange);
    }
    public Boolean isScBlackList(String source,String channel) {
        List<String> blackList = Arrays.asList(scBlackList.split(Constants.SPLIT));
        return blackList.contains(source+channel);
    }
    public Boolean isCacheOpenSwitch() {
        return "0".equals(cacheOpenSwitch);
    }
}
