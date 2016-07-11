package com.nativeblemodule.ble;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.telecom.Call;
import android.view.View;
import android.widget.AbsListView;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.nativeblemodule.MainActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by joelwasserman on 7/5/16.
 */
public class bleModule extends ReactContextBaseJavaModule {


    public BluetoothManager btManager;
    public BluetoothAdapter btAdapter;
    public BluetoothLeScanner btScanner;
    private final static int REQUEST_ENABLE_BT = 1;
    ArrayList<BluetoothDevice> devicesDiscovered = new ArrayList<BluetoothDevice>();
    BluetoothGatt bluetoothGatt;
    Boolean btScanning = false;
    int deviceIndex = 0;
    String btCurrentState = "nothing has happened";

    public bleModule(ReactApplicationContext reactContext) {
        super(reactContext);

        btManager = (BluetoothManager)getReactApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
    }

    @Override
    public String getName() {
        return "BLE";
    }

    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            devicesDiscovered.add(result.getDevice());
            deviceIndex++;

            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("DeviceDiscovered", deviceIndex + ": " + result.getDevice().getName());

        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            // this will get called anytime you perform a read or write characteristic operation
//            MainActivity.this.runOnUiThread(new Runnable() {
//                public void run() {
//                    peripheralTextView.append("device read or wrote to\n");
//                }
//            });
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            System.out.println(newState);
            switch (newState) {
                case 0:
                    // connected
                    getReactApplicationContext()
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("DeviceStateChanged", "0 connected");
                    break;
                case 2:
                    // disconnected
                    getReactApplicationContext()
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("DeviceStateChanged", "2, disconnected");
                    break;
                default:
                    getReactApplicationContext()
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("DeviceStateChanged", "default: shouldn't be hitting");
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call

        }
    };

    @ReactMethod
    public void startScanning() {
        System.out.println("start scanning");
        btScanning = true;
        btCurrentState = "scanning";
        deviceIndex = 0;
        devicesDiscovered.clear();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    @ReactMethod
    public void stopScanning() {
        System.out.println("stopping scanning");
        btCurrentState = "stopped scanning";
        btScanning = false;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    @ReactMethod
    public void getState(Callback intCallback) {
        String _getState = btCurrentState;
        intCallback.invoke(_getState);
    }

    @ReactMethod
    public void getDevices(Callback setCallback) {
        setCallback.invoke(devicesDiscovered.get(0).getName());
    }

    public void connectToDeviceSelected() {
        //peripheralTextView.append("Trying to connect to device at index: " + deviceIndexInput.getText() + "\n");
        //int deviceSelected = Integer.parseInt(deviceIndexInput.getText().toString());
        //bluetoothGatt = devicesDiscovered.get(deviceSelected).connectGatt(this, false, btleGattCallback);
    }

    public void disconnectDeviceSelected() {
        //peripheralTextView.append("Disconnecting from device\n");
        bluetoothGatt.disconnect();
    }

}
