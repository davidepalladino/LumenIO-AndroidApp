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
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.TransitionInflater;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.databinding.FragmentLibraryListBinding;
import it.davidepalladino.lumenio.util.BluetoothHelper;
import it.davidepalladino.lumenio.util.DeviceArrayAdapter;
import it.davidepalladino.lumenio.util.DeviceStatusService;
import it.davidepalladino.lumenio.util.LibraryListAdapter;
import it.davidepalladino.lumenio.util.NotificationService;
import it.davidepalladino.lumenio.view.activity.MainActivity;
import it.davidepalladino.lumenio.view.viewModel.LibraryViewModel;
import it.davidepalladino.lumenio.view.viewModel.ManualViewModel;

public class LibraryListFragment extends Fragment {
    private FragmentLibraryListBinding fragmentLibraryListBinding;

    private LibraryViewModel libraryViewModel;
    private ManualViewModel manualViewModel;

    private Menu menu;
    private MenuInflater inflater;

    private AlertDialog dialogSelectDevice = null;

    private BluetoothHelper bluetoothHelper;

    private DeviceArrayAdapter deviceArrayAdapter = null;

    private NotificationService notificationService;

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
                                snackbarMessage = getString(R.string.device_connected) + " " + bluetoothHelper.getDeviceName();

                                notificationService.createNotification(getString(R.string.device_connected_name) + " " + bluetoothHelper.getDeviceName(), getString(R.string.notification_click_here_return_app));

                                updateActonBarSubtitle(getString(R.string.on) + " " + bluetoothHelper.getDeviceName());

                                DeviceStatusService.isTurnedOn = true;

