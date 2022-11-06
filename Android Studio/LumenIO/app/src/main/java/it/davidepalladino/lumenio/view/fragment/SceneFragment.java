package it.davidepalladino.lumenio.view.fragment;

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
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.databinding.FragmentSceneBinding;
import it.davidepalladino.lumenio.util.BluetoothHelper;
import it.davidepalladino.lumenio.util.DeviceArrayAdapter;
import it.davidepalladino.lumenio.util.DeviceStatusService;
import it.davidepalladino.lumenio.util.NotificationService;
import it.davidepalladino.lumenio.view.activity.MainActivity;
import it.davidepalladino.lumenio.view.dialog.SearchProfileSceneDialog;
import it.davidepalladino.lumenio.view.viewModel.ManualViewModel;
import it.davidepalladino.lumenio.view.viewModel.SceneViewModel;

public class SceneFragment extends Fragment {
    private FragmentSceneBinding fragmentSceneBinding;

    private SceneViewModel sceneViewModel;
    private ManualViewModel manualViewModel;

    private Menu menu;
    private MenuInflater inflater;

    private AlertDialog dialogSelectDevice = null;

    private BluetoothHelper bluetoothHelper;
    private DeviceArrayAdapter deviceArrayAdapter = null;

    private NotificationService notificationService;

    private Profile profileSceneOne;
    private Profile profileSceneTwo;
    private Profile profileSceneThree;

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

                                updateDevice(DeviceStatusService.latestRed, DeviceStatusService.latestGreen, DeviceStatusService.latestBlue);

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
                    Snackbar.make(fragmentSceneBinding.getRoot(), snackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
                }

                updateInfoSceneCard(profileSceneOne, fragmentSceneBinding.nameOne, fragmentSceneBinding.previewOne, fragmentSceneBinding.valuesOne, fragmentSceneBinding.turnOnOne);
                updateInfoSceneCard(profileSceneTwo, fragmentSceneBinding.nameTwo, fragmentSceneBinding.previewTwo, fragmentSceneBinding.valuesTwo, fragmentSceneBinding.turnOnTwo);
                updateInfoSceneCard(profileSceneThree, fragmentSceneBinding.nameThree, fragmentSceneBinding.previewThree, fragmentSceneBinding.valuesThree, fragmentSceneBinding.turnOnThree);
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

    public static SceneFragment newInstance() { return new SceneFragment(); }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        sceneViewModel = new ViewModelProvider(requireActivity()).get(SceneViewModel.class);
        manualViewModel = new ViewModelProvider(requireActivity()).get(ManualViewModel.class);

