package com.uoscs09.theuos2.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class UosApiService {
    private static final String URL_UOS = "http://www.uos.ac.kr/";

    public static final String URL_M_SCHOLARSHIP = AnnounceApi.URL_M_SCHOLARSHIP;
    public static final String URL_SCHOLARSHIP = AnnounceApi.URL_SCHOLARSHIP;
    public static final String URL_M_ANNOUNCE= AnnounceApi.URL_M_ANNOUNCE;

    public static final String URL_SEATS = LibraryApi.SEATS_URL;

    public static final String URL_REST_WEEK = RestaurantApi.WEEK_URL;

    private static UosOApi uosOApi;

    public static UosOApi oApi() {
        if (uosOApi == null) {
            uosOApi = new Retrofit.Builder()
                    .baseUrl(UosOApi.URL)
                    .addCallAdapterFactory(TaskCallAdapterFactory.getInstance())
                    .addConverterFactory(ConverterFactory.getInstance())
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
                    .addCallAdapterFactory(TaskCallAdapterFactory.getInstance())
                    .addConverterFactory(ConverterFactory.getInstance())
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
                    .addCallAdapterFactory(TaskCallAdapterFactory.getInstance())
                    .addConverterFactory(ConverterFactory.getInstance())
                    .client(okHttpClient())
                    .build()
                    .create(AnnounceApi.class);

        return announceApi;
    }

    private static RestaurantApi restaurantApi;

    public static RestaurantApi restaurantApi() {
        if (restaurantApi == null)
            restaurantApi = new Retrofit.Builder()
                    .baseUrl(RestaurantApi.URL)
                    .addCallAdapterFactory(TaskCallAdapterFactory.getInstance())
                    .addConverterFactory(ConverterFactory.getInstance())
                    .client(okHttpClient())
                    .build()
                    .create(RestaurantApi.class);

        return restaurantApi;
    }


    private static OkHttpClient okHttpClient;

    private static OkHttpClient okHttpClient() {

        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(ApiValues.Networks.CHECK_NETWORK)
                    .addInterceptor(ApiValues.Networks.LOG_INTERCEPTOR)
                    .build();
        }

        return okHttpClient;
    }
}