                                itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_on));
                                itemStatus.setTitle(R.string.status_on);
                                itemStatus.setVisible(true);

                                itemBluetooth.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_connected));

                                break;

                            case BluetoothHelper.EXTRA_SWITCHED:
                                snackbarMessage = getString(R.string.device_switched) + " " + bluetoothHelper.getDeviceName();

                                notificationService.destroyNotification();
                                notificationService.createNotification(getString(R.string.device_connected_name) + " " + bluetoothHelper.getDeviceName(), getString(R.string.notification_click_here_return_app));

                                updateActonBarSubtitle(getString(R.string.on) + " " + bluetoothHelper.getDeviceName());

                                updateDevice(manualViewModel.getSelectedRed().getValue().byteValue(), manualViewModel.getSelectedGreen().getValue().byteValue(), manualViewModel.getSelectedBlue().getValue().byteValue());
                                DeviceStatusService.latestRed = manualViewModel.getSelectedRed().getValue().byteValue();
                                DeviceStatusService.latestGreen = manualViewModel.getSelectedGreen().getValue().byteValue();
                                DeviceStatusService.latestBlue = manualViewModel.getSelectedBlue().getValue().byteValue();

                                break;

                            case BluetoothHelper.EXTRA_DISCONNECTED:
                                snackbarMessage = getString(R.string.device_disconnected);

                                notificationService.destroyNotification();

                                updateActonBarSubtitle("");

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
                        if (bluetoothHelper.isConnected() && !BluetoothHelper.isRequestedSwitch) {
                            bluetoothHelper.disconnect();
                        } else if (BluetoothHelper.isRequestedSwitch) {
                            BluetoothHelper.isRequestedSwitch = false;
                        }

                        break;
                }

                if (!snackbarMessage.isEmpty()) {
                    Snackbar.make(fragmentLibraryListBinding.getRoot(), snackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);
        manualViewModel = new ViewModelProvider(requireActivity()).get(ManualViewModel.class);

        bluetoothHelper = BluetoothHelper.getInstance(requireActivity().getSystemService(BluetoothManager.class).getAdapter(), requireContext());

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LibraryListAdapter adapter = new LibraryListAdapter(new LibraryListAdapter.ProfileDiff());

        Observer<List<Profile>> profileGetAllObserver = list -> {
            if (fragmentLibraryListBinding != null) {
                if (list.isEmpty()) {
                    fragmentLibraryListBinding.noEmptyListContainer.setVisibility(View.GONE);
                    fragmentLibraryListBinding.emptyListContainer.setVisibility(View.VISIBLE);
                } else {
                    fragmentLibraryListBinding.noEmptyListContainer.setVisibility(View.VISIBLE);
                    fragmentLibraryListBinding.emptyListContainer.setVisibility(View.GONE);

                    adapter.submitList(list);
                }
            }
        };

        Observer<List<Profile>> profileGetAllByNameObserver = list -> {
            if (fragmentLibraryListBinding != null) {
                if (list.isEmpty()) {
                    fragmentLibraryListBinding.list.setVisibility(View.GONE);
                    fragmentLibraryListBinding.itemNoFound.setVisibility(View.VISIBLE);
                } else {
                    fragmentLibraryListBinding.list.setVisibility(View.VISIBLE);
                    fragmentLibraryListBinding.itemNoFound.setVisibility(View.GONE);

                    adapter.submitList(list);
                }
            }
        };


        fragmentLibraryListBinding = FragmentLibraryListBinding.inflate(inflater, container, false);
        fragmentLibraryListBinding.setLifecycleOwner(this);

        fragmentLibraryListBinding.list.setLayoutManager(new LinearLayoutManager(requireActivity()));
        fragmentLibraryListBinding.list.setAdapter(adapter);

        fragmentLibraryListBinding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() != 0) {
                    libraryViewModel.getAll().removeObserver(profileGetAllObserver);
                    libraryViewModel.getAllByName(newText).observe(requireActivity(), profileGetAllByNameObserver);
                } else {
                    libraryViewModel.getAll().observe(requireActivity(), profileGetAllObserver);

                    fragmentLibraryListBinding.list.setVisibility(View.VISIBLE);
                    fragmentLibraryListBinding.itemNoFound.setVisibility(View.GONE);
                }

                return true;
            }
        });

        libraryViewModel.getAll().observe(requireActivity(), profileGetAllObserver);

        return fragmentLibraryListBinding.getRoot();
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

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        appCompatActivity.getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onPause() {
        super.onPause();

        requireActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fragmentLibraryListBinding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        this.menu = menu;
        this.inflater = inflater;

        this.menu.clear();
        this.inflater.inflate(R.menu.menu_library_list, menu);

        MenuItem itemBluetooth = menu.findItem(R.id.bluetooth);
        MenuItem itemStatus = menu.findItem(R.id.status_light);

        if (bluetoothHelper.isConnected()) {
            itemBluetooth.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_connected));
            itemStatus.setVisible(true);
        } else {
            itemBluetooth.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_disconnected));
            itemStatus.setVisible(false);
        }

        if (DeviceStatusService.isTurnedOn) {
            itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_on));
            itemStatus.setTitle(R.string.status_on);
        } else {
            itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_off));
            itemStatus.setTitle(R.string.status_off);
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

            case R.id.bluetooth_settings:
                showDialogSelectDevice();
                break;
        }

        return true;
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

    private void pairAndConnectDevice() {
        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        String deviceSelected = sharedPreferences.getString(getString(R.string.device_selected), "");

        /* If the device is never been set, or the actual device is now not bonded, will be an AlertDialog for a selection. */
        if (deviceSelected.isEmpty() || (!deviceSelected.isEmpty() && bluetoothHelper.getBluetoothAdapter().getRemoteDevice(deviceSelected).getBondState() == 10)) {
            showDialogSelectDevice();
        } else {
            if (bluetoothHelper.pair(deviceSelected)) {
                bluetoothHelper.connect();
            }
        }
    }

    private void showDialogSelectDevice() {
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

            if (bluetoothHelper.isConnected()) {
                bluetoothHelper.switchConnection(selection);
            } else {
                if (bluetoothHelper.pair(selection)) {
                    bluetoothHelper.connect();
                }
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
    }

    public void updateDevice(byte red, byte green, byte blue) {
        if (bluetoothHelper.isConnected() && DeviceStatusService.isTurnedOn) {
            bluetoothHelper.writeData(new byte[]{red, green, blue});
        }
    }

    private void updateActonBarSubtitle(String subtitle) {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setSubtitle(subtitle);
    }
}