package it.davidepalladino.lumenio.view.activity;

import com.google.android.material.tabs.TabLayoutMediator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.os.Bundle;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.ActivityMainBinding;
import it.davidepalladino.lumenio.view.fragment.ManualFragment;
import it.davidepalladino.lumenio.view.fragment.LibraryFragment;
import it.davidepalladino.lumenio.view.fragment.SceneFragment;

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
                    tab.setText(getString(R.string.manual));
                    tab.setIcon(R.drawable.ic_round_manual_tab);
                    break;
                case 1:
                    tab.setText(getString(R.string.scene));
                    tab.setIcon(R.drawable.ic_round_scene_tab);
                    break;
                case 2:
                    tab.setText(getString(R.string.library));
                    tab.setIcon(R.drawable.ic_round_library_tab);
                    break;
            }
        }).attach();
    }

    @Override
    public void onBackPressed() {
        switch (binding.tabLayout.getSelectedTabPosition()) {
            case 0:
                super.onBackPressed();
                break;
            case 1:
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
                break;
            case 2:
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
                    return new ManualFragment();
                case 1:
                    return new SceneFragment();
                case 2:
                    return new LibraryFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 3;   // TODO: Replace with 3 during the implementation of Scene.
        }
    }
}