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

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothDeviceDecorator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DeviceItemAdapter extends RecyclerView.Adapter<DeviceItemAdapter.ViewHolder> {

    private final Context mContext;
    private final List<BluetoothDeviceDecorator> mDevices;
    private final LayoutInflater mInflater;
    private OnAdapterItemClickListener mOnItemClickListener;


    public DeviceItemAdapter(Context context, List<BluetoothDevice> devices) {
        super();
        mContext = context;
        mDevices = decorateDevices(devices);
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static List<BluetoothDeviceDecorator> decorateDevices(Collection<BluetoothDevice> btDevices) {
        List<BluetoothDeviceDecorator> devices = new ArrayList<>();
        for (BluetoothDevice dev : btDevices) {
            devices.add(new BluetoothDeviceDecorator(dev, 0));
        }
        return devices;
    }

    public DeviceItemAdapter(Context context, Set<BluetoothDevice> devices) {
        this(context, new ArrayList<>(devices));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh = new ViewHolder(mInflater.inflate(R.layout.devicelist, parent, false));
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final BluetoothDeviceDecorator device = mDevices.get(position);

        holder.DeviceName.setText(TextUtils.isEmpty(device.getName()) ? "---" : device.getName());
        holder.DeviceAdress.setText(device.getAddress());
        holder.DeviceRSSI.setText(device.getRSSI() + "");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(device, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public List<BluetoothDeviceDecorator> getDevices() {
        return mDevices;
    }

    public void setOnAdapterItemClickListener(OnAdapterItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnAdapterItemClickListener {
        void onItemClick(BluetoothDeviceDecorator device, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView DeviceName;
        private final TextView DeviceAdress;
        private final TextView DeviceRSSI;

        public ViewHolder(View itemView) {
            super(itemView);
            DeviceName = (TextView) itemView.findViewById(R.id.DeviceName);
            DeviceAdress = (TextView) itemView.findViewById(R.id.DeviceAdress);
            DeviceRSSI = (TextView) itemView.findViewById(R.id.DeviceRSSI);
        }
    }
}