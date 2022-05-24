package it.davidepalladino.lumenio.util;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.databinding.RecycleViewLibraryBinding;
import it.davidepalladino.lumenio.view.LibraryRecycleViewHolder;

public class LibraryListAdapter extends ListAdapter<Profile, LibraryRecycleViewHolder> {
    public Fragment fragmentHost;
    public LibraryListAdapter(@NonNull DiffUtil.ItemCallback<Profile> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public LibraryRecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LibraryRecycleViewHolder(RecycleViewLibraryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryRecycleViewHolder holder, int position) {
        Profile profile = getItem(position);
        holder.bind(profile.id, profile.name, profile.brightness, profile.red, profile.green, profile.blue);
        holder.itemView.setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.action_ListLibraryFragment_to_DetailLibraryFragment));
    }

    public static class ProfileDiff extends DiffUtil.ItemCallback<Profile> {
        @Override
        public boolean areItemsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
//            return oldItem == newItem;
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Profile oldItem, @NonNull Profile newItem) {
//            return oldItem.id == newItem.id;
            return false;
        }
    }
}
