package edu.uri.egr.bme363lab;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class ReplacingLineChartView extends FrameLayout {
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
        mChart = new LineChart(getContext());

        LineData data = new LineData();
        LineDataSet set = createDataSet();

        data.addDataSet(set);
        mChart.setData(data);
        mChart.getAxisLeft().setAxisMaxValue(255);

        addView(mChart, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private LineDataSet createDataSet() {
        LineDataSet set = new LineDataSet(null, "Value");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawValues(false);

        return set;
    }

    public void addEntry(int val) {
        LineData data = mChart.getData();

        data.addEntry(new Entry(val, mXInsertEntry), 0);

        // If we hit our maximum cap, we need to do some magic.
        if (mXInsertEntry == mMaximumX) {
            // Remove the first value in our entry.
            data.removeEntry(0, 0);

            // Shift all entries down one.
            for (Entry entry : data.getDataSetByIndex(0).getYVals()) {
                entry.setXIndex(entry.getXIndex() - 1);
            }
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
