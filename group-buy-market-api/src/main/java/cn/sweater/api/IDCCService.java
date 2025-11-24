package cn.sweater.api;

import cn.sweater.api.response.Response;

public interface IDCCService {
    public Response<Boolean>updateConfig(String key,String value);
}
