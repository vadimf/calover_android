package com.varteq.catslovers;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.StoringMechanism;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.utils.ChatHelper;
import com.varteq.catslovers.utils.Log;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.StorageUtils;
import com.varteq.catslovers.utils.qb.CognitoAuthHelper;

import net.gotev.uploadservice.Logger;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class AppController extends Application {//extends MultiDexApplication {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    static final String APP_ID = "66489";//"65691";
    static final String AUTH_KEY = "XsVALhPAEAC3k7n";//"hSnYYnZ9Y27YZyx";
    static final String AUTH_SECRET = "eBQa5HzyyWZA9Hx";//"L2dJuWtC8LDCKQ6";
    static final String ACCOUNT_KEY = "QUhvHK4RN_Hzz4iEnS9k";//"GmQhUChypQHbecmradXr";
    public static final String USER_PASS = "xfzMLA6YFn673sN9";
    private static Context instance;

    public static Context getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CognitoAuthHelper.init(getApplicationContext());
        ServiceGenerator.setToken(Profile.getAuthToken(this));

        configureCrashReporting();

        // Init Quick Block
        QBSettings.getInstance().setStoringMehanism(StoringMechanism.UNSECURED);
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);

        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.HTTP_STACK = new OkHttpStack(getOkHttpClient());

        String TAG = "MultipartLogger";
        /*Logger.setLoggerDelegate(new Logger.LoggerDelegate() {

            @Override
            public void error(String tag, String message) {
                Log.e(TAG, message);
            }

            @Override
            public void error(String tag, String message, Throwable exception) {
                Log.e(TAG, message);
            }

            @Override
            public void debug(String tag, String message) {
                Log.d(TAG, message);
            }

            @Override
            public void info(String tag, String message) {
                Log.i(TAG, message);
            }
        });*/
        Logger.setLogLevel(Logger.LogLevel.DEBUG);

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);
        Realm myRealm = Realm.getDefaultInstance();

        clearImagePickerDirectory();
        ChatHelper.getInstance().loginToQuickBlox(this);
    }

    private void clearImagePickerDirectory() {
        File imagePickerDirectory = StorageUtils.getImagePickerDirectoryFile();
        StorageUtils.clearDirectory(imagePickerDirectory);
    }

    private void configureCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)

                // you can add your own request interceptors to add authorization headers.
                // do not modify the body or the http method here, as they are set and managed
                // internally by Upload Service, and tinkering with them will result in strange,
                // erroneous and unpredicted behaviors
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request.Builder request = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + ServiceGenerator.token);

                        return chain.proceed(request.build());
                    }
                })

                // open up your Chrome and go to: chrome://inspect
                //.addNetworkInterceptor(new StethoInterceptor())

                // if you use HttpLoggingInterceptor, be sure to put it always as the last interceptor
                // in the chain and to not use BODY level logging, otherwise you will get all your
                // file contents in the log. Logging body is suitable only for small requests.
                .addInterceptor(new HttpLoggingInterceptor(message -> Log.d("OkHttp multipart", message))
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .cache(null)
                .build();
    }

    /*protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }*/
}
