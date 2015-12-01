package edu.uri.egr.bme363lab;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.line_chart_original) ReplacingLineChartView mChartOriginal;
    @Bind(R.id.line_chart_transformed) ReplacingLineChartView mChartTransformed;

    private BluetoothSocket mSocket;
    private int currentFunction;
    private int byteReadFlag = -1;
    private volatile int graphValueToAdd;
    private boolean skipTriggerOriginal = false;
    private boolean skipTriggerTransformed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mChartOriginal.setMaximumX(1024);
        mChartTransformed.setMaximumX(1024);
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        DeviceListDialog dialog = new DeviceListDialog();
        dialog.getDevice().subscribe(this::deviceSelected);
        dialog.show(getFragmentManager(), "deviceList");
    }

    private void switchFunction(int function) {
        currentFunction = function;

        Timber.d("Switching to function %d.", function);
        runOnUiThread(() -> {
                    switch (function) {
                        case 0:
                            setTitle("Binary Counter");
                            break;
                        case 1:
                            setTitle("ECG Simulation");
                            break;
                        case 2:
                            setTitle("Echo (A/D - D/A)");
                            break;
                        case 3:
                            setTitle("");
                            break;
                        case 4:
                            setTitle("Derivative");
                            break;
                        case 5:
                            setTitle("Low-pass Filter");
                            break;
                        case 6:
                            setTitle("Hi-Freq Enhance");
                            break;
                        case 7:
                            setTitle("60hz Notch Filter");
                            break;
                        case 8:
                            setTitle("Median Filter");
                            break;
                        case 9:
                            setTitle("MOBD");
                            break;
                        default:
                            setTitle("Unknown Function");
                    }
                }
        );
    }

    private void graphValue(int val, ReplacingLineChartView chart) {
        runOnUiThread(() -> chart.addEntry(val));
    }

    private void onBytesReceived(byte[] data) {
        /*
        This is a bad way of doing this, but we need to.
        The predictability of the RN-42 is unreliable - hence, data we receive may or may not all come at once.
        So, we'll set some flags based on the first byte we receive, and check the next one after.
         */
        for (int i = 0; i < data.length; i++) {
            int val = data[i] & 0xFF;

            switch (byteReadFlag) {
                case -1: // Setting the expected next value.
                    byteReadFlag = val;
                    break;
                case 0: // Expecting a function change.
                    switchFunction(val);
                    byteReadFlag = -1;
                    break;
                case 1: // Expecting an untouched graph value.
                    // Skip every other graph value.  Too much data!
                    if (!skipTriggerOriginal) {
                        graphValue(val, mChartOriginal);
                    }
                    skipTriggerOriginal = !skipTriggerOriginal;
                    byteReadFlag = -1;
                    break;
                case 2: // Expecting the transformed graph value.
                    if (!skipTriggerTransformed) {
                        graphValue(val, mChartTransformed);
                    }
                    skipTriggerTransformed = !skipTriggerTransformed;
                    byteReadFlag = -1;
                    break;
                default:
                    byteReadFlag = -1;
                    Timber.e("Unknown byteReadFlag set (%d).", val);
                    break;
            }
        }
    }

    private void deviceSelected(BluetoothDevice device) {
        RxBluetooth.connectAsClient(device)
                .subscribe(socket -> {
                    Timber.d("Connected.");
                    snackMessage("Connected");

                    mSocket = socket;
                    RxBluetooth.readInputStream(socket)
                            .subscribe(this::onBytesReceived, this::onError);
                }, this::onError);
    }

    private void onError(Throwable e) {
        Timber.e(e, "Input Stream Error");

        if (e.getMessage().contains("bt socket closed")) {
            snackMessage("Disconnected");
        } else {
            snackMessage(e.getMessage());
        }
    }

    private void snackMessage(String text) {
        Snackbar.make(mToolbar, text, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSocket != null)
            try {
                mSocket.close();
            } catch (IOException e) {
            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
