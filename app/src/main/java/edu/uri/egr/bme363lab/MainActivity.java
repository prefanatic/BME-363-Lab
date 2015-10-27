package edu.uri.egr.bme363lab;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
    @Bind(R.id.line_chart) ReplacingLineChartView mChart;

    private BluetoothSocket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mChart.setMaximumX(255);
        mChart.setForwardRemoveSize(10);
    }

    @OnClick(R.id.fab)
    public void onFabClicked() {
        DeviceListDialog dialog = new DeviceListDialog();
        dialog.getDevice().subscribe(this::deviceSelected);
        dialog.show(getFragmentManager(), "deviceList");
    }

    private void onBytesReceived(byte[] data) {
        int ledCount = data[0] & 0xFF;
        Timber.d("Received %d", ledCount);

        mChart.addEntry(ledCount);
    }

    private void deviceSelected(BluetoothDevice device) {
        RxBluetooth.connectAsClient(device)
                .subscribe(socket -> {
                    Timber.d("Connected.");
                    mSocket = socket;
                    RxBluetooth.readInputStream(socket)
                            .subscribe(this::onBytesReceived);
                }, err -> {
                    Timber.e("Failed to connect: %s", err.getMessage());
                });
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
