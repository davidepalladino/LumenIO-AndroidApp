package it.davidepalladino.lumenio;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import it.davidepalladino.lumenio.databinding.FragmentControlBinding;
import it.davidepalladino.lumenio.data.ProfileViewModel;

public class ControlFragment extends Fragment {
    private FragmentControlBinding binding;
    private ProfileViewModel profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profile = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        // Example: The values could be found from SharedPreference.
        profile.setBrightness(255);
        profile.setRed(255);
        profile.setGreen(182);
        profile.setBlue(112);

        binding = FragmentControlBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setProfile(profile);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(ControlFragment.this)
//                        .navigate(R.id.action_ControlFragment_to_LibraryFragment);
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}