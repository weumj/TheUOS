package com.uoscs09.theuos2.api;

import android.util.Log;

import com.uoscs09.theuos2.BuildConfig;
import com.uoscs09.theuos2.util.NetworkUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

class ApiValues {
    private static final RxJavaCallAdapterFactory RX_JAVA_CALL_ADAPTER = RxJavaCallAdapterFactory.create();

    static RxJavaCallAdapterFactory rxJavaCallAdapter(){
       return RX_JAVA_CALL_ADAPTER;
    }

    static final class Networks {

        static final Interceptor CHECK_NETWORK = chain -> {
            if (!NetworkUtil.isConnectivityEnable()) {
                throw new IOException("Failed to access current network.");
            }
            return chain.proceed(chain.request());
        };

        static final Interceptor LOG_INTERCEPTOR = chain -> {
            if (BuildConfig.DEBUG) {
                Request request = chain.request();
                long t1 = System.nanoTime();
                Log.i("network", String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()));

                Response response = chain.proceed(request);

                long t2 = System.nanoTime();
                Log.i("network", String.format("Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6d, response.headers()));

                return response;
            } else {
                return chain.proceed(chain.request());
            }
        };

    }

}
