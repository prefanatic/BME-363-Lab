package edu.uri.egr.bme363lab;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    public final ArrayList<BluetoothDevice> devices = new ArrayList<>();
    private final PublishSubject<View> clickSubject = PublishSubject.create();

    public void add(BluetoothDevice device) {
        devices.add(device);
        notifyItemInserted(devices.size());
    }

    public void remove(BluetoothDevice device) {
        int i = devices.indexOf(device);

        devices.remove(device);
        notifyItemRemoved(i);
    }

    public Observable<View> getClicked() {
        return clickSubject.asObservable();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);

        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    final View.OnClickListener clickListener = clickSubject::onNext;

    final class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.device_name) TextView name;
        @Bind(R.id.device_address) TextView address;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(clickListener);
        }
    }
}
