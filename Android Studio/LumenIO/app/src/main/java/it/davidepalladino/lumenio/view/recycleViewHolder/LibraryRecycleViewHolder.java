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
    private RecycleViewLibraryBinding recycleViewLibraryBinding;

    public LibraryRecycleViewHolder(RecycleViewLibraryBinding itemBinding) {
        super(itemBinding.getRoot());
        this.recycleViewLibraryBinding = itemBinding;
    }

    public void bind(Profile profile) {
        recycleViewLibraryBinding.name.setText(profile.name);
        recycleViewLibraryBinding.values.setText(
                String.format("%03d", profile.red) + " " +
                String.format("%03d", profile.green) + " " +
                String.format("%03d", profile.blue)
        );
        recycleViewLibraryBinding.preview.setBackground(new ColorDrawable(Color.rgb(profile.red, profile.green, profile.blue)));
        recycleViewLibraryBinding.item.setOnClickListener(view -> {
            NavController navController =  Navigation.findNavController(view);

            Bundle bundle = new Bundle();
            bundle.putLong(LibraryDetailFragment.BUNDLE_PROFILE_ID, profile.id);

            navController.navigate(R.id.action_LibraryListFragment_to_LibraryDetailFragment, bundle, null);
        });
    }
}
