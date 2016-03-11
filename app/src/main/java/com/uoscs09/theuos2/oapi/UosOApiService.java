package com.uoscs09.theuos2.oapi;

import com.uoscs09.theuos2.util.NetworkUtil;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class UosOApiService {

    private static UosOApi uosOApi;

    public static UosOApi api() {
        if (uosOApi == null) {
            uosOApi = new Retrofit.Builder()
                    .addCallAdapterFactory(TaskCallAdapterFactory.create())
                    .addConverterFactory(XmlConverterFactory.create())
                    .baseUrl(UosOApi.URL)
                    .client(okHttpClient())
                    .build()
                    .create(UosOApi.class);
        }

        return uosOApi;
    }


    private static OkHttpClient okHttpClient() {

        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    if (!NetworkUtil.isConnectivityEnable()) {
                        throw new IOException("Failed to access current network.");
                    }

                    return chain.proceed(chain.request());
                })
                .build();
    }
}
