package com.nativeblemodule.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.AsyncTask;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    int deviceIndex = 0;

    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public BluetoothDevice firstStarling;

    public Map<String, String> uuids = new HashMap<String, String>();
    public Map<String, BluetoothGattCharacteristic> characteristics = new HashMap<String, BluetoothGattCharacteristic>();


    public bleModule(ReactApplicationContext reactContext) {
        super(reactContext);

        btManager = (BluetoothManager)getReactApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        uuids.put("EA540000-7D58-4E4B-A451-4BDD68DFE056", "Status");
        uuids.put("EA540001-7D58-4E4B-A451-4BDD68DFE056", "status_flags");
        uuids.put("EA540002-7D58-4E4B-A451-4BDD68DFE056", "unix_time");
        uuids.put("EA540003-7D58-4E4B-A451-4BDD68DFE056", "mic_volume");

        uuids.put("EA540010-7D58-4E4B-A451-4BDD68DFE056", "Config");
        uuids.put("EA540011-7D58-4E4B-A451-4BDD68DFE056", "threshold");
        uuids.put("EA540019-7D58-4E4B-A451-4BDD68DFE056", "indicator_led_enable");
        uuids.put("EA54001A-7D58-4E4B-A451-4BDD68DFE056", "request_to_enter_bootloader");
        uuids.put("EA54001B-7D58-4E4B-A451-4BDD68DFE056", "sync_mode");
        uuids.put("EA54001C-7D58-4E4B-A451-4BDD68DFE056", "device_name");
        uuids.put("EA54001D-7D58-4E4B-A451-4BDD68DFE056", "device_reset");

        uuids.put("EA540020-7D58-4E4B-A451-4BDD68DFE056", "WordCountData");
        uuids.put("EA540021-7D58-4E4B-A451-4BDD68DFE056", "total_word_count");
        uuids.put("EA540022-7D58-4E4B-A451-4BDD68DFE056", "enable");
        uuids.put("EA540024-7D58-4E4B-A451-4BDD68DFE056", "valid_records");
        uuids.put("EA540025-7D58-4E4B-A451-4BDD68DFE056", "record");
        uuids.put("EA540026-7D58-4E4B-A451-4BDD68DFE056", "oldest_record");
        uuids.put("EA540027-7D58-4E4B-A451-4BDD68DFE056", "next_record");
        uuids.put("EA540028-7D58-4E4B-A451-4BDD68DFE056", "delete_record");
        uuids.put("EA540029-7D58-4E4B-A451-4BDD68DFE056", "new_record");
        uuids.put("EA54002A-7D58-4E4B-A451-4BDD68DFE056", "daily_word_count_history");
        uuids.put("EA54002B-7D58-4E4B-A451-4BDD68DFE056", "daily_reset_time");
        uuids.put("EA54002C-7D58-4E4B-A451-4BDD68DFE056", "daily_word_goal");
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
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("DeviceDiscovered", deviceIndex + ": " + result.getDevice().getName());

            if (result.getDevice().getName() == "Starling") {
                firstStarling = result.getDevice();
                stopScanning();
            }
            deviceIndex++;
        }
    };

    // Device connect call back
    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("Event", "Characteristic Updated");

            readWordCount();
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
                            .emit("DeviceStateChanged", "0, disconnected");
                    break;
                case 2:
                    // disconnected
                    getReactApplicationContext()
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit("DeviceStateChanged", "2, connected");

                    bluetoothGatt.discoverServices();

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
            displayGattServices(bluetoothGatt.getServices());
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            } else {
                getReactApplicationContext()
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("Event", "Something went wrong");
            }
        }
    };

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        if (characteristic == characteristics.get("EA540021-7D58-4E4B-A451-4BDD68DFE056")) {
            final int count = ByteBuffer.wrap(characteristic.getValue()).order(ByteOrder.LITTLE_ENDIAN).getInt();
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("WordCount", count);
        } else {
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("Event", "Characteristic read");
        }
    }

    @ReactMethod
    public void startScanning() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("Event", "Started Scanning");
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
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("Event", "Stopped Scanning");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }

    @ReactMethod
    public void writeLEDOn() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("Event", "turn starling led on");

        byte[] value = new byte[1];
        value[0] = (byte) (1 & 0xFF);
        BluetoothGattCharacteristic enableLEDCharacteristic = characteristics.get("EA540019-7D58-4E4B-A451-4BDD68DFE056");
        enableLEDCharacteristic.setValue(value);
        bluetoothGatt.writeCharacteristic(enableLEDCharacteristic);
    }

    @ReactMethod
    public void writeLEDOff() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("Event", "turn starling led off");

        byte[] value = new byte[1];
        value[0] = (byte) (0 & 0xFF);
        BluetoothGattCharacteristic enableLEDCharacteristic = characteristics.get("EA540019-7D58-4E4B-A451-4BDD68DFE056");
        enableLEDCharacteristic.setValue(value);
        bluetoothGatt.writeCharacteristic(enableLEDCharacteristic);
    }

    @ReactMethod
    public void readLED() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("Event", "Reading LED value");

        bluetoothGatt.readCharacteristic(characteristics.get("EA540019-7D58-4E4B-A451-4BDD68DFE056"));
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            final String uuid = gattService.getUuid().toString();
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("ServiceDiscovered", uuids.get(uuid.toUpperCase()));
            new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            // Loops through available Characteristics.
            for (final BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {

                final String charUuid = gattCharacteristic.getUuid().toString();
                getReactApplicationContext()
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("CharacteristicDiscovered", charUuid.toUpperCase());
                characteristics.put(charUuid.toUpperCase(), gattCharacteristic);
            }
        }
    }

    @ReactMethod
    public void connectToDeviceSelected(String deviceIndex) {
        int deviceSelected = Integer.parseInt(deviceIndex);
        bluetoothGatt = devicesDiscovered.get(deviceSelected).connectGatt(getReactApplicationContext(), false, btleGattCallback);
    }

    @ReactMethod
    public void disconnectDeviceSelected() {
        //peripheralTextView.append("Disconnecting from device\n");
        bluetoothGatt.disconnect();
    }

    @ReactMethod
    public void readWordCount() {
        if (btAdapter == null || bluetoothGatt == null) {
            System.out.println("BluetoothAdapter not initialized");
            return;
        }
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("Event", "Reading Word Count");
        // replace with characteristic uuid you're trying to read
        bluetoothGatt.readCharacteristic(characteristics.get("EA540021-7D58-4E4B-A451-4BDD68DFE056"));
    }

    @ReactMethod
    public void subscribeToWordCount() {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("Event", "Subscribing to word count");

        // replace with characteristic uuid you're trying to subscribe to
        BluetoothGattCharacteristic wordCountUpdate = characteristics.get("EA540021-7D58-4E4B-A451-4BDD68DFE056");
        BluetoothGattDescriptor descriptor = wordCountUpdate.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        Boolean status = bluetoothGatt.writeDescriptor(descriptor);
        System.out.println(status);
        bluetoothGatt.setCharacteristicNotification(wordCountUpdate, true);
    }

    // helper int to bytes method for writing values
    byte[] toBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }
}
