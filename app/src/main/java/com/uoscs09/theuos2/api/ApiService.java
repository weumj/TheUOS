package com.uoscs09.theuos2.api;

import com.uoscs09.theuos2.util.NetworkUtil;

import java.io.IOException;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class ApiService {
    private static final String URL_UOS = "http://www.uos.ac.kr/";

    public static final String URL_SCHOLARSHIP = AnnounceApi.URL_SCHOLARSHIP;

    public static final String URL_SEATS = LibraryApi.SEATS_URL;

    public static final String URL_REST_WEEK = RestaurantApi.WEEK_URL;

    private static UosOApi uosOApi;

    public static UosOApi oApi() {
        if (uosOApi == null) {
            uosOApi = new Retrofit.Builder()
                    .addCallAdapterFactory(TaskCallAdapterFactory.create())
                    .addConverterFactory(ConverterFactory.create())
                    .baseUrl(UosOApi.URL)
                    .client(okHttpClient())
                    .build()
                    .create(UosOApi.class);
        }

        return uosOApi;
    }

    private static LibraryApi libraryApi;

    public static LibraryApi libraryApi() {
        if (libraryApi == null)
            libraryApi = new Retrofit.Builder()
                    .baseUrl(LibraryApi.BOOK_URL)
                    .addCallAdapterFactory(TaskCallAdapterFactory.create())
                    .addConverterFactory(ConverterFactory.create())
                    .client(okHttpClient())
                    .build()
                    .create(LibraryApi.class);

        return libraryApi;
    }

    private static AnnounceApi announceApi;

    public static AnnounceApi announceApi() {
        if (announceApi == null)
            announceApi = new Retrofit.Builder()
                    .baseUrl(URL_UOS)
                    .addCallAdapterFactory(TaskCallAdapterFactory.create())
                    .addConverterFactory(ConverterFactory.create())
                    .client(okHttpClient())
                    .build()
                    .create(AnnounceApi.class);

        return announceApi;
    }

    private static RestaurantApi restaurantApi;

    public static RestaurantApi restaurantApi(){
        if (restaurantApi == null)
            restaurantApi = new Retrofit.Builder()
                    .baseUrl(RestaurantApi.URL)
                    .addCallAdapterFactory(TaskCallAdapterFactory.create())
                    .addConverterFactory(ConverterFactory.create())
                    .client(okHttpClient())
                    .build()
                    .create(RestaurantApi.class);

        return restaurantApi;
    }


    private static OkHttpClient okHttpClient;

    private static OkHttpClient okHttpClient() {

        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        if (!NetworkUtil.isConnectivityEnable()) {
                            throw new IOException("Failed to access current network.");
                        }

                        return chain.proceed(chain.request());
                    })
                    .build();
        }

        return okHttpClient;
    }
}
