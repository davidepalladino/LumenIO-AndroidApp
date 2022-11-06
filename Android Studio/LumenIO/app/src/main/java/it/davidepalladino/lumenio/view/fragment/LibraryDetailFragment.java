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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionInflater;

import com.google.android.material.snackbar.Snackbar;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Scene;
import it.davidepalladino.lumenio.databinding.FragmentLibraryDetailBinding;
import it.davidepalladino.lumenio.util.BluetoothHelper;
import it.davidepalladino.lumenio.util.DeviceArrayAdapter;
import it.davidepalladino.lumenio.util.DeviceStatusService;
import it.davidepalladino.lumenio.util.NotificationService;
import it.davidepalladino.lumenio.view.activity.MainActivity;
import it.davidepalladino.lumenio.view.viewModel.ManualViewModel;
import it.davidepalladino.lumenio.view.viewModel.LibraryViewModel;
import it.davidepalladino.lumenio.view.viewModel.SceneViewModel;

public class LibraryDetailFragment extends Fragment {
    public static final String BUNDLE_PROFILE_ID = "PROFILE_ID";

    private FragmentLibraryDetailBinding fragmentLibraryDetailBinding;

    private LibraryViewModel libraryViewModel;
    private SceneViewModel sceneViewModel;
    private ManualViewModel manualViewModel;

    private Menu menu;
    private MenuInflater inflater;

    private AlertDialog dialogSelectDevice = null;

    private boolean selectedByUser = false;

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
                                snackbarMessage = getString(R.string.device_connected);

                                notificationService.createNotification(getString(R.string.device_connected_name) + " " + bluetoothHelper.getDeviceName(), getString(R.string.notification_click_here_return_app));

                                DeviceStatusService.isTurnedOn = true;

