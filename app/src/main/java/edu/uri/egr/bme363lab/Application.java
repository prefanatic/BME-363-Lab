/*
 * Copyright 2015 Cody Goldberg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
