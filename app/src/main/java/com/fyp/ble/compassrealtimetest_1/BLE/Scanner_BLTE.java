package com.fyp.ble.compassrealtimetest_1.BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import com.fyp.ble.compassrealtimetest_1.MainActivity;


public class Scanner_BLTE {
//    private MainActivity ma;
    private MainActivity ma;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private long scanPeriod;
    private int signalStrength;

    //Constructor
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public Scanner_BLTE(MainActivity mainActivity, long scanPeriod, int signalStrength) {
        ma = mainActivity;
        mHandler = new Handler();
        this.scanPeriod = scanPeriod;
        this.signalStrength = signalStrength;

        final BluetoothManager bluetoothManager =
                (BluetoothManager) ma.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    public boolean isScanning() {
        return mScanning;
    }

    public void start() {
        scanLeDevice(true);
    }

    public void stop() {
        scanLeDevice(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private synchronized void scanLeDevice(final boolean enable) {
        if (enable && !mScanning) {

//             Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    mScanning = false;
                    stopLE();
                    ma.stopScan();
                }
            }, scanPeriod);

            mScanning = true;
            startLE();
        }
        else {
            mScanning = false;
            stopLE();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public synchronized void startLE(){
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public synchronized void stopLE(){
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public synchronized void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    final int new_rssi = rssi;
                    if ((rssi > signalStrength)) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ma.addDevice(device, new_rssi);
                            }
                        });
                    }
                }
            };




}
