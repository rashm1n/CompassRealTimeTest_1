package com.fyp.ble.compassrealtimetest_1.BLE;//package com.fyp.ble.navigationinitialize.BLE;
//
//import android.app.Activity;
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//
///**
// * Created by Kelvin on 5/7/16.
// */
//public class ListAdapter_BTLE_Devices extends ArrayAdapter<BLTE_Device> {
//
//    Activity activity;
//    int layoutResourceID;
//    ArrayList<BLTE_Device> devices;
//
//    public ListAdapter_BTLE_Devices(Activity activity, int resource, ArrayList<BLTE_Device> objects) {
//        super(activity.getApplicationContext(), resource, objects);
//
//        this.activity = activity;
//        layoutResourceID = resource;
//        devices = objects;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        if (convertView == null) {
//            LayoutInflater inflater =
//                    (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = inflater.inflate(layoutResourceID, parent, false);
//        }
//
//        BLTE_Device device = devices.get(position);
//        String name = device.getName();
//        String address = device.getAddress();
//        int rssi = device.getRSSI();
//
//
//        TextView tv_rssi = (TextView) convertView.findViewById(R.id.tv_rssi);
//        tv_rssi.setText("RSSI: " + Integer.toString(rssi));
//
//        TextView tv_macaddr = (TextView) convertView.findViewById(R.id.tv_macaddr);
//        if (address != null && address.length() > 0) {
//            tv_macaddr.setText(device.getAddress());
//        }
//        else {
//            tv_macaddr.setText("No Address");
//        }
//
//        return convertView;
//    }
//}
//
//
