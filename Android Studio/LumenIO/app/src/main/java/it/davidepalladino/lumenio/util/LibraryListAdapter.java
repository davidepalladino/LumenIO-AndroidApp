package it.davidepalladino.lumenio.util;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.databinding.RecycleViewLibraryBinding;
import it.davidepalladino.lumenio.view.LibraryRecycleViewHolder;

public class LibraryListAdapter extends ListAdapter<Profile, LibraryRecycleViewHolder> {
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
        holder.bind(profile);
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
