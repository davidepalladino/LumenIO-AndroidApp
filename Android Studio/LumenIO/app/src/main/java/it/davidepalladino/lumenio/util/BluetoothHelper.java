package it.davidepalladino.lumenio.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class BluetoothHelper {
    public static final String ACTION_STATUS = "STATUS";

    public static final String EXTRA_STATE = "STATUS";
    public static final String EXTRA_CONNECTED = "CONNECTED";
    public static final String EXTRA_DISCONNECTED = "DISCONNECTED";
    public static final String EXTRA_ERROR = "ERROR";

    public static final int REQUIRE_ENABLE_BLUETOOTH = 1;

    private static Context context = null;

    private static BluetoothHelper bluetoothHelper = null;

    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothDevice bluetoothDevice;
    private static BluetoothSocket bluetoothSocket;

    private BluetoothHelper() { }

    /**
     * Get the BluetoothService. If the Context was not saved previously, will be saved; BluetoothService, too.
     * @param bluetoothAdapterFrom BluetoothAdapter to save.
     * @param contextFrom Context of the application.
     * @return The instance of BluetoothService.
     */
    public static BluetoothHelper getInstance(BluetoothAdapter bluetoothAdapterFrom, Context contextFrom) {
        if (context == null) {
            context = contextFrom;
        }

        if (bluetoothHelper == null) {
            bluetoothHelper = new BluetoothHelper();
            bluetoothAdapter = bluetoothAdapterFrom;
        }

        return bluetoothHelper;
    }

    /**
     * Get the BluetoothAdapter.
     * @return The instance of BluetoothAdapter.
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    /**
     * Get a list of devices, filtered by name.
     * @param filterDeviceName The filter by name.
     * @return An ArrayList empty or with the BluetoothDevices.
     */
    public ArrayList<BluetoothDevice> getList(String filterDeviceName) {
        Set<BluetoothDevice> bluetoothDevicesFullList = bluetoothAdapter.getBondedDevices();

        if (filterDeviceName != null) {
            ArrayList<BluetoothDevice> bluetoothDevicesFilteredList = new ArrayList<>();
            Iterator<BluetoothDevice> bluetoothDeviceIterator = bluetoothDevicesFullList.iterator();

            while (bluetoothDeviceIterator.hasNext()) {
                BluetoothDevice bluetoothDevice = bluetoothDeviceIterator.next();
                if (bluetoothDevice.getName().contains(filterDeviceName)) {
                    bluetoothDevicesFilteredList.add(bluetoothDevice);
                }
            }

            return bluetoothDevicesFilteredList;
        } else {
            return new ArrayList<>(bluetoothDevicesFullList) ;
        }
    }

    public String getDeviceName() {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            return bluetoothSocket.getRemoteDevice().getName();
        } else {
            return "";
        }
    }

    /**
     * Pair the device via MAC Address.
     * @param macAddress MAC Address of device desired.
     * @return Value `true` if the pairing is established; else `false`.
     */
    public boolean pair(String macAddress) {
        try {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(bluetoothDevice.getUuids()[0].getUuid());

            Log.i(BluetoothHelper.class.getSimpleName(), "Device paired");

            return true;
        } catch (IOException e) {
            Log.e(BluetoothHelper.class.getSimpleName(), "Error pairing with this reason: " + e);
        }

        return false;
    }

    /**
     * Connect to the device and send a broadcast message in success case (`EXTRA_CONNECTED`) or not (`EXTRA_ERROR`).
     */
    public void connect() {
        if (bluetoothSocket != null && !bluetoothSocket.isConnected()) {
            new Thread(() -> {
                String intentExtra = null;

                try {
                    bluetoothSocket.connect();

                    intentExtra = EXTRA_CONNECTED;
                    Log.i(BluetoothHelper.class.getSimpleName(), "Connected to device");
                } catch (IOException e){
                    intentExtra = EXTRA_ERROR;
                    Log.e(BluetoothHelper.class.getSimpleName(), "Error connection with this reason: " + e);
                }

                Intent intent = new Intent(ACTION_STATUS);
                intent.putExtra(EXTRA_STATE, intentExtra);
                context.sendBroadcast(intent);
            }).start();
        }
    }

    /**
     * Disconnect from the device and send a broadcast message in success case (`EXTRA_DISCONNECTED`) or not (`EXTRA_ERROR`).
     */
    public void disconnect() {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            new Thread(() -> {
                String intentExtra = null;

                try {
                    bluetoothSocket.close();

                    intentExtra = EXTRA_DISCONNECTED;
                    Log.i(BluetoothHelper.class.getSimpleName(), "Disconnected from device");
                } catch (IOException e){
                    intentExtra = EXTRA_ERROR;
                    Log.e(BluetoothHelper.class.getSimpleName(), "Error disconnection with this reason: " + e);
                }

                Intent intent = new Intent(ACTION_STATUS);
                intent.putExtra(EXTRA_STATE, intentExtra);
                context.sendBroadcast(intent);
            }).start();
        }
    }

    /**
     * Check if there is a connecvtion between the App and the device.
     * @return Value `true` if the connection is established; else `false`.
     */
    public boolean isConnected() { return bluetoothSocket != null && bluetoothSocket.isConnected(); }

    /**
     * Send an array of bytes with the values sorted by RED, GREEN and BLUE. Is the fasted way for
     *  the device because it must not allocate a String object for every packet received.
     * @param data Array of bytes to send.
     */
    public void writeData(byte[] data) {
        if (bluetoothSocket != null) {
            new Thread(() -> {
                try {
                    OutputStream outputStream = bluetoothSocket.getOutputStream();
                    outputStream.write(data);
                    Log.i(BluetoothHelper.class.getSimpleName(), "Data send: " + "{Red: " + Byte.toUnsignedInt(data[0]) + ", Green: "  + Byte.toUnsignedInt(data[1]) + ", Blue: "  + Byte.toUnsignedInt(data[2]) + "}");
                } catch (IOException e) {
                    disconnect();
                    Log.e(BluetoothHelper.class.getSimpleName(), "Error during transfer with this reason " + e);
                }
            }).start();
        }
    }

    /**
     * Send a string in JSON format to the device.
     * @param data String to write.
     */
    public void writeData(String data) {
        if (bluetoothSocket != null) {
            new Thread(() -> {
                try {
                    OutputStream outputStream = bluetoothSocket.getOutputStream();
                    outputStream.write(data.getBytes());
                    Log.i(BluetoothHelper.class.getSimpleName(), "Data send: " + "{" + data + "}");
                } catch (IOException e) {
                    disconnect();
                    Log.e(BluetoothHelper.class.getSimpleName(), "Error during transfer with this reason " + e);
                }
            }).start();
        }
    }
}
