package edu.uri.egr.bme363lab;

import timber.log.Timber;

public class Application extends android.app.Application {

    /**
     * onCreate
     * This runs when the Application is first starting.  Before any Activities or anything else show up.
     * Right now we "plant" a "debug tree" in order to see logging in our ADB logcat.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // If we're in DEBUG mode.  This flag is set based on the buildType set on compile.
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
    }
}
