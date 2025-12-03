package cn.sweater.infrastructure.gateway;

import cn.sweater.types.enums.ResponseCode;
import cn.sweater.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;



@Slf4j
@Service
public class GroupBuyNotifyService {
    @Resource
    private OkHttpClient okHttpClient;
    public String GroupBuyNotify(String apiUrl,String notifyRequestDTOJson)throws Exception{
        try {
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, notifyRequestDTOJson);
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .addHeader("content-type", "application/json")
                    .build();

            Response response = okHttpClient.newCall(request).execute();//NOTE这里是对小型支付商城的回调
            return response.body().string();
        }catch (Exception e) {
            throw new AppException(ResponseCode.HTTP_EXCEPTION.getCode(),ResponseCode.HTTP_EXCEPTION.getInfo());
        }
    }
}
