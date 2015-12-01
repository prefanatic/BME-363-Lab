package edu.uri.egr.bme363lab.ui.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.uri.egr.bme363lab.R;
import edu.uri.egr.bme363lab.ui.adapter.DeviceAdapter;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

public class DeviceListDialog extends DialogFragment {
    @Bind(R.id.recycler_view) RecyclerView mRecycler;
    @Bind(R.id.button_search) Button mSearch;

    private DeviceAdapter mAdapter;
    private Subscription mSubscription;
    private final PublishSubject<BluetoothDevice> mSubject = PublishSubject.create();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_device_list, null, false);

        ButterKnife.bind(this, view);

        setupRecyclerView();
        populateFromBondedDevices();

        builder.setTitle(R.string.dialog_select_device);
        builder.setView(view);

        return builder.create();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (!mSubject.hasCompleted())
            mSubject.onCompleted();
        mSubscription.unsubscribe();
    }

    @OnClick(R.id.button_search)
    public void searchForDevices() {
        // TODO: 10/17/2015 SEARCH
    }

    public Observable<BluetoothDevice> getDevice() {
        return mSubject.asObservable();
    }

    private void deviceClicked(View view) {
        int i = mRecycler.getChildAdapterPosition(view);
        BluetoothDevice device = mAdapter.devices.get(i);

        mSubject.onNext(device);
        mSubject.onCompleted();

        dismiss();
    }

    private void setupRecyclerView() {
        mAdapter = new DeviceAdapter();
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubscription = mAdapter.getClicked().subscribe(this::deviceClicked);
    }

    private void populateFromBondedDevices() {
        for (BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices())
            mAdapter.add(device);
    }
}
