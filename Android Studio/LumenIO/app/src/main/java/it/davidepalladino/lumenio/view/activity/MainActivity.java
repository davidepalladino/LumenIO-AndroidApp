package it.davidepalladino.lumenio.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.ActivityMainBinding;
import it.davidepalladino.lumenio.util.BluetoothHelper;
import it.davidepalladino.lumenio.util.DeviceStatusService;
import it.davidepalladino.lumenio.util.NotificationService;
import it.davidepalladino.lumenio.view.fragment.LibraryFragment;
import it.davidepalladino.lumenio.view.fragment.ManualFragment;
import it.davidepalladino.lumenio.view.fragment.SceneFragment;

public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding activityMainBinding;

    private FragmentManager fragmentManager;
    private Fragment actualFragment;
    private ManualFragment manualFragment;
    private SceneFragment sceneFragment;
    private LibraryFragment libraryFragment;

    private Intent intentService = null;
    private NotificationService notificationService;

    private BluetoothHelper bluetoothHelper;

    private boolean isBroadcastReceiverRegistered = false;
    private boolean isNotificationServiceConnectionRegistered = false;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothHelper.ACTION_STATUS:
                    String extra = intent.getStringExtra(BluetoothHelper.EXTRA_STATE);
                    switch (extra) {
                        case BluetoothHelper.EXTRA_CONNECTED:
                            notificationService.createNotification(getString(R.string.device_connected_name) + " " + bluetoothHelper.getDeviceName(), getString(R.string.notification_click_here_return_app));
                            DeviceStatusService.isTurnedOn = true;

                            break;

                        case BluetoothHelper.EXTRA_DISCONNECTED:
                            notificationService.destroyNotification();
                            DeviceStatusService.isTurnedOn = false;

                            break;

                        case BluetoothHelper.EXTRA_ERROR:
                            DeviceStatusService.isTurnedOn = false;
                            break;
                    }

                    break;

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    if (bluetoothHelper.isConnected()) {
                        bluetoothHelper.disconnect();
                    }

                    break;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothHelper = BluetoothHelper.getInstance(getSystemService(BluetoothManager.class).getAdapter(), this);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(activityMainBinding.getRoot());
        setSupportActionBar(activityMainBinding.toolbar);

        activityMainBinding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();
            switch (itemID) {
                case R.id.bottom_navigation_manual:
                    fragmentManager.beginTransaction().detach(actualFragment).attach(manualFragment).commit();
                    actualFragment = manualFragment;

                    return true;
                case R.id.bottom_navigation_scene:
                    fragmentManager.beginTransaction().detach(actualFragment).attach(sceneFragment).commit();
                    actualFragment = sceneFragment;

                    return true;
                case R.id.bottom_navigation_library:
                    fragmentManager.beginTransaction().detach(actualFragment).attach(libraryFragment).commit();
                    actualFragment = libraryFragment;

                    return true;
            }

            return false;
        });

        fragmentManager = getSupportFragmentManager();
        manualFragment = ManualFragment.newInstance();
        sceneFragment = SceneFragment.newInstance();
        libraryFragment = LibraryFragment.newInstance();
        actualFragment = manualFragment;

        fragmentManager.beginTransaction().add(activityMainBinding.frameLayout.getId(), libraryFragment).detach(libraryFragment).commit();
        fragmentManager.beginTransaction().add(activityMainBinding.frameLayout.getId(), sceneFragment).detach(sceneFragment).commit();
        fragmentManager.beginTransaction().add(activityMainBinding.frameLayout.getId(), manualFragment).detach(sceneFragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        intentService = new Intent(this, NotificationService.class);
        startService(intentService);
    }

    @Override
    protected void onResume() {
        super.onResume();

        fragmentManager.beginTransaction().attach(manualFragment).commit();

        /*
         * Unregister the Broadcast and unbind the ServiceConnection when the App is in resume state.
         * In this case the located broadcastReceiver of active fragment will updated the Notification of bluetooth status
         *  and the DeviceStatusService's fields, sent the bytes to the device.
         */
        if (isBroadcastReceiverRegistered) {
            unregisterReceiver(broadcastReceiver);
            isBroadcastReceiverRegistered = false;
        }

        if (isNotificationServiceConnectionRegistered) {
            unbindService(notificationServiceConnection);
            isNotificationServiceConnectionRegistered = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        fragmentManager.beginTransaction().detach(actualFragment).commit();

        /*
         * Register the Broadcast and bind the ServiceConnection to receive
         *  the status of connection when the App is in pause state.
         * In this case the broadcastReceiver will updated the Notification of bluetooth status
         *  and the DeviceStatusService's fields, without send any byte to the device.
         */
        if (!isBroadcastReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothHelper.ACTION_STATUS);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            registerReceiver(broadcastReceiver, intentFilter);

            isBroadcastReceiverRegistered = true;
        }

        if (!isNotificationServiceConnectionRegistered) {
            Intent intentDatabaseService = new Intent(this, NotificationService.class);
            bindService(intentDatabaseService, notificationServiceConnection, BIND_AUTO_CREATE);

            isNotificationServiceConnectionRegistered = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intentService);
    }
}