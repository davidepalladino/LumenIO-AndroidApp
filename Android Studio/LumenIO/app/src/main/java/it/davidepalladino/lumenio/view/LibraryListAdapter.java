package it.davidepalladino.lumenio.view;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import it.davidepalladino.lumenio.data.Profile;

public class LibraryListAdapter extends ListAdapter<Profile, LibraryRecycleViewHolder> {
    public LibraryListAdapter(@NonNull DiffUtil.ItemCallback<Profile> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public LibraryRecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return LibraryRecycleViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull LibraryRecycleViewHolder holder, int position) {
        Profile profile = getItem(position);
        holder.bind(profile.name);
    }

    static class ProfileDiff extends DiffUtil.ItemCallback<Profile> {
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
