package com.swolo.lpy.pysx.http;

import com.swolo.lpy.pysx.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.functions.Func1;

/**
 * http请求管理类
 */
public class HttpManager {
    private static final Object _lock = new Object();
    private static SoftReference<HttpManager> _instance;
    private Retrofit retrofit;
    //    public static final String BASE_URL = "http://120.27.19.17:8089/TasteSource/";
//    public static final String BASE_URL = "https://grainservice.club:8443/nongxinle/";
    public static final String BASE_URL = "http://192.168.0.102:8080/nongxinle_master_war_exploded/";
//    public static final String BASE_URL = "http://192.168.43.163:8080/nongxinle_master_war_exploded/";




    OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            HttpLogger.i("url : " + request.url() + "?" + bodyToString(request));
            return chain.proceed(request);
        }
    }).build();

    private HttpManager() {
        retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(new JsonConverterFactory())
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .build();

    }

    private static String bodyToString(final Request request) {

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final Exception e) {
            return "did not work";
        }
    }

    public static HttpManager getInstance() {

        HttpManager manager = null;
        synchronized (_lock) {
            if (null == _instance || null == _instance.get()) {
                manager = new HttpManager();
                _instance = new SoftReference<HttpManager>(manager);
                manager = null;
            }
            manager = _instance.get();
        }
        return manager;

    }

    public <T> T getApi(Class<T> service) {
        return retrofit.create(service);
    }

    public <T> Observable<T> request(Observable<CommonResponse> observable, TypeToken<T> typeToken) {
        return observable.flatMap(new Func1<CommonResponse, Observable<T>>() {
            @Override
            public Observable<T> call(CommonResponse commonResponse) {
                if (commonResponse == null) {
                    return Observable.error(new ResponseException("网络连接失败！"));
                } else if (commonResponse.code == -1) {
                    return Observable.error(new ResponseException(commonResponse.msg));
                } else {
                    T data = new Gson().fromJson(new Gson().toJson(commonResponse.data), typeToken.getType());
                    return Observable.just(data);
                }
            }
        });
    }

}
