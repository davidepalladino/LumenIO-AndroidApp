package it.davidepalladino.lumenio.view.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;

import static it.davidepalladino.lumenio.util.BluetoothHelper.REQUIRE_ENABLE_BLUETOOTH;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.ArrayList;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.DialogSaveProfileBinding;
import it.davidepalladino.lumenio.databinding.FragmentManualBinding;
import it.davidepalladino.lumenio.util.BluetoothHelper;
import it.davidepalladino.lumenio.util.DeviceArrayAdapter;
import it.davidepalladino.lumenio.util.DeviceStatusService;
import it.davidepalladino.lumenio.util.NotificationService;
import it.davidepalladino.lumenio.view.activity.MainActivity;
import it.davidepalladino.lumenio.view.viewModel.ManualViewModel;

public class ManualFragment extends Fragment {
    private FragmentManualBinding fragmentManualBinding;
    private DialogSaveProfileBinding dialogSaveProfileBinding;

    private ManualViewModel manualViewModel;

    private Menu menu;
    private MenuInflater inflater;

    private AlertDialog dialogSelectDevice = null;
    private AlertDialog dialogSaveProfile = null;

    private BluetoothHelper bluetoothHelper;

    private DeviceArrayAdapter deviceArrayAdapter = null;

    private NotificationService notificationService;

    private boolean isColorSelectedByUser = false;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (menu != null) {
                MenuItem itemStatus = menu.findItem(R.id.status_light);
                MenuItem itemBluetooth = menu.findItem(R.id.bluetooth);

                String snackbarMessage = "";

                String action = intent.getAction();
                switch (action) {
                    case BluetoothHelper.ACTION_STATUS:
                        String extra = intent.getStringExtra(BluetoothHelper.EXTRA_STATE);
                        switch (extra) {
                            case BluetoothHelper.EXTRA_CONNECTED:
                                snackbarMessage = getString(R.string.device_connected);

                                notificationService.createNotification(getString(R.string.device_connected_name) + " " + bluetoothHelper.getDeviceName(), getString(R.string.notification_click_here_return_app));

                                DeviceStatusService.isTurnedOn = true;

                                itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_on));
                                itemStatus.setTitle(R.string.status_on);
                                itemStatus.setVisible(true);

                                itemBluetooth.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_connected));

