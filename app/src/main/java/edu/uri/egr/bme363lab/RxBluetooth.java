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

package edu.uri.egr.bme363lab;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.UUID;

import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import rx.schedulers.Schedulers;

/**
 * RxBluetooth
 * ReactiveX wrapper for Android's Bluetooth API.
 * Makes it easier to work with Bluetooth streams by using a stream!
 */
public class RxBluetooth {
    private static final UUID SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static Observable<byte[]> readInputStream(BluetoothSocket socket) {
        return Observable.create(subscriber -> {
            InputStream stream = null;
            try {
                stream = socket.getInputStream();

                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((!subscriber.isUnsubscribed() || bytesRead != -1) && socket.isConnected()) {
                    bytesRead = stream.read(buffer);

                    if (bytesRead != 0) {
                        subscriber.onNext(Arrays.copyOf(buffer, bytesRead));
                    }
                }

            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    public static Observable<BluetoothSocket> connectAsClient(BluetoothDevice device) {
        return Observable.defer(() -> {
            BluetoothSocket socket = null;
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP);

                if (BluetoothAdapter.getDefaultAdapter().isDiscovering())
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

                socket.connect();
            } catch (IOException e) {

                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException a) {
                        throw OnErrorThrowable.from(a);
                    }
                }

                throw OnErrorThrowable.from(e);
            }

            return Observable.just(socket);
        }).subscribeOn(Schedulers.io());
    }
}