        bluetoothHelper = BluetoothHelper.getInstance(requireActivity().getSystemService(BluetoothManager.class).getAdapter(), requireContext());

        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        DeviceStatusService.latestSceneLoaded = sharedPreferences.getInt(getString(R.string.latest_scene_selected), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sceneViewModel.getSceneById(1).observe(requireActivity(), scene -> {
            if (scene != null) {
                sceneViewModel.getProfileById(scene.profileId).observe(requireActivity(), profile -> {
                    profileSceneOne = profile;
                    if (profile == null) {
                        sceneViewModel.getProfileById(scene.profileId).removeObservers(requireActivity());
                    } else {
                        if (DeviceStatusService.latestSceneLoaded == scene.id) {
                            DeviceStatusService.latestRed = (byte) profile.red;
                            DeviceStatusService.latestGreen = (byte) profile.green;
                            DeviceStatusService.latestBlue = (byte) profile.blue;
                        }
                    }

                    updateInfoSceneCard(profile, fragmentSceneBinding.nameOne, fragmentSceneBinding.previewOne, fragmentSceneBinding.valuesOne, fragmentSceneBinding.turnOnOne);
                });
            } else {
                updateInfoSceneCard(null, fragmentSceneBinding.nameOne, fragmentSceneBinding.previewOne, fragmentSceneBinding.valuesOne, fragmentSceneBinding.turnOnOne);
            }
        });

        sceneViewModel.getSceneById(2).observe(requireActivity(), scene -> {
            if (scene != null) {
                sceneViewModel.getProfileById(scene.profileId).observe(requireActivity(), profile -> {
                    profileSceneTwo = profile;
                    if (profile == null) {
                        sceneViewModel.getProfileById(scene.profileId).removeObservers(requireActivity());
                    } else {
                        if (DeviceStatusService.latestSceneLoaded == scene.id) {
                            DeviceStatusService.latestRed = (byte) profile.red;
                            DeviceStatusService.latestGreen = (byte) profile.green;
                            DeviceStatusService.latestBlue = (byte) profile.blue;
                        }
                    }

                    updateInfoSceneCard(profile, fragmentSceneBinding.nameTwo, fragmentSceneBinding.previewTwo, fragmentSceneBinding.valuesTwo, fragmentSceneBinding.turnOnTwo);
                });
            } else {
                updateInfoSceneCard(null, fragmentSceneBinding.nameTwo, fragmentSceneBinding.previewTwo, fragmentSceneBinding.valuesTwo, fragmentSceneBinding.turnOnTwo);
            }
        });

        sceneViewModel.getSceneById(3).observe(requireActivity(), scene -> {
            if (scene != null) {
                sceneViewModel.getProfileById(scene.profileId).observe(requireActivity(), profile -> {
                    profileSceneThree = profile;
                    if (profile == null) {
                        sceneViewModel.getProfileById(scene.profileId).removeObservers(requireActivity());
                    } else {
                        if (DeviceStatusService.latestSceneLoaded == scene.id) {
                            DeviceStatusService.latestRed = (byte) profile.red;
                            DeviceStatusService.latestGreen = (byte) profile.green;
                            DeviceStatusService.latestBlue = (byte) profile.blue;
                        }
                    }

                    updateInfoSceneCard(profile, fragmentSceneBinding.nameThree, fragmentSceneBinding.previewThree, fragmentSceneBinding.valuesThree, fragmentSceneBinding.turnOnThree);
                });
            } else {
                updateInfoSceneCard(null, fragmentSceneBinding.nameThree, fragmentSceneBinding.previewThree, fragmentSceneBinding.valuesThree, fragmentSceneBinding.turnOnThree);
            }
        });

        fragmentSceneBinding = FragmentSceneBinding.inflate(inflater, container, false);
        fragmentSceneBinding.setLifecycleOwner(this);

        fragmentSceneBinding.setSceneViewModel(sceneViewModel);
        fragmentSceneBinding.setSceneFragment(this);

        fragmentSceneBinding.turnOnOne.setOnClickListener(v -> {
            updateDevice((byte) profileSceneOne.red, (byte) profileSceneOne.green, (byte) profileSceneOne.blue);

            DeviceStatusService.latestRed = (byte) profileSceneOne.red;
            DeviceStatusService.latestGreen = (byte) profileSceneOne.green;
            DeviceStatusService.latestBlue = (byte) profileSceneOne.blue;

            updateStatusSceneCards(DeviceStatusService.latestSceneLoaded, DeviceStatusService.latestSceneLoaded = 1);

            SharedPreferences.Editor sharedPreferencesEditor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
            sharedPreferencesEditor.putInt(getString(R.string.latest_scene_selected), DeviceStatusService.latestSceneLoaded);
            sharedPreferencesEditor.apply();

            sharedPreferencesEditor.putLong(getString(R.string.latest_profile_selected), profileSceneOne.id);
            sharedPreferencesEditor.apply();

            new Thread(() -> manualViewModel.loadByID(profileSceneOne.id)).start();
        });

        fragmentSceneBinding.turnOnTwo.setOnClickListener(v -> {
            updateDevice((byte) profileSceneTwo.red, (byte) profileSceneTwo.green, (byte) profileSceneTwo.blue);

            DeviceStatusService.latestRed = (byte) profileSceneTwo.red;
            DeviceStatusService.latestGreen = (byte) profileSceneTwo.green;
            DeviceStatusService.latestBlue = (byte) profileSceneTwo.blue;

            updateStatusSceneCards(DeviceStatusService.latestSceneLoaded, DeviceStatusService.latestSceneLoaded = 2);

            SharedPreferences.Editor sharedPreferencesEditor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
            sharedPreferencesEditor.putInt(getString(R.string.latest_scene_selected), DeviceStatusService.latestSceneLoaded);
            sharedPreferencesEditor.apply();

            sharedPreferencesEditor.putLong(getString(R.string.latest_profile_selected), profileSceneTwo.id);
            sharedPreferencesEditor.apply();

            new Thread(() -> manualViewModel.loadByID(profileSceneTwo.id)).start();
        });

        fragmentSceneBinding.turnOnThree.setOnClickListener(v -> {
            updateDevice((byte) profileSceneTwo.red, (byte) profileSceneTwo.green, (byte) profileSceneTwo.blue);

            DeviceStatusService.latestRed = (byte) profileSceneThree.red;
            DeviceStatusService.latestGreen = (byte) profileSceneThree.green;
            DeviceStatusService.latestBlue = (byte) profileSceneThree.blue;

            updateStatusSceneCards(DeviceStatusService.latestSceneLoaded, DeviceStatusService.latestSceneLoaded = 3);

            SharedPreferences.Editor sharedPreferencesEditor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
            sharedPreferencesEditor.putInt(getString(R.string.latest_scene_selected), DeviceStatusService.latestSceneLoaded);
            sharedPreferencesEditor.apply();

            sharedPreferencesEditor.putLong(getString(R.string.latest_profile_selected), profileSceneThree.id);
            sharedPreferencesEditor.apply();

            new Thread(() -> manualViewModel.loadByID(profileSceneThree.id)).start();
        });

        fragmentSceneBinding.editOne.setOnClickListener(v -> {
            SearchProfileSceneDialog searchProfilesDialog = new SearchProfileSceneDialog(getView(), 1);
            searchProfilesDialog.show(getParentFragmentManager(), SceneFragment.class.getSimpleName());
        });

        fragmentSceneBinding.editTwo.setOnClickListener(v -> {
            SearchProfileSceneDialog searchProfilesDialog = new SearchProfileSceneDialog(getView(), 2);
            searchProfilesDialog.show(getParentFragmentManager(), SceneFragment.class.getSimpleName());
        });

        fragmentSceneBinding.editThree.setOnClickListener(v -> {
            SearchProfileSceneDialog searchProfilesDialog = new SearchProfileSceneDialog(getView(), 3);
            searchProfilesDialog.show(getParentFragmentManager(), SceneFragment.class.getSimpleName());
        });

        updateStatusSceneCards(DeviceStatusService.latestSceneLoaded, DeviceStatusService.latestSceneLoaded);

        return fragmentSceneBinding.getRoot();
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
        fragmentSceneBinding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        this.menu = menu;
        this.inflater = inflater;

        this.menu.clear();
        this.inflater.inflate(R.menu.menu_scene, menu);

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
        }