                                itemStatus.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_status_on));
                                itemStatus.setTitle(R.string.status_on);
                                itemStatus.setVisible(true);

                                itemBluetooth.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_round_bluetooth_connected));

                                updateDevice(libraryViewModel.getSelectedRed().getValue().byteValue(), libraryViewModel.getSelectedGreen().getValue().byteValue(), libraryViewModel.getSelectedBlue().getValue().byteValue());

                                DeviceStatusService.latestRed = libraryViewModel.getSelectedRed().getValue().byteValue();
                                DeviceStatusService.latestGreen = libraryViewModel.getSelectedGreen().getValue().byteValue();
                                DeviceStatusService.latestBlue = libraryViewModel.getSelectedBlue().getValue().byteValue();

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
                    Snackbar.make(fragmentLibraryDetailBinding.getRoot(), snackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
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
        sceneViewModel = new ViewModelProvider(requireActivity()).get(SceneViewModel.class);
        manualViewModel = new ViewModelProvider(requireActivity()).get(ManualViewModel.class);

        bluetoothHelper = BluetoothHelper.getInstance(requireActivity().getSystemService(BluetoothManager.class).getAdapter(), requireContext());

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.slide_left));

        new Thread(() -> libraryViewModel.loadByID(getArguments().getLong(BUNDLE_PROFILE_ID))).start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentLibraryDetailBinding = FragmentLibraryDetailBinding.inflate(inflater, container, false);
        fragmentLibraryDetailBinding.setLifecycleOwner(this);
        fragmentLibraryDetailBinding.setButtonsHandler(new ButtonsHandler());

        fragmentLibraryDetailBinding.setLibraryViewModel(libraryViewModel);

        fragmentLibraryDetailBinding.colorPicker.attachBrightnessSlider(fragmentLibraryDetailBinding.brightnessSlide);
        fragmentLibraryDetailBinding.colorPicker.setOnTouchListener((view, motionEvent) -> {
            motionEvent.getAction();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    fragmentLibraryDetailBinding.scrollView.setIsScrollable(false);
                    selectedByUser = true;
                    break;
                case MotionEvent.ACTION_UP:
                    fragmentLibraryDetailBinding.scrollView.setIsScrollable(true);
                    selectedByUser = false;
                    break;
            }

            return false;
        });
        fragmentLibraryDetailBinding.colorPicker.setColorListener((ColorEnvelopeListener) (envelope, fromUser) -> {
            if (fromUser) {
                libraryViewModel.setSelectedHex(envelope.getHexCode().substring(2, 8));
                libraryViewModel.setSelectedRed(envelope.getArgb()[1]);
                libraryViewModel.setSelectedGreen(envelope.getArgb()[2]);
                libraryViewModel.setSelectedBlue(envelope.getArgb()[3]);
            }
        });

        fragmentLibraryDetailBinding.brightnessSlide.setOnTouchListener((view, motionEvent) -> {
            motionEvent.getAction();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    fragmentLibraryDetailBinding.scrollView.setIsScrollable(false);
                    selectedByUser = true;
                    break;
                case MotionEvent.ACTION_UP:
                    fragmentLibraryDetailBinding.scrollView.setIsScrollable(true);
                    selectedByUser = false;
                    break;
            }

            return false;
        });

        return fragmentLibraryDetailBinding.getRoot();
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

        libraryViewModel.getSelectedRed().observe(requireActivity(), integer -> {
            if (selectedByUser) {
                try {
                    fragmentLibraryDetailBinding.colorPicker.selectByHsvColor(Color.rgb(libraryViewModel.getSelectedRed().getValue(), libraryViewModel.getSelectedGreen().getValue(), libraryViewModel.getSelectedBlue().getValue()));
                } catch (IllegalAccessException e) { e.printStackTrace(); }
            } else {
                fragmentLibraryDetailBinding.colorPicker.setInitialColor(Color.rgb(libraryViewModel.getSelectedRed().getValue(), libraryViewModel.getSelectedGreen().getValue(), libraryViewModel.getSelectedBlue().getValue()));
            }

            updateDevice(libraryViewModel.getSelectedRed().getValue().byteValue(), libraryViewModel.getSelectedGreen().getValue().byteValue(), libraryViewModel.getSelectedBlue().getValue().byteValue());

            DeviceStatusService.latestRed = libraryViewModel.getSelectedRed().getValue().byteValue();
            DeviceStatusService.latestGreen = libraryViewModel.getSelectedGreen().getValue().byteValue();
            DeviceStatusService.latestBlue = libraryViewModel.getSelectedBlue().getValue().byteValue();
        });

        libraryViewModel.getSelectedGreen().observe(requireActivity(), integer -> {
            if (selectedByUser) {
                try {
                    fragmentLibraryDetailBinding.colorPicker.selectByHsvColor(Color.rgb(libraryViewModel.getSelectedRed().getValue(), libraryViewModel.getSelectedGreen().getValue(), libraryViewModel.getSelectedBlue().getValue()));
                } catch (IllegalAccessException e) { e.printStackTrace(); }
            } else {
                fragmentLibraryDetailBinding.colorPicker.setInitialColor(Color.rgb(libraryViewModel.getSelectedRed().getValue(), libraryViewModel.getSelectedGreen().getValue(), libraryViewModel.getSelectedBlue().getValue()));
            }

            updateDevice(libraryViewModel.getSelectedRed().getValue().byteValue(), libraryViewModel.getSelectedGreen().getValue().byteValue(), libraryViewModel.getSelectedBlue().getValue().byteValue());

            DeviceStatusService.latestRed = libraryViewModel.getSelectedRed().getValue().byteValue();
            DeviceStatusService.latestGreen = libraryViewModel.getSelectedGreen().getValue().byteValue();
            DeviceStatusService.latestBlue = libraryViewModel.getSelectedBlue().getValue().byteValue();
        });

        libraryViewModel.getSelectedBlue().observe(requireActivity(), integer -> {
            if (selectedByUser) {
                try {
                    fragmentLibraryDetailBinding.colorPicker.selectByHsvColor(Color.rgb(libraryViewModel.getSelectedRed().getValue(), libraryViewModel.getSelectedGreen().getValue(), libraryViewModel.getSelectedBlue().getValue()));
                } catch (IllegalAccessException e) { e.printStackTrace(); }
            } else {
                fragmentLibraryDetailBinding.colorPicker.setInitialColor(Color.rgb(libraryViewModel.getSelectedRed().getValue(), libraryViewModel.getSelectedGreen().getValue(), libraryViewModel.getSelectedBlue().getValue()));
            }

            updateDevice(libraryViewModel.getSelectedRed().getValue().byteValue(), libraryViewModel.getSelectedGreen().getValue().byteValue(), libraryViewModel.getSelectedBlue().getValue().byteValue());

            DeviceStatusService.latestRed = libraryViewModel.getSelectedRed().getValue().byteValue();
            DeviceStatusService.latestGreen = libraryViewModel.getSelectedGreen().getValue().byteValue();
            DeviceStatusService.latestBlue = libraryViewModel.getSelectedBlue().getValue().byteValue();
        });

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        Objects.requireNonNull(appCompatActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        appCompatActivity.getSupportActionBar().setTitle(R.string.detail);
    }

    @Override
    public void onPause() {
        super.onPause();

        requireActivity().unregisterReceiver(broadcastReceiver);

        libraryViewModel.getSelectedRed().removeObservers(requireActivity());
        libraryViewModel.getSelectedGreen().removeObservers(requireActivity());
        libraryViewModel.getSelectedBlue().removeObservers(requireActivity());

        NavHostFragment.findNavController(this).navigateUp();

        disableEditMode();

        new Thread(() -> libraryViewModel.reload()).start();

        updateDevice(manualViewModel.getSelectedRed().getValue().byteValue(), manualViewModel.getSelectedGreen().getValue().byteValue(), manualViewModel.getSelectedBlue().getValue().byteValue());

        DeviceStatusService.latestRed = manualViewModel.getSelectedRed().getValue().byteValue();
        DeviceStatusService.latestGreen = manualViewModel.getSelectedGreen().getValue().byteValue();
        DeviceStatusService.latestBlue = manualViewModel.getSelectedBlue().getValue().byteValue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fragmentLibraryDetailBinding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        this.menu = menu;
        this.inflater = inflater;

        this.menu.clear();
        this.inflater.inflate(R.menu.menu_library_detail_no_edit, this.menu);

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
            case android.R.id.home:
                requireActivity().onBackPressed();
                break;

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

            case R.id.edit:
                enableEditMode();

                this.menu.clear();
                this.inflater.inflate(R.menu.menu_library_detail_edit, this.menu);

                break;

            case R.id.discard:
                disableEditMode();

                new Thread(() -> libraryViewModel.reload()).start();

                this.menu.clear();
                this.inflater.inflate(R.menu.menu_library_detail_no_edit, this.menu);

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

                break;

            case R.id.accept:
                updateProfile();
                disableEditMode();

                this.menu.clear();
                this.inflater.inflate(R.menu.menu_library_detail_no_edit, this.menu);

                break;

            case R.id.remove:
                showRemoveDialog();

                break;
        }

        return true;
    }

    public void checkSyntaxName(CharSequence s, int start, int before, int count) {
        if (s.toString().matches(getString(R.string.sentence_incorrect_only_white_space))) {
            fragmentLibraryDetailBinding.messageName.setText(getString(R.string.field_empty));
            fragmentLibraryDetailBinding.messageName.setVisibility(View.VISIBLE);
        } else if (s.toString().matches(getString(R.string.sentence_incorrect_white_space_start))) {
            fragmentLibraryDetailBinding.messageName.setText(R.string.field_incorrect_start);
            fragmentLibraryDetailBinding.messageName.setVisibility(View.VISIBLE);
        } else if (s.toString().matches(getString(R.string.sentence_correct))) {
            fragmentLibraryDetailBinding.messageName.setText("");
            fragmentLibraryDetailBinding.messageName.setVisibility(View.GONE);
        }
    }

    private void showRemoveDialog() {
        String dialogMessage = getString(R.string.profile_question_pre) + " " + fragmentLibraryDetailBinding.nameTextView.getText().toString() + " " + getString(R.string.profile_question_post);
        SpannableString spannableDialogMessage = new SpannableString(dialogMessage);
        spannableDialogMessage.setSpan(
                new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)),
                getString(R.string.profile_question_pre).length() + 1,
                getString(R.string.profile_question_pre).length() + 1 + fragmentLibraryDetailBinding.nameTextView.getText().toString().length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
        alertDialog
                .setMessage(spannableDialogMessage)
                .setPositiveButton(getString(R.string.remove), (dialog, idDialog) -> {
                    deleteProfile();
                    requireActivity().onBackPressed();
                })
                .setNegativeButton(getString(R.string.discard), (dialog, idDialog) -> {
                })
                .create()
                .show();
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

    public boolean updateDevice(byte red, byte green, byte blue) {
        if (bluetoothHelper.isConnected() && DeviceStatusService.isTurnedOn) {
            bluetoothHelper.writeData(new byte[]{red, green, blue});
            return true;
        }

        return false;
    }

    private void updateProfile() {
        new Thread(() -> {
            String snackbarMessage = "";

            try {
                libraryViewModel.updateValues();
                libraryViewModel.reload();

                if (Objects.requireNonNull(libraryViewModel.getSelectedID().getValue()).longValue() == Objects.requireNonNull(manualViewModel.getSelectedID().getValue()).longValue()) {
                    manualViewModel.reload();
                }

                snackbarMessage = libraryViewModel.getSelectedName().getValue() + " " + getString(R.string.profile_updated);
            } catch (SQLiteConstraintException e) {
                snackbarMessage = libraryViewModel.getSelectedName().getValue() + " " + getString(R.string.profile_not_updated_for_name);
            }

            SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
            spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, libraryViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            Snackbar.make(fragmentLibraryDetailBinding.getRoot(), spannableSnackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
        }).start();
    }

    private void deleteProfile() {
        new Thread(() -> {
            try {
                String snackbarMessage = "";

                libraryViewModel.delete();
                if (libraryViewModel.getSelectedID().getValue() == (manualViewModel.getSelectedID().getValue())) {
                    manualViewModel.loadByID(0);
                }

                snackbarMessage = libraryViewModel.getSelectedName().getValue() + " " + getString(R.string.profile_removed);
                SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
                spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, libraryViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                Snackbar.make(fragmentLibraryDetailBinding.getRoot(), spannableSnackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
            } catch (SQLiteConstraintException ignored) { }
        }).start();

        /* Checking if this profile is used for some scene. If so, the scene will be removed. */
        sceneViewModel.getScenesAll().observe(requireActivity(), scenes -> {
            Iterator iteratorScenesAll = scenes.iterator();
            while (iteratorScenesAll.hasNext()) {
                Scene scene = (Scene) iteratorScenesAll.next();
                if (scene.profileId == libraryViewModel.getSelectedID().getValue()) {
                    new Thread(() -> {
                        sceneViewModel.deleteScene(scene);
                    }).start();
                    break;
                }
            }
        });
    }

    private void enableEditMode() {
        fragmentLibraryDetailBinding.controlCard.setVisibility(View.VISIBLE);
        fragmentLibraryDetailBinding.nameEditText.setVisibility(View.VISIBLE);
        fragmentLibraryDetailBinding.nameTextView.setVisibility(View.GONE);
        fragmentLibraryDetailBinding.messageName.setVisibility(View.VISIBLE);

        fragmentLibraryDetailBinding.colorPicker.setInitialColor(Color.rgb(libraryViewModel.getSelectedRed().getValue(), libraryViewModel.getSelectedGreen().getValue(), libraryViewModel.getSelectedBlue().getValue()));
    }

    public void disableEditMode() {
        fragmentLibraryDetailBinding.controlCard.setVisibility(View.GONE);
        fragmentLibraryDetailBinding.nameEditText.setVisibility(View.GONE);
        fragmentLibraryDetailBinding.nameTextView.setVisibility(View.VISIBLE);
        fragmentLibraryDetailBinding.messageName.setVisibility(View.GONE);

        /* Disabling the keyboard. */
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
    }

    public class ButtonsHandler {
        public void controlIt(View v) {
            new Thread(() -> {
                SharedPreferences.Editor sharedPreferencesEditor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
                sharedPreferencesEditor.putLong(getString(R.string.latest_profile_selected), libraryViewModel.getSelectedID().getValue());
                sharedPreferencesEditor.apply();

                libraryViewModel.updateUse();
                libraryViewModel.reload();
                manualViewModel.loadByID(libraryViewModel.getSelectedID().getValue());

                updateDevice(libraryViewModel.getSelectedRed().getValue().byteValue(), libraryViewModel.getSelectedGreen().getValue().byteValue(), libraryViewModel.getSelectedBlue().getValue().byteValue());

                DeviceStatusService.latestRed = libraryViewModel.getSelectedRed().getValue().byteValue();
                DeviceStatusService.latestGreen = libraryViewModel.getSelectedGreen().getValue().byteValue();
                DeviceStatusService.latestBlue = libraryViewModel.getSelectedBlue().getValue().byteValue();

                Snackbar.make(fragmentLibraryDetailBinding.getRoot(), getString(R.string.profile_loaded), 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
            }).start();
        }

        public void setAsScene(View v) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
            alertDialog
                .setTitle("Select a scene slot")
                .setItems(R.array.scenes_array, (dialog, which) -> {
                    AtomicReference<String> snackbarMessage = new AtomicReference<>("");

                    new Thread(() -> {
                        snackbarMessage.set(libraryViewModel.getSelectedName().getValue() + " ");

                        try {
                            Scene scene = new Scene(which + 1, libraryViewModel.getSelectedID().getValue());
                            sceneViewModel.updateScene(scene);

                            snackbarMessage.set(snackbarMessage.get() + getString(R.string.scene_saved) + " " + scene.id + ".");
                        } catch (Exception e) {
                            snackbarMessage.set(snackbarMessage.get() + getString(R.string.scene_not_saved_for_name));
                        }

                        SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage.toString());
                        spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, libraryViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Snackbar.make(fragmentLibraryDetailBinding.getRoot(), spannableSnackbarMessage, 5000).setAnchorView(((MainActivity) requireActivity()).activityMainBinding.bottomNavigation).show();
                    }).start();
                })
                .create()
                .show();
        }
    }
}