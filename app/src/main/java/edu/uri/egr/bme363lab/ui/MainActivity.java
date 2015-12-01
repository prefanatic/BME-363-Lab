package edu.uri.egr.bme363lab.ui;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.uri.egr.bme363lab.ui.dialog.DeviceListDialog;
import edu.uri.egr.bme363lab.R;
import edu.uri.egr.bme363lab.ui.widget.ReplacingLineChartView;
import edu.uri.egr.bme363lab.RxBluetooth;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    /*
    Using the library ButterKnife (https://github.com/JakeWharton/butterknife)
    we can "Bind" our views directly into fields within our MainActivity.  This is pretty much short hand.
    Without ButterKnife, we cannot do @Bind, or @OnClick.  We would have to use findViewById() - and that is a pain.
     */
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.fab) FloatingActionButton mFab;
    @Bind(R.id.line_chart_original) ReplacingLineChartView mChartOriginal;
    @Bind(R.id.line_chart_transformed) ReplacingLineChartView mChartTransformed;

    /*
    Define the fields we are going to use globally within the MainActivity.
     */
    private BluetoothSocket mSocket; // BluetoothSocket that contains bluetooth info to our PIC.
    private int currentFunction; // Unused?  Delete?
    private int byteReadFlag = -1; // An integer determining the type of data we are expected to receive next receive click.
    private volatile int graphValueToAdd;
    private boolean skipTriggerOriginal = false; // Skip triggers used to draw every other point we receive.
    private boolean skipTriggerTransformed = false;

    /**
     * OnCreate Override.
     * This runs when the Activity is being created.  This can be through the initial start of the activity, a screen rotation, or a restore from being deleted.
     * @param savedInstanceState A bundle of objects if onCreate is run after being persisted through a rotation or delete.  Can be null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Always need to call super, otherwise Android can't perform the back-end related things to onCreate.
        setContentView(R.layout.activity_main); // Set our layout information.
        ButterKnife.bind(this); // Tell ButterKnife we want to bind the views we just made from setContentView.

        setSupportActionBar(mToolbar); // Set our toolbar to the one provided in our layout resource.
        mChartOriginal.setMaximumX(1024); // Prevent both charts from going beyond a 1024 point size in the X direction.
        mChartTransformed.setMaximumX(1024);
    }

    /**
     * OnFabClicked Callback.
     * This runs whenever the FAB (R.id.fab) is clicked.  This @OnClick functionality is provided through ButterKnife.
     * When the FAB is clicked, we'll open up a DeviceListDialog to choose the PIC to connect to.
     */
    @OnClick(R.id.fab)
    public void onFabClicked() {
        DeviceListDialog dialog = new DeviceListDialog(); // Create a new dialog object.
        dialog.getDevice().subscribe(this::deviceSelected); // "Subscribe" to the output of whatever we select.
        dialog.show(getFragmentManager(), "deviceList"); // Finally, show the dialog.
    }

    /**
     * switchFunction
     * This is called whenever we would like to switch the int value of the function from the PIC.
     * It handles the conversion of the int value to the String representation.
     * @param function Integer value of the function sent from the PIC.  Equal to the PIC's global function.
     */
    private void switchFunction(int function) {
        currentFunction = function; // Save this just incase we need it later.

        Timber.d("Switching to function %d.", function); // Log for debugging.

        /*
        Android requires any manipulation of the views to be done on the UI Thread (also known as the Main Thread)
        Because this function is being called from the Bluetooth callbacks, we need to hop over to the UI thread by running runOnUiThread.
         */
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

    /**
     * graphValue
     * Uses the parameters given to graph to a specified chart.
     * @param val Integer value of data to graph
     * @param chart The specified chart to graph on.
     */
    private void graphValue(int val, ReplacingLineChartView chart) {
        /*
        As with switchFunction, we also need to move to the UI Thread to manipulate this view.
         */
        runOnUiThread(() -> chart.addEntry(val));
    }

    /**
     * onBytesReceived
     * This is a callback run every time there is any sort of byte[] data received from the PIC.
     * @param data byte[] data containing info from the PIC.  Length is always unknown.
     */
    private void onBytesReceived(byte[] data) {
        /*
        This is a bad way of doing this, but we need to.
        The predictability of the RN-42 is unreliable - hence, data we receive may or may not all come at once.
        So, we'll set some flags based on the first byte we receive, and check the next one after.
         */
        for (int i = 0; i < data.length; i++) {
            int val = data[i] & 0xFF; // Shift the byte to 0-255

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
                    byteReadFlag = -1; // We've got no idea what this is.  Hold on tight until we get a byteReadFlag we know.
                    Timber.e("Unknown byteReadFlag set (%d).", val);
                    break;
            }
        }
    }

    /**
     * deviceSelected
     * Callback for the DeviceListDialog shown on FAB click.
     * This will run any time a device is selected from the dialog, and handles connecting to that device.
     * @param device BluetoothDevice from the dialog list.
     */
    private void deviceSelected(BluetoothDevice device) {
        // Use the provided RxBluetooth library to connect to the device.
        RxBluetooth.connectAsClient(device)
                .subscribe(socket -> {
                    Timber.d("Connected.");
                    snackMessage("Connected");

                    mSocket = socket; // Save the socket for when we need to clean up when we're done.

                    // Use RxBluetooth to listen to the InputStream of data.  Use onBytesReceived as a callback.
                    RxBluetooth.readInputStream(socket)
                            .subscribe(this::onBytesReceived, this::onError);

                }, this::onError);
    }

    /**
     * onError
     * Called anytime a Throwable is used to signify an error.
     * @param e Throwable of an error.
     */
    private void onError(Throwable e) {
        Timber.e(e, "Input Stream Error");

        if (e.getMessage().contains("bt socket closed")) {
            snackMessage("Disconnected");
        } else {
            snackMessage(e.getMessage());
        }
    }

    /**
     * snackMessage
     * Little helper function to display a Snackbar with a text provided.
     * @param text String message
     */
    private void snackMessage(String text) {
        Snackbar.make(mToolbar, text, Snackbar.LENGTH_LONG).show();
    }

    /**
     * onDestroy
     * Called anytime the Activity is about to go down for the count.  Anything you've made, you need to clean up here.
     * Currently, we're closing the BluetoothSocket.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this); // We need to unbind from ButterKnife, otherwise we leak memory.

        // If we've connected/this isn't null
        if (mSocket != null)

            try {
                mSocket.close(); // Close our socket!
            } catch (IOException e) {
            }
    }
}
