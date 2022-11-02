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
import android.os.Bundle;
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
import it.davidepalladino.lumenio.util.BluetoothService;
import it.davidepalladino.lumenio.util.DeviceArrayAdapter;
import it.davidepalladino.lumenio.util.LibraryListAdapter;
import it.davidepalladino.lumenio.view.activity.MainActivity;
import it.davidepalladino.lumenio.view.viewModel.LibraryViewModel;

public class LibraryListFragment extends Fragment {
    private FragmentLibraryListBinding fragmentLibraryListBinding;

    private LibraryViewModel libraryViewModel;

    private Menu menu;
    private MenuInflater inflater;

    private AlertDialog dialogSelectDevice = null;

    private BluetoothService bluetoothService;

    private DeviceArrayAdapter deviceArrayAdapter = null;

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

                Snackbar.make(fragmentLibraryListBinding.getRoot(), snackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        bluetoothService = BluetoothService.getInstance(requireActivity().getSystemService(BluetoothManager.class).getAdapter(), requireContext());

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
        fragmentLibraryListBinding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        this.menu = menu;
        this.inflater = inflater;

        this.menu.clear();
        this.inflater.inflate(R.menu.menu_library_list, menu);

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
}