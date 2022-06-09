package it.davidepalladino.lumenio.view;

import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(MainActivity.this);
        binding.viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Control");
                    tab.setIcon(R.drawable.ic_round_control_tab);
                    break;
//                    case 1:
//                        tab.setIcon(R.drawable.ic_round_scene);
//                        tab.setText("Scene");
//                        break;
                case 1: // TODO: Replace with 2 during the implementation of Scene.
                    tab.setIcon(R.drawable.ic_round_library_tab);
                    tab.setText("Library");
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        switch (binding.tabLayout.getSelectedTabPosition()) {
            case 0:
                super.onBackPressed();
                break;
//            case 1:
//                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
//                break;
            case 1: // TODO: Replace with 2 during the implementation of Scene.
                /* Checking if in this Tab the current destination is an Home or not, to execute the right command. */
                if (Navigation.findNavController(this, R.id.nav_host_fragment_content_library).getCurrentDestination().getId() != R.id.LibraryListFragment) {
                    super.onBackPressed();
                } else {
                    binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
                }
                break;
            default:
                super.onBackPressed();
                break;
        }
    }

    public static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) { super(fragmentActivity);}

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ControlFragment();
//                case 1:
//                    return new SceneFragment();
                case 1: // TODO: Replace with 2 during the implementation of Scene.
                    return new LibraryFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 2;   // TODO: Replace with 3 during the implementation of Scene.
        }
    }
}