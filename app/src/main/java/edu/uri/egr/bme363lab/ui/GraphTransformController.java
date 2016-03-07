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

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.View;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import edu.uri.egr.bme363lab.ui.widget.ReplacingLineChartView;
import timber.log.Timber;

/**
 * Listens to the setOnChartGestureListener from both charts, and handles syncing the viewports together.
 */
public class GraphTransformController {
    public GraphTransformController(ReplacingLineChartView graphOne, ReplacingLineChartView graphTwo) {
        injectControl(graphOne.mChart, graphTwo.mChart);
        injectControl(graphTwo.mChart, graphOne.mChart);
    }

    private void injectControl(LineChart chartToView, LineChart chartToControl) {
        chartToView.setOnChartGestureListener(new OnChartGestureListener() {
            private void sync() {
                Matrix srcMatrix;
                float[] srcVals = new float[9];
                Matrix dstMatrix;
                float[] dstVals = new float[9];

                // Get our source view matrix information.
                srcMatrix = chartToView.getViewPortHandler().getMatrixTouch();
                srcMatrix.getValues(srcVals);

                // Apply this source matrix information to the destination.
                dstMatrix = chartToControl.getViewPortHandler().getMatrixTouch();
                dstMatrix.getValues(dstVals);
                dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X];
                dstVals[Matrix.MSCALE_Y] = srcVals[Matrix.MSCALE_Y];
                dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X];
                dstVals[Matrix.MTRANS_Y] = srcVals[Matrix.MTRANS_Y];
                dstMatrix.setValues(dstVals);

                chartToControl.getViewPortHandler().refresh(dstMatrix, chartToControl, true);
            }

            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {
            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {
            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {
            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                sync();
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {
                sync();
            }
        });
    }
}
