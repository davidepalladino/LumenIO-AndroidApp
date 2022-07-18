package it.davidepalladino.lumenio.util;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import it.davidepalladino.lumenio.R;

public class DeviceArrayAdapter extends ArrayAdapter<BluetoothDevice> {
    private final Context context;
    private final int resource;

    public DeviceArrayAdapter(@NonNull Context context, @NonNull List<BluetoothDevice> objects) {
        super(context, R.layout.adapter_view_device, objects);

        this.context = context;
        this.resource = R.layout.adapter_view_device;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(resource, null);

        BluetoothDevice bluetoothDevice = getItem(position);

        TextView nameDevice = convertView.findViewById(R.id.name_device);
        TextView macDevice = convertView.findViewById(R.id.mac_device);

        nameDevice.setText(bluetoothDevice.getName());
        macDevice.setText(bluetoothDevice.getAddress());

        return convertView;
    }
}
