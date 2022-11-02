package it.davidepalladino.lumenio.view.fragment;

import static it.davidepalladino.lumenio.util.BluetoothService.REQUIRE_ENABLE_BLUETOOTH;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.databinding.FragmentSceneBinding;
import it.davidepalladino.lumenio.util.BluetoothService;
import it.davidepalladino.lumenio.util.DeviceArrayAdapter;
import it.davidepalladino.lumenio.view.activity.MainActivity;
import it.davidepalladino.lumenio.view.dialog.SearchProfileSceneDialog;
import it.davidepalladino.lumenio.view.viewModel.SceneViewModel;

public class SceneFragment extends Fragment {
    private FragmentSceneBinding fragmentSceneBinding;
    private SceneViewModel sceneViewModel;

    private Menu menu;
    private MenuInflater inflater;

    private AlertDialog dialogSelectDevice = null;

    private BluetoothService bluetoothService;
    private DeviceArrayAdapter deviceArrayAdapter = null;

    private Profile profileSceneOne;
    private Profile profileSceneTwo;
    private Profile profileSceneThree;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (menu != null) {
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
                    case BluetoothService.STATUS_LOST:
                        snackbarMessage = getString(R.string.device_lost);
                        menu.findItem(R.id.bluetooth).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_disconnected));
                        break;
                    case BluetoothService.STATUS_ERROR:
                        snackbarMessage = getString(R.string.device_error);
                        menu.findItem(R.id.bluetooth).setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_disconnected));
                        break;
                }

                Snackbar.make(fragmentSceneBinding.getRoot(), snackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
            }
        }
    };

    public static SceneFragment newInstance() {
        return new SceneFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        sceneViewModel = new ViewModelProvider(requireActivity()).get(SceneViewModel.class);

        bluetoothService = BluetoothService.getInstance(requireActivity().getSystemService(BluetoothManager.class).getAdapter(), requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sceneViewModel.getSceneById(1).observe(requireActivity(), scene -> {
            if (scene != null) {
                sceneViewModel.getProfileById(scene.profileId).observe(requireActivity(), profile -> {
                    profileSceneOne = profile;
                    if (profile == null) {
                        sceneViewModel.getProfileById(scene.profileId).removeObservers(requireActivity());
                    }

                    updateSceneCard(profile, fragmentSceneBinding.nameOne, fragmentSceneBinding.previewOne, fragmentSceneBinding.valuesOne, fragmentSceneBinding.turnOnOne, fragmentSceneBinding.uploadOne);
                });
            } else {
                updateSceneCard(null, fragmentSceneBinding.nameOne, fragmentSceneBinding.previewOne, fragmentSceneBinding.valuesOne, fragmentSceneBinding.turnOnOne, fragmentSceneBinding.uploadOne);
            }
        });

        sceneViewModel.getSceneById(2).observe(requireActivity(), scene -> {
            if (scene != null) {
                sceneViewModel.getProfileById(scene.profileId).observe(requireActivity(), profile -> {
                    profileSceneTwo = profile;
                    if (profile == null) {
                        sceneViewModel.getProfileById(scene.profileId).removeObservers(requireActivity());
                    }

                    updateSceneCard(profile, fragmentSceneBinding.nameTwo, fragmentSceneBinding.previewTwo, fragmentSceneBinding.valuesTwo, fragmentSceneBinding.turnOnTwo, fragmentSceneBinding.uploadTwo);
                });
            } else {
                updateSceneCard(null, fragmentSceneBinding.nameTwo, fragmentSceneBinding.previewTwo, fragmentSceneBinding.valuesTwo, fragmentSceneBinding.turnOnTwo, fragmentSceneBinding.uploadTwo);
            }
        });

        sceneViewModel.getSceneById(3).observe(requireActivity(), scene -> {
            if (scene != null) {
                sceneViewModel.getProfileById(scene.profileId).observe(requireActivity(), profile -> {
                    profileSceneThree = profile;
                    if (profile == null) {
                        sceneViewModel.getProfileById(scene.profileId).removeObservers(requireActivity());
                    }

                    updateSceneCard(profile, fragmentSceneBinding.nameThree, fragmentSceneBinding.previewThree, fragmentSceneBinding.valuesThree, fragmentSceneBinding.turnOnThree, fragmentSceneBinding.uploadThree);
                });
            } else {
                updateSceneCard(null, fragmentSceneBinding.nameThree, fragmentSceneBinding.previewThree, fragmentSceneBinding.valuesThree, fragmentSceneBinding.turnOnThree, fragmentSceneBinding.uploadThree);
            }
        });

        fragmentSceneBinding = FragmentSceneBinding.inflate(inflater, container, false);
        fragmentSceneBinding.setLifecycleOwner(this);

        fragmentSceneBinding.setSceneViewModel(sceneViewModel);
        fragmentSceneBinding.setSceneFragment(this);

        fragmentSceneBinding.turnOnOne.setOnClickListener(v -> {
            turnOnDevice(1, profileSceneOne);
        });

        fragmentSceneBinding.turnOnTwo.setOnClickListener(v -> {
            turnOnDevice(2, profileSceneTwo);
        });

        fragmentSceneBinding.turnOnThree.setOnClickListener(v -> {
            turnOnDevice(3, profileSceneThree);
        });

        fragmentSceneBinding.uploadOne.setOnClickListener(v -> {
            uploadDevice(1, profileSceneOne);
        });

        fragmentSceneBinding.uploadTwo.setOnClickListener(v -> {
            uploadDevice(2, profileSceneTwo);
        });

        fragmentSceneBinding.uploadThree.setOnClickListener(v -> {
            uploadDevice(3, profileSceneThree);
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

        return fragmentSceneBinding.getRoot();
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
                    bluetoothService.disconnect();
                }

                break;
        }

        return true;
    }

    private void updateSceneCard(Profile profile, TextView name, View preview, TextView values, ImageView turnOn, ImageView transfer) {
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

            turnOn.setVisibility(View.VISIBLE);
            transfer.setVisibility(View.VISIBLE);
        } else {
            String notSet = getString(R.string.not_set);
            SpannableString spannableNameNotSet = new SpannableString(notSet);
            spannableNameNotSet.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.ITALIC)), 0, notSet.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            name.setText(notSet);
            values.setVisibility(View.GONE);

            preview.setBackground(new ColorDrawable(Color.argb(0, 0, 0,0)));
            preview.setVisibility(View.GONE);

            turnOn.setVisibility(View.INVISIBLE);
            transfer.setVisibility(View.INVISIBLE);
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
                    bluetoothService.connect();
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
                bluetoothService.connect();
            }
        }
    }

    public void turnOnDevice(int sceneID, Profile profile) {
        if (bluetoothService.isConnected()) {
            bluetoothService.writeData(new byte[]{(byte) profile.red, (byte) profile.green, (byte) profile.blue});
        } else {
            Snackbar.make(fragmentSceneBinding.getRoot(), R.string.request_connection_execute_action, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
        }
    }

    public void uploadDevice(int sceneID, Profile profile) {
        if (bluetoothService.isConnected()) {
            String json = "";
            try {
                json = new JSONObject()
                        .put(getString(R.string.request), 1)
                        .put(getString(R.string.values), new JSONObject()
                                .put(getString(R.string.red), profile.red)
                                .put(getString(R.string.green), profile.green)
                                .put(getString(R.string.blue), profile.blue)
                        )
                        .toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            bluetoothService.writeData(requireContext(), json);
        } else {
            Snackbar.make(fragmentSceneBinding.getRoot(), R.string.request_connection_execute_action, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
        }
    }
}