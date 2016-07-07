package com.nativeblemodule.ble;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.AbsListView;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by joelwasserman on 7/5/16.
 */
public class bleModule extends ReactContextBaseJavaModule {

    private AbsListView mListView;
    final Set<BluetoothDevice> peripheralList = new Set<BluetoothDevice>() {
        @Override
        public boolean add(BluetoothDevice object) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends BluetoothDevice> collection) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public boolean contains(Object object) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @NonNull
        @Override
        public Iterator<BluetoothDevice> iterator() {
            return null;
        }

        @Override
        public boolean remove(Object object) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @NonNull
        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @NonNull
        @Override
        public <T> T[] toArray(T[] array) {
            return null;
        }
    };
    private static BluetoothAdapter bTAdapter;

    public bleModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "BLE";
    }

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                peripheralList.add(device);
            }
        }
    };

    @ReactMethod
    public void startScanning() {
        bTAdapter = BluetoothAdapter.getDefaultAdapter();
        peripheralList.clear();
        //registerReceiver(bReciever, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        bTAdapter.startDiscovery();
    }

    @ReactMethod
    public Set<BluetoothDevice> getDevices() {
        return peripheralList;
    }

    @ReactMethod
    public void stopScanning() {
        peripheralList.clear();
        bTAdapter.cancelDiscovery();
    }
}
