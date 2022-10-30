package it.davidepalladino.lumenio.view.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.skydoves.colorpickerview.listeners.ColorListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.FragmentManualBinding;
import it.davidepalladino.lumenio.util.BluetoothService;
import it.davidepalladino.lumenio.util.DeviceArrayAdapter;
import it.davidepalladino.lumenio.view.viewModel.ManualViewModel;

public class ManualFragment extends Fragment {
    public static int REQUIRE_ENABLE_BLUETOOTH = 1;

    private FragmentManualBinding fragmentManualBinding;
    private ManualViewModel manualViewModel;

    private Menu menu;
    private MenuInflater inflater;

    private AlertDialog dialogSelectDevice = null;

    private boolean errorSyntaxFieldName = false;

    private BluetoothService bluetoothService;
    private DeviceArrayAdapter deviceArrayAdapter = null;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String snackbarMessage = "";

            final String state = intent.getStringExtra(BluetoothService.STATUS);
            switch (state) {
                case BluetoothService.STATUS_CONNECTED:
                    menu.findItem(R.id.bluetooth).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_connected));
                    snackbarMessage = getString(R.string.device_connected);
                    break;
                case BluetoothService.STATUS_DISCONNECTED:
                    snackbarMessage = getString(R.string.device_disconnected);
                    menu.findItem(R.id.bluetooth).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_disconnected));
                    break;
                case BluetoothService.STATUS_ERROR:
                    snackbarMessage = getString(R.string.device_error);
                    menu.findItem(R.id.bluetooth).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_disconnected));
                    break;
            }

            Snackbar.make(fragmentManualBinding.getRoot(), snackbarMessage, 5000).show();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        bluetoothService = BluetoothService.getInstance(requireActivity().getSystemService(BluetoothManager.class).getAdapter());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        manualViewModel = new ViewModelProvider(requireActivity()).get(ManualViewModel.class);

        new Thread(() -> {
            SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
            manualViewModel.loadByID(sharedPreferences.getLong(getString(R.string.latest_profile_selected), 0));
        }).start();

        fragmentManualBinding = FragmentManualBinding.inflate(inflater, container, false);
        fragmentManualBinding.setLifecycleOwner(this);

        fragmentManualBinding.setManualViewModel(manualViewModel);
        fragmentManualBinding.setManualFragment(this);

        fragmentManualBinding.colorPickerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                motionEvent.getAction();
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        fragmentManualBinding.scrollView.setIsScrollable(false);
                        break;
                    case MotionEvent.ACTION_UP:
                        fragmentManualBinding.scrollView.setIsScrollable(true);
                        break;
                }

                return false;
            }
        });

        fragmentManualBinding.colorPickerView.setColorListener(new ColorListener() {
            @Override
            public void onColorSelected(int color, boolean fromUser) {
                fragmentManualBinding.preview.setBackgroundColor(color);
            }
        });

        fragmentManualBinding.brightnessSlide.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                motionEvent.getAction();
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        fragmentManualBinding.scrollView.setIsScrollable(false);
                        break;
                    case MotionEvent.ACTION_UP:
                        fragmentManualBinding.scrollView.setIsScrollable(true);
                        break;
                }

                return false;
            }
        });

        return fragmentManualBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        requireActivity().registerReceiver(broadcastReceiver, new IntentFilter("BLUETOOTH_CONNECTION"));

        /* Verify if the dialog for device selection is open, to update the list of devices. */
        if (dialogSelectDevice != null && dialogSelectDevice.isShowing()) {
            ArrayList<BluetoothDevice> bluetoothDevices = bluetoothService.getList(getString(R.string.app_name));
            deviceArrayAdapter.clear();
            deviceArrayAdapter.addAll(bluetoothDevices);
            deviceArrayAdapter.notifyDataSetChanged();

            if (bluetoothDevices.size() > 0) {
                dialogSelectDevice.findViewById(R.id.no_device_dialog_search_device).setVisibility(View.GONE);
                dialogSelectDevice.findViewById(R.id.list_dialog_search_device).setVisibility(View.VISIBLE);
            } else {
                dialogSelectDevice.findViewById(R.id.no_device_dialog_search_device).setVisibility(View.VISIBLE);
                dialogSelectDevice.findViewById(R.id.list_dialog_search_device).setVisibility(View.GONE);
            }
        }

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(fragmentManualBinding.toolbar);
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        appCompatActivity.getSupportActionBar().setTitle(R.string.app_name);

        fragmentManualBinding.colorPickerView.attachBrightnessSlider(fragmentManualBinding.brightnessSlide);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentManualBinding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        this.menu = menu;
        this.inflater = inflater;

        this.menu.clear();
        this.inflater.inflate(R.menu.menu_control, menu);

        if (bluetoothService.isConnected()) {
            menu.findItem(R.id.bluetooth).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_connected));
        } else {
            menu.findItem(R.id.bluetooth).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_disconnected));
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.bluetooth:
                if (!bluetoothService.isConnected()) {
                    if (!bluetoothService.getBluetoothAdapter().isEnabled()) {
                        Intent intentRequestEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intentRequestEnable, REQUIRE_ENABLE_BLUETOOTH);
                    } else {
                        pairAndConnectDevice();
                    }
                } else {
                    bluetoothService.disconnect(requireContext());
                }

                break;
        }

        return true;
    }

    public void checkSyntaxName(CharSequence s, int start, int before, int count) {
        if (s.toString().matches(getString(R.string.sentence_incorrect_only_white_space))) {
            errorSyntaxFieldName = true;

            fragmentManualBinding.messageName.setText(getString(R.string.empty_field));
            fragmentManualBinding.messageName.setVisibility(View.VISIBLE);
            fragmentManualBinding.fabAdd.setClickable(false);
        } else if (s.toString().matches(getString(R.string.sentence_incorrect_white_space_start))) {
            errorSyntaxFieldName = true;

            fragmentManualBinding.messageName.setText(R.string.incorrect_start_field);
            fragmentManualBinding.messageName.setVisibility(View.VISIBLE);
            fragmentManualBinding.fabAdd.setClickable(false);
        } else if (s.toString().matches(getString(R.string.sentence_correct))) {
            errorSyntaxFieldName = false;

            fragmentManualBinding.messageName.setText("");
            fragmentManualBinding.messageName.setVisibility(View.GONE);
            fragmentManualBinding.fabAdd.setClickable(true);
        }
    }

    public void saveIntoLibrary() {
        if (fragmentManualBinding.name.getText().length() > 0 && !errorSyntaxFieldName) {
            new Thread(() -> {
                String snackbarMessage = manualViewModel.getSelectedName().getValue();

                try {
                    SharedPreferences.Editor sharedPreferencesEditor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
                    sharedPreferencesEditor.putLong(getString(R.string.latest_profile_selected), manualViewModel.insert());
                    sharedPreferencesEditor.apply();

                    manualViewModel.reload();

                    snackbarMessage += " " + getString(R.string.profile_saved);
                } catch (SQLiteConstraintException e) {
                    snackbarMessage += " " + getString(R.string.profile_not_saved_for_name);
                }

                SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
                spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, manualViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                Snackbar.make(fragmentManualBinding.getRoot(), spannableSnackbarMessage, 5000).setAnchorView(fragmentManualBinding.fabAdd).show();
            }).start();
        } else if (fragmentManualBinding.name.getText().length() == 0 && !errorSyntaxFieldName) {
            fragmentManualBinding.messageName.setText(getString(R.string.empty_field));
            fragmentManualBinding.messageName.setVisibility(View.VISIBLE);
            fragmentManualBinding.fabAdd.setClickable(false);
        }
    }

    private void pairAndConnectDevice() {
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String deviceSelected = sharedPreferences.getString(getString(R.string.device_selected), "");

        /* If the device is never been set, or the actual device is now not bonded, will be an AlertDialog for a selection. */
        if (deviceSelected.isEmpty() || (!deviceSelected.isEmpty() && bluetoothService.getBluetoothAdapter().getRemoteDevice(deviceSelected).getBondState() == 10)) {
            ArrayList<BluetoothDevice> bluetoothDevices = bluetoothService.getList(getString(R.string.app_name));
            deviceArrayAdapter = new DeviceArrayAdapter(requireContext(), bluetoothDevices);

            View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_search_device, null);

            dialogSelectDevice = new AlertDialog.Builder(requireActivity())
                    .setView(view)
                    .create();

            ImageView buttonBluetoothSetting = view.findViewById(R.id.button_setting_dialog_search_device);
            buttonBluetoothSetting.setOnClickListener(v -> {
                Intent intentBluetoothSetting = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentBluetoothSetting);
            });

            ListView listView = view.findViewById(R.id.list_dialog_search_device);
            listView.setAdapter(deviceArrayAdapter);
            listView.setOnItemClickListener((parent, view1, position, id) -> {
                String selection = deviceArrayAdapter.getItem(position).getAddress();
                SharedPreferences.Editor sharedPreferencesEditor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
                sharedPreferencesEditor.putString(getString(R.string.device_selected), selection);
                sharedPreferencesEditor.apply();

                if (bluetoothService.pair(selection)) {
                    bluetoothService.connect(requireContext());
                }

                dialogSelectDevice.dismiss();
            });

            LinearLayout noDevice = view.findViewById(R.id.no_device_dialog_search_device);

            if (bluetoothDevices.size() > 0) {
                noDevice.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            } else {
                noDevice.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            }

            dialogSelectDevice.show();
        } else {
            if (bluetoothService.pair(deviceSelected)) {
                bluetoothService.connect(requireContext());
            }
        }
    }

    // FIXME: Implement the process.
    public void updateDevice() {
        if (bluetoothService.isConnected()) {
            //FIXME
//            String json = "";
//            try {
//                json = new JSONObject()
//                        .put(getString(R.string.request), 1)
//                        .put(getString(R.string.values), new JSONObject()
//                                .put(getString(R.string.brightness), fragmentManualBinding.seekbarBrightness.getProgress())
//                                .put(getString(R.string.red), fragmentManualBinding.seekbarRed.getProgress())
//                                .put(getString(R.string.green), fragmentManualBinding.seekbarGreen.getProgress())
//                                .put(getString(R.string.blue), fragmentManualBinding.seekbarBlue.getProgress())
//                        )
//                        .toString();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            bluetoothService.writeData(requireContext(), json);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUIRE_ENABLE_BLUETOOTH) {
                pairAndConnectDevice();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}