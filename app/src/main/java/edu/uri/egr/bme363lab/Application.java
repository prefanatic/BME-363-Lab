package edu.uri.egr.bme363lab;

import timber.log.Timber;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
    }
}
