package com.varteq.catslovers;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

public class AppController extends Application {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
