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

package edu.uri.egr.bme363lab.ui.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import edu.uri.egr.bme363lab.R;

public class ReplacingLineChartView extends FrameLayout {
    private int COLOR_ACCENT;

    private LineChart mChart;

    private int mXInsertEntry = 0;
    private int mForwardRemoveSize = 0;
    private int mMaximumX = 100;

    public ReplacingLineChartView(Context context) {
        this(context, null);
    }

    public ReplacingLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReplacingLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        COLOR_ACCENT = ContextCompat.getColor(getContext(), R.color.colorAccent);
        mChart = new LineChart(getContext());

        LineData data = new LineData();
        LineDataSet set = createDataSet();

        data.addDataSet(set);
        mChart.setData(data);
        mChart.getLegend().setEnabled(false);
        mChart.setDescription("");
        mChart.setDrawGridBackground(false);
        mChart.getAxisLeft().setAxisMaxValue(255);
        mChart.getAxisRight().setEnabled(false);
        mChart.getXAxis().setDrawLabels(false);

        addView(mChart, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private LineDataSet createDataSet() {
        LineDataSet set = new LineDataSet(null, "Value");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setColor(COLOR_ACCENT);

        return set;
    }

    public void addEntry(int val) {
        LineData data = mChart.getData();

        data.addEntry(new Entry(val, mXInsertEntry), 0);

        // If we hit our maximum cap, we need to do some magic.
        if (mXInsertEntry == mMaximumX) {
            // Remove the first value in our entry.
            data.getDataSetByIndex(0).removeFirst();


            // Shift all entries down one.
            for (int i = 0; i < data.getDataSetByIndex(0).getYVals().size(); i++)
                data.getDataSetByIndex(0).getYVals().get(i).setXIndex(i - 1);

        } else {
            mXInsertEntry++;
        }

        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    public void setMaximumX(int xMax) {
        mMaximumX = xMax;

        for (int i = 0; i < xMax; i++) {
            mChart.getData().addXValue(String.valueOf(i));
        }
    }

    public void setForwardRemoveSize(int forwardRemoveSize) {
        mForwardRemoveSize = forwardRemoveSize;
    }
}