        return true;
    }

    private void updateInfoSceneCard(Profile profile, TextView name, View preview, TextView values, ImageView turnOn) {
        if (profile != null)  {
            name.setText(profile.name);

            values.setText(
                    String.format("%03d", profile.red) + " " +
                    String.format("%03d", profile.green) + " " +
                    String.format("%03d", profile.blue)
            );
            values.setVisibility(View.VISIBLE);

            preview.setBackground(new ColorDrawable(Color.rgb(profile.red, profile.green, profile.blue)));
            preview.setVisibility(View.VISIBLE);

            if (bluetoothHelper.isConnected()) {
                turnOn.setVisibility(View.VISIBLE);
            } else {
                turnOn.setVisibility(View.GONE);
            }
        } else {
            String notSet = getString(R.string.not_set);
            SpannableString spannableNameNotSet = new SpannableString(notSet);
            spannableNameNotSet.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.ITALIC)), 0, notSet.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            name.setText(notSet);
            values.setVisibility(View.GONE);

            preview.setBackground(new ColorDrawable(Color.argb(0, 0, 0,0)));
            preview.setVisibility(View.GONE);

            turnOn.setVisibility(View.GONE);
        }
    }

    private void updateStatusSceneCards(int oldScene, int newScene) {
        switch (oldScene) {
            case 1:
                fragmentSceneBinding.statusSceneOne.setBackground(ContextCompat.getDrawable(requireContext(), R.color.footer_card_color_background_scene_unselected));
                break;
            case 2:
                fragmentSceneBinding.statusSceneTwo.setBackground(ContextCompat.getDrawable(requireContext(), R.color.footer_card_color_background_scene_unselected));
                break;
            case 3:
                fragmentSceneBinding.statusSceneThree.setBackground(ContextCompat.getDrawable(requireContext(), R.color.footer_card_color_background_scene_unselected));
                break;
        }

        switch (newScene) {
            case 1:
                fragmentSceneBinding.statusSceneOne.setBackground(ContextCompat.getDrawable(requireContext(), R.color.footer_card_color_background_scene_selected));
                break;
            case 2:
                fragmentSceneBinding.statusSceneTwo.setBackground(ContextCompat.getDrawable(requireContext(), R.color.footer_card_color_background_scene_selected));
                break;
            case 3:
                fragmentSceneBinding.statusSceneThree.setBackground(ContextCompat.getDrawable(requireContext(), R.color.footer_card_color_background_scene_selected));
                break;
        }
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
        if (bluetoothHelper.isConnected()) {
            bluetoothHelper.writeData(new byte[]{red, green, blue});
        } else {
            Snackbar.make(fragmentSceneBinding.getRoot(), R.string.request_connection_execute_action, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
        }
    }
}