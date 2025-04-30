package com.swolo.lpy.pysx.http;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * Created by Administrator on 2016/9/8 0008.
 */

public class JsonResponseBodyConverter implements Converter<ResponseBody, CommonResponse> {
    @Override
    public CommonResponse convert(ResponseBody value) throws IOException {
        CommonResponse commonResponse = new CommonResponse();
        String response = value.string();
        Log.d("JsonResponseBodyConverter", "原始后端返回内容: " + response);
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("data")) {
                commonResponse.data = jsonObject.get("data");
            } else {
                commonResponse.data = null;
            }
            commonResponse.code = jsonObject.optInt("code");
            commonResponse.msg = jsonObject.optString("msg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return commonResponse;
    }
}
