package it.davidepalladino.lumenio.view.recycleViewHolder;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.databinding.RecycleViewLibraryBinding;
import it.davidepalladino.lumenio.view.fragment.LibraryDetailFragment;

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

            NavController navController =  Navigation.findNavController(view);

            Bundle bundle = new Bundle();
            bundle.putLong(LibraryDetailFragment.BUNDLE_PROFILE_ID, profile.id);

            navController.navigate(R.id.action_ListLibraryFragment_to_DetailLibraryFragment, bundle, null);
        });
    }
}
