package it.davidepalladino.lumenio.view.recycleViewHolder;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.core.view.ViewCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.recyclerview.widget.RecyclerView;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.databinding.RecycleViewLibraryBinding;

public class LibraryRecycleViewHolder extends RecyclerView.ViewHolder {
    private RecycleViewLibraryBinding binding;

    public LibraryRecycleViewHolder(RecycleViewLibraryBinding itemBinding) {
        super(itemBinding.getRoot());
        this.binding = itemBinding;
    }

    public void bind(Profile profile) {
        binding.name.setText(profile.name);
        binding.name.setTransitionName(profile.name);     // Setting this and commenting that in `setOnClickListener`, is possible to see the animation.
        binding.preview.setBackground(new ColorDrawable(Color.argb(profile.brightness, profile.red, profile.green, profile.blue)));
        binding.item.setOnClickListener(view -> {
//            binding.name.setTransitionName(profile.name);

            NavController navController =  Navigation.findNavController(view);

            Bundle bundle = new Bundle();
            bundle.putString("transitionForName", profile.name);

            FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                    .addSharedElement(binding.name, profile.name)
                    .build();
            navController.navigate(R.id.action_ListLibraryFragment_to_DetailLibraryFragment, bundle, null, extras);
        });
    }
}