//                                updateDevice(manualViewModel.getSelectedRed().getValue().byteValue(), manualViewModel.getSelectedGreen().getValue().byteValue(), manualViewModel.getSelectedBlue().getValue().byteValue());

                                DeviceStatusService.latestRed = manualViewModel.getSelectedRed().getValue().byteValue();
                                DeviceStatusService.latestGreen = manualViewModel.getSelectedGreen().getValue().byteValue();
                                DeviceStatusService.latestBlue = manualViewModel.getSelectedBlue().getValue().byteValue();

                                break;

                            case BluetoothHelper.EXTRA_DISCONNECTED:
                                snackbarMessage = getString(R.string.device_disconnected);

                                notificationService.destroyNotification();

                                DeviceStatusService.isTurnedOn = false;

                                itemStatus.setVisible(false);
                                itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_on));
                                itemStatus.setTitle(R.string.status_off);

                                itemBluetooth.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_disconnected));

                                break;

                            case BluetoothHelper.EXTRA_ERROR:
                                snackbarMessage = getString(R.string.device_error);

                                DeviceStatusService.isTurnedOn = false;

                                itemStatus.setVisible(false);
                                itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_on));
                                itemStatus.setTitle(R.string.status_off);

                                itemBluetooth.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_disconnected));

                                break;
                        }

                        break;

                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        if (bluetoothHelper.isConnected()) {
                            bluetoothHelper.disconnect();
                        }

                        break;
                }

                if (!snackbarMessage.isEmpty()) {
                    Snackbar.make(fragmentManualBinding.getRoot(), snackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
                }
            }
        }
    };

    private final ServiceConnection notificationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationService = ((NotificationService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    public static ManualFragment newInstance() { return new ManualFragment(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        manualViewModel = new ViewModelProvider(requireActivity()).get(ManualViewModel.class);

        bluetoothHelper = BluetoothHelper.getInstance(requireActivity().getSystemService(BluetoothManager.class).getAdapter(), requireContext());

        new Thread(() -> {
            SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
            manualViewModel.loadByID(sharedPreferences.getLong(getString(R.string.latest_profile_selected), 0));
        }).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentManualBinding = FragmentManualBinding.inflate(inflater, container, false);
        fragmentManualBinding.setLifecycleOwner(this);

        fragmentManualBinding.setManualViewModel(manualViewModel);
        fragmentManualBinding.setManualFragment(this);

        fragmentManualBinding.colorPicker.attachBrightnessSlider(fragmentManualBinding.brightnessSlide);
        fragmentManualBinding.colorPicker.setOnTouchListener((view, motionEvent) -> {
            motionEvent.getAction();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    fragmentManualBinding.scrollView.setIsScrollable(false);
                    isColorSelectedByUser = true;
                    break;
                case MotionEvent.ACTION_UP:
                    fragmentManualBinding.scrollView.setIsScrollable(true);
                    isColorSelectedByUser = false;
                    break;
            }

            return false;
        });
        fragmentManualBinding.colorPicker.setColorListener((ColorEnvelopeListener) (envelope, fromUser) -> {
            if (fromUser) {
                if (DeviceStatusService.latestSceneLoaded != 0) {
                    DeviceStatusService.latestSceneLoaded = 0;
                }

                manualViewModel.setSelectedHex(envelope.getHexCode().substring(2, 8));
                manualViewModel.setSelectedRed(envelope.getArgb()[1]);
                manualViewModel.setSelectedGreen(envelope.getArgb()[2]);
                manualViewModel.setSelectedBlue(envelope.getArgb()[3]);

                DeviceStatusService.latestRed = manualViewModel.getSelectedRed().getValue().byteValue();
                DeviceStatusService.latestGreen = manualViewModel.getSelectedGreen().getValue().byteValue();
                DeviceStatusService.latestBlue = manualViewModel.getSelectedBlue().getValue().byteValue();
            }
        });

        fragmentManualBinding.brightnessSlide.setOnTouchListener((view, motionEvent) -> {
            motionEvent.getAction();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    fragmentManualBinding.scrollView.setIsScrollable(false);
                    isColorSelectedByUser = true;
                    break;
                case MotionEvent.ACTION_UP:
                    fragmentManualBinding.scrollView.setIsScrollable(true);
                    isColorSelectedByUser = false;
                    break;
            }

            return false;
        });

        return fragmentManualBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothHelper.ACTION_STATUS);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        requireActivity().registerReceiver(broadcastReceiver, intentFilter);

        Intent intentDatabaseService = new Intent(getActivity(), NotificationService.class);
        requireActivity().bindService(intentDatabaseService, notificationServiceConnection, BIND_AUTO_CREATE);

        /* Verify if the dialog for device selection is open, to update the list of devices. */
        if (dialogSelectDevice != null && dialogSelectDevice.isShowing()) {
            ArrayList<BluetoothDevice> bluetoothDevices = bluetoothHelper.getList(getString(R.string.app_name));
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

        manualViewModel.getSelectedRed().observe(requireActivity(), integer -> {
            if (isColorSelectedByUser) {
                try {
                    fragmentManualBinding.colorPicker.selectByHsvColor(Color.rgb(manualViewModel.getSelectedRed().getValue(), manualViewModel.getSelectedGreen().getValue(), manualViewModel.getSelectedBlue().getValue()));
                    updateDevice(manualViewModel.getSelectedRed().getValue().byteValue(), manualViewModel.getSelectedGreen().getValue().byteValue(), manualViewModel.getSelectedBlue().getValue().byteValue());
                } catch (IllegalAccessException e) { e.printStackTrace(); }
            } else {
                fragmentManualBinding.colorPicker.setInitialColor(Color.rgb(manualViewModel.getSelectedRed().getValue(), manualViewModel.getSelectedGreen().getValue(), manualViewModel.getSelectedBlue().getValue()));
            }
        });

        manualViewModel.getSelectedGreen().observe(requireActivity(), integer -> {
            if (isColorSelectedByUser) {
                try {
                    fragmentManualBinding.colorPicker.selectByHsvColor(Color.rgb(manualViewModel.getSelectedRed().getValue(), manualViewModel.getSelectedGreen().getValue(), manualViewModel.getSelectedBlue().getValue()));
                    updateDevice(manualViewModel.getSelectedRed().getValue().byteValue(), manualViewModel.getSelectedGreen().getValue().byteValue(), manualViewModel.getSelectedBlue().getValue().byteValue());
                } catch (IllegalAccessException e) { e.printStackTrace(); }
            } else {
                fragmentManualBinding.colorPicker.setInitialColor(Color.rgb(manualViewModel.getSelectedRed().getValue(), manualViewModel.getSelectedGreen().getValue(), manualViewModel.getSelectedBlue().getValue()));
            }
        });

        manualViewModel.getSelectedBlue().observe(requireActivity(), integer -> {
            if (isColorSelectedByUser) {
                try {
                    fragmentManualBinding.colorPicker.selectByHsvColor(Color.rgb(manualViewModel.getSelectedRed().getValue(), manualViewModel.getSelectedGreen().getValue(), manualViewModel.getSelectedBlue().getValue()));
                    updateDevice(manualViewModel.getSelectedRed().getValue().byteValue(), manualViewModel.getSelectedGreen().getValue().byteValue(), manualViewModel.getSelectedBlue().getValue().byteValue());
                } catch (IllegalAccessException e) { e.printStackTrace(); }
            } else {
                fragmentManualBinding.colorPicker.setInitialColor(Color.rgb(manualViewModel.getSelectedRed().getValue(), manualViewModel.getSelectedGreen().getValue(), manualViewModel.getSelectedBlue().getValue()));
            }
        });

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        appCompatActivity.getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onPause() {
        super.onPause();

        requireActivity().unregisterReceiver(broadcastReceiver);

        manualViewModel.getSelectedRed().removeObservers(requireActivity());
        manualViewModel.getSelectedGreen().removeObservers(requireActivity());
        manualViewModel.getSelectedBlue().removeObservers(requireActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fragmentManualBinding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        this.menu = menu;
        this.inflater = inflater;

        this.menu.clear();
        this.inflater.inflate(R.menu.menu_manual, menu);

        MenuItem itemBluetooth = menu.findItem(R.id.bluetooth);
        if (bluetoothHelper.isConnected()) {
            itemBluetooth.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_connected));
        } else {
            itemBluetooth.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_disconnected));
        }

        MenuItem itemStatus = menu.findItem(R.id.status_light);
        if (DeviceStatusService.isTurnedOn) {
            itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_on));
            itemStatus.setTitle(R.string.status_on);
            itemStatus.setVisible(true);
        } else {
            itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_off));
            itemStatus.setTitle(R.string.status_off);
            itemStatus.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.status_light:
                if (bluetoothHelper.isConnected()) {
                    MenuItem itemStatus = menu.findItem(R.id.status_light);
                    if (DeviceStatusService.isTurnedOn) {
                        itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_off));
                        itemStatus.setTitle(R.string.status_off);

                        updateDevice((byte) 0, (byte ) 0, (byte) 0);

                        DeviceStatusService.isTurnedOn = false;
                    } else {
                        DeviceStatusService.isTurnedOn = true;
                        itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_on));
                        itemStatus.setTitle(R.string.status_on);

                        updateDevice(DeviceStatusService.latestRed, DeviceStatusService.latestGreen, DeviceStatusService.latestBlue);
                    }
                }
                break;

            case R.id.bluetooth:
                if (!bluetoothHelper.isConnected()) {
                    if (!bluetoothHelper.getBluetoothAdapter().isEnabled()) {
                        Intent intentRequestEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intentRequestEnable, REQUIRE_ENABLE_BLUETOOTH);
                    } else {
                        pairAndConnectDevice();
                    }
                } else {
                    bluetoothHelper.disconnect();
                }

                break;

            case R.id.add:
                dialogSaveProfileBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_save_profile, null, false);
                dialogSaveProfileBinding.setManualFragment(this);
                dialogSaveProfileBinding.setManualViewModel(manualViewModel);

                dialogSaveProfile = new AlertDialog.Builder(requireActivity())
                        .setTitle(getString(R.string.save))
                        .setView(dialogSaveProfileBinding.getRoot())
                        .setPositiveButton(R.string.save, (dialogInterface, i) -> saveIntoLibrary())
                        .setNegativeButton(R.string.discard, (dialogInterface, i) -> dialogInterface.dismiss())
                        .create();

                dialogSaveProfile.setOnShowListener(dialogInterface -> {
                    if (dialogSaveProfileBinding.name.length() == 0) {
                        dialogSaveProfileBinding.messageName.setText(getString(R.string.field_empty));
                        dialogSaveProfileBinding.messageName.setVisibility(View.VISIBLE);

                        dialogSaveProfile.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                    }
                });

                dialogSaveProfile.show();

                break;
        }

        return true;
    }

    public void checkSyntaxDialogSaveProfile(CharSequence s, int start, int before, int count) {
        if (s.toString().matches(getString(R.string.sentence_incorrect_only_white_space))) {
            dialogSaveProfileBinding.messageName.setText(getString(R.string.field_empty));
            dialogSaveProfileBinding.messageName.setVisibility(View.VISIBLE);

            dialogSaveProfile.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
        } else if (s.toString().matches(getString(R.string.sentence_incorrect_white_space_start))) {
            dialogSaveProfileBinding.messageName.setText(R.string.field_incorrect_start);
            dialogSaveProfileBinding.messageName.setVisibility(View.VISIBLE);

            dialogSaveProfile.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
        } else if (s.toString().matches(getString(R.string.sentence_correct))) {
            dialogSaveProfileBinding.messageName.setText("");
            dialogSaveProfileBinding.messageName.setVisibility(View.INVISIBLE);

            dialogSaveProfile.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
        }
    }

    public void saveIntoLibrary() {
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

            Snackbar.make(fragmentManualBinding.getRoot(), spannableSnackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
        }).start();
    }

    private void pairAndConnectDevice() {
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String deviceSelected = sharedPreferences.getString(getString(R.string.device_selected), "");

        /* If the device is never been set, or the actual device is now not bonded, will be an AlertDialog for a selection. */
        if (deviceSelected.isEmpty() || (!deviceSelected.isEmpty() && bluetoothHelper.getBluetoothAdapter().getRemoteDevice(deviceSelected).getBondState() == 10)) {
            ArrayList<BluetoothDevice> bluetoothDevices = bluetoothHelper.getList(getString(R.string.app_name));
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

                if (bluetoothHelper.pair(selection)) {
                    bluetoothHelper.connect();
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
            if (bluetoothHelper.pair(deviceSelected)) {
                bluetoothHelper.connect();
            }
        }
    }

    public void updateDevice(byte red, byte green, byte blue) {
        if (bluetoothHelper.isConnected() && DeviceStatusService.isTurnedOn) {
            bluetoothHelper.writeData(new byte[]{red, green, blue});
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