package cn.sweater.trigger.http;

import cn.sweater.api.IDCCService;
import cn.sweater.api.response.Response;
import cn.sweater.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/dcc/")
public class DCCServiceController implements IDCCService {
    @Resource
    private RTopic dccTopic;
    @RequestMapping(value = "update_config",method = RequestMethod.GET)
    @Override
    public Response<Boolean> updateConfig(@RequestParam String key, @RequestParam String value) {
        try{
            dccTopic.publish(key+","+value);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
            .build();
        }catch (Exception e){
            log.error(e.getMessage());
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
