package it.davidepalladino.lumenio.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class BluetoothService {
    public static final String INTENT_ACTION = "BLUETOOTH_CONNECTION";
    public static final String STATUS = "STATUS";
    public static final String STATUS_CONNECTED = "CONNECTED";
    public static final String STATUS_DISCONNECTED = "DISCONNECTED";
    public static final String STATUS_ERROR = "ERROR";

    private static BluetoothService bluetoothService = null;

    private static BluetoothDevice bluetoothDevice;
    private static BluetoothSocket bluetoothSocket;

    private static Handler handlerCheckConnection = new Handler();
    private static Runnable runnableCheckConnection = null;

    private BluetoothService() {}

    public static BluetoothService getInstance() {
        if (bluetoothService == null) {
            bluetoothService = new BluetoothService();
        }

        return bluetoothService;
    }

    public ArrayList<BluetoothDevice> getList(BluetoothAdapter bluetoothAdapter, String filterDeviceName) {
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

    public boolean pair(BluetoothAdapter bluetoothAdapter, String macAddress) {
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

    public void connect(Context context) {
        if (bluetoothSocket != null && !bluetoothSocket.isConnected()) {
            new Thread(() -> {
                String intentValue = null;

                try {
                    bluetoothSocket.connect();

                    intentValue = STATUS_CONNECTED;
                    Log.i(BluetoothService.class.getSimpleName(), "Connected to device");
                } catch (IOException e){
                    intentValue = STATUS_ERROR;
                    Log.e(BluetoothService.class.getSimpleName(), "Error connection with this reason: " + e);
                }

                Intent intent = new Intent(INTENT_ACTION);
                intent.putExtra(STATUS, intentValue);
                context.sendBroadcast(intent);
            }).start();
        }
    }

    public void disconnect(Context context) {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            new Thread(() -> {
                String intentValue = null;

                try {
                    bluetoothSocket.close();

                    intentValue = STATUS_DISCONNECTED;
                    Log.i(BluetoothService.class.getSimpleName(), "Disconnected from device");
                } catch (IOException e){
                    intentValue = STATUS_ERROR;
                    Log.e(BluetoothService.class.getSimpleName(), "Error disconnection with this reason: " + e);
                }

                Intent intent = new Intent(INTENT_ACTION);
                intent.putExtra(STATUS, intentValue);
                context.sendBroadcast(intent);
            }).start();
        }
    }

    public boolean isConnected() {
        return bluetoothSocket != null && bluetoothSocket.isConnected();
    }

    public void writeData(Context context, String data) {
        if (bluetoothSocket != null) {
            new Thread(() -> {
                try {
                    OutputStream outputStream = bluetoothSocket.getOutputStream();
                    outputStream.write(data.getBytes());
                    Log.i(BluetoothService.class.getSimpleName(), "Data send");
                } catch (IOException e) {
                    disconnect(context);
                    Log.e(BluetoothService.class.getSimpleName(), "Error during transfer with this reason " + e);
                }
            }).start();
        }
    }
}