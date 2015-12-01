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

package edu.uri.egr.bme363lab.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import edu.uri.egr.bme363lab.R;
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

    private final View.OnClickListener clickListener = clickSubject::onNext;

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
