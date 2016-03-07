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

package edu.uri.egr.bme363lab.ui;

import android.view.View;

/**
 * Utilities to handle simple animation on views.
 */
public class Animation {
    /**
     * Makes a view animate in by changing the alpha from 0 to 100%
     * @param view View to animate.
     */
    public static void show(View view) {
        if (view.getVisibility() == View.VISIBLE) return;

        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .start();
    }

    /**
     * Makes a view animate out by chaning the alpha from 100 to 0%
     * @param view View to animate.
     */
    public static void hide(View view) {
        if (view.getVisibility() == View.GONE) return;

        view.animate()
                .alpha(0f)
                .withEndAction(() -> view.setVisibility(View.GONE))
                .start();
    }
}
