package com.varteq.catslovers.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    private static final int CONNECT_TIMEOUT = 15;
    private static final int WRITE_TIMEOUT = 60;
    private static final int TIMEOUT = 60;

    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .addInterceptor(logging);

    /*
    static {
        CLIENT.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS);
        CLIENT.setWriteTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);
        CLIENT.setReadTimeout(TIMEOUT, TimeUnit.SECONDS);
    }*/

    private static String apiBaseUrl = "http://catslovers.clients.in.ua/";

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(apiBaseUrl)
                    .client(httpClient.build());

    private static Retrofit retrofit = builder.build();
    private static ApiService apiService = retrofit.create(ApiService.class);

    public static void changeApiBaseUrl(String newApiBaseUrl) {
        apiBaseUrl = newApiBaseUrl;

        builder = new Retrofit.Builder()
                .baseUrl(apiBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build());
    }

    public static <S> S createService(Class<S> serviceClass) {
        return createService(serviceClass);
    }

    public static Retrofit retrofit() {
        return retrofit;
    }

    public static ApiService getApiService() {
        return apiService;
    }

    /*public static <S> S createService(Class<S> serviceClass, AccessToken token) {
        String authToken = token.getTokenType().concat(token.getAccessToken());
        return createService(serviceClass, authToken);
    }*/
}

