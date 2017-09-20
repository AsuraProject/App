package me.mountainviewer.asura;

/* Copyright 2017 Yuri Faria

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

import android.annotation.TargetApi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothDeviceDecorator;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothlowenergylibrary.BluetoothLeService;

import java.util.Arrays;
import java.util.UUID;

@TargetApi(21)
public class SelectGlassActivity extends AppCompatActivity implements BluetoothService.OnBluetoothScanCallback, BluetoothService.OnBluetoothEventCallback, DeviceItemAdapter.OnAdapterItemClickListener {

    /* BLE Variables */
    private BluetoothService mService;

    private boolean mScanning;
    private DeviceItemAdapter mAdapter;

    private static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static UUID UUID_CHARACTERISTIC = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private String TAG = "Asura";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_glass);

        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        BluetoothConfiguration mConfiguration = new BluetoothConfiguration();

        mConfiguration.bluetoothServiceClass = BluetoothLeService.class;

        mConfiguration.context = getApplicationContext();
        mConfiguration.bufferSize = 1024;
        mConfiguration.characterDelimiter = '\n';
        mConfiguration.deviceName = "Asura";
        mConfiguration.callListenersInMainThread = true;


        mConfiguration.uuidService = UUID_SERVICE;
        mConfiguration.uuidCharacteristic = UUID_CHARACTERISTIC;

        BluetoothService.init(mConfiguration);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.DeviceRecyclerView);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mAdapter = new DeviceItemAdapter(this, mBluetoothAdapter.getBondedDevices());
        mAdapter.setOnAdapterItemClickListener(this);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        assert mRecyclerView != null;
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mService = BluetoothService.getDefaultInstance();
        mService.setOnScanCallback(this);
        mService.setOnEventCallback(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mService.setOnEventCallback(this);
    }

    /* Button Functions */

    public void searchGlass(View v) {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Device don't suport Bluetooth Low Energy", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothAdapter mBluetoothAdapter;
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            int REQUEST_ENABLE_BT = 1;
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        }

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        startStopScan();
    }

    /* Marshmallow Functions */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length < 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Turn on GPS for the app to work", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /* Callback */

    private void startStopScan() {
        if (!mScanning) {
            mService.startScan();
        } else {
            mService.stopScan();
        }
    }

    @Override
    public void onDeviceDiscovered(BluetoothDevice device, int rssi) {
        Log.d(TAG, "onDeviceDiscovered: " + device.getName() + " - " + device.getAddress() + " - " + Arrays.toString(device.getUuids()));
        BluetoothDeviceDecorator dv = new BluetoothDeviceDecorator(device, rssi);
        int index = mAdapter.getDevices().indexOf(dv);
        if (index < 0) {
            mAdapter.getDevices().add(dv);
            mAdapter.notifyItemInserted(mAdapter.getDevices().size() - 1);
        } else {
            mAdapter.getDevices().get(index).setDevice(device);
            mAdapter.getDevices().get(index).setRSSI(rssi);
            mAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void onStartScan() {
        Log.d(TAG, "onStartScan");
        mScanning = true;
    }

    @Override
    public void onStopScan() {
        Log.d(TAG, "onStopScan");
        mScanning = false;
    }

    @Override
    public void onDataRead(byte[] buffer, int length) {
        Log.d(TAG, "onDataRead");
    }

    @Override
    public void onStatusChange(BluetoothStatus status) {
        Log.d(TAG, "onStatusChange: " + status);

        if (status == BluetoothStatus.CONNECTED) {
            startActivity(new Intent(this, AsuraActivity.class));
        }

    }

    @Override
    public void onDeviceName(String deviceName) {
        Toast.makeText(this, "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onToast(String message) {
        Log.d(TAG, "onToast");
    }

    @Override
    public void onDataWrite(byte[] buffer) {
        Log.d(TAG, "onDataWrite");
    }

    @Override
    public void onItemClick(BluetoothDeviceDecorator device, int position) {
        mService.connect(device.getDevice());
    }
}