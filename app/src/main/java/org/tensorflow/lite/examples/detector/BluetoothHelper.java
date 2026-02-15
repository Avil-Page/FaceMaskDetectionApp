package org.tensorflow.lite.examples.detector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Helper class to connect to a paired HC-05 Bluetooth module and send data.
 */
public class BluetoothHelper {
    private static final String TAG = "BluetoothHelper";
    // Standard SPP UUID for HC-05
    private static final UUID HC05_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String HC05_NAME = "HC-05";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private boolean connected = false;

    public BluetoothHelper() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Find the paired HC-05 device and connect to it.
     * Call this from a background thread.
     */
    public void connect() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported on this device");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled");
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice hc05Device = null;

        for (BluetoothDevice device : pairedDevices) {
            if (device.getName() != null && device.getName().contains(HC05_NAME)) {
                hc05Device = device;
                break;
            }
        }

        if (hc05Device == null) {
            Log.e(TAG, "HC-05 not found in paired devices");
            return;
        }

        try {
            socket = hc05Device.createRfcommSocketToServiceRecord(HC05_UUID);
            bluetoothAdapter.cancelDiscovery();
            socket.connect();
            outputStream = socket.getOutputStream();
            connected = true;
            Log.i(TAG, "Connected to HC-05");
        } catch (IOException e) {
            Log.e(TAG, "Connection failed: " + e.getMessage());
            connected = false;
            close();
        }
    }

    /**
     * Send a string to the HC-05 module.
     */
    public void send(String data) {
        if (!connected || outputStream == null) return;
        try {
            outputStream.write(data.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Send failed: " + e.getMessage());
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Close the Bluetooth connection.
     */
    public void close() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            Log.e(TAG, "Close failed: " + e.getMessage());
        }
        connected = false;
    }
}
