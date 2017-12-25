package com.varteq.catslovers;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.StoringMechanism;
import com.varteq.catslovers.api.ServiceGenerator;
import com.varteq.catslovers.utils.Profile;
import com.varteq.catslovers.utils.qb.CognitoAuthHelper;

import io.fabric.sdk.android.Fabric;

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
    }

    private void configureCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    /*protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }*/
}
