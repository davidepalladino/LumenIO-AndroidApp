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

public class BluetoothService {
    public static final String ACTION_STATUS = "STATUS";

    public static final String EXTRA_STATE = "STATUS";
    public static final String EXTRA_CONNECTED = "CONNECTED";
    public static final String EXTRA_DISCONNECTED = "DISCONNECTED";
    public static final String EXTRA_ERROR = "ERROR";

    public static final int REQUIRE_ENABLE_BLUETOOTH = 1;

    private static Context context = null;

    private static BluetoothService bluetoothService = null;

    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothDevice bluetoothDevice;
    private static BluetoothSocket bluetoothSocket;

    private BluetoothService() { }

    public static BluetoothService getInstance(BluetoothAdapter bluetoothAdapterFrom, Context contextFrom) {
        if (context == null) {
            context = contextFrom;
        }

        if (bluetoothService == null) {
            bluetoothService = new BluetoothService();
            bluetoothAdapter = bluetoothAdapterFrom;
        }

        return bluetoothService;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

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

    public boolean pair(String macAddress) {
        try {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(bluetoothDevice.getUuids()[0].getUuid());

            Log.i(BluetoothService.class.getSimpleName(), "Device paired");

            return true;
        } catch (IOException e) {
            Log.e(BluetoothService.class.getSimpleName(), "Error pairing with this reason: " + e);
        }

        return false;
    }

    public void connect() {
        if (bluetoothSocket != null && !bluetoothSocket.isConnected()) {
            new Thread(() -> {
                String intentExtra = null;

                try {
                    bluetoothSocket.connect();

                    intentExtra = EXTRA_CONNECTED;
                    Log.i(BluetoothService.class.getSimpleName(), "Connected to device");
                } catch (IOException e){
                    intentExtra = EXTRA_ERROR;
                    Log.e(BluetoothService.class.getSimpleName(), "Error connection with this reason: " + e);
                }

                Intent intent = new Intent(ACTION_STATUS);
                intent.putExtra(EXTRA_STATE, intentExtra);
                context.sendBroadcast(intent);
            }).start();
        }
    }

    public void disconnect() {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            new Thread(() -> {
                String intentExtra = null;

                try {
                    bluetoothSocket.close();

                    intentExtra = EXTRA_DISCONNECTED;
                    Log.i(BluetoothService.class.getSimpleName(), "Disconnected from device");
                } catch (IOException e){
                    intentExtra = EXTRA_ERROR;
                    Log.e(BluetoothService.class.getSimpleName(), "Error disconnection with this reason: " + e);
                }

                Intent intent = new Intent(ACTION_STATUS);
                intent.putExtra(EXTRA_STATE, intentExtra);
                context.sendBroadcast(intent);
            }).start();
        }
    }

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
                    Log.i(BluetoothService.class.getSimpleName(), "Data send: " + "{Red: " + Byte.toUnsignedInt(data[0]) + ", Green: "  + Byte.toUnsignedInt(data[1]) + ", Blue: "  + Byte.toUnsignedInt(data[2]) + "}");
                } catch (IOException e) {
                    disconnect();
                    Log.e(BluetoothService.class.getSimpleName(), "Error during transfer with this reason " + e);
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
                    Log.i(BluetoothService.class.getSimpleName(), "Data send: " + "{" + data + "}");
                } catch (IOException e) {
                    disconnect();
                    Log.e(BluetoothService.class.getSimpleName(), "Error during transfer with this reason " + e);
                }
            }).start();
        }
    }
}
