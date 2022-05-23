package it.davidepalladino.lumenio.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.FragmentLibraryBinding;
import it.davidepalladino.lumenio.databinding.RecycleviewLibraryBinding;

public class LibraryRecycleViewHolder extends RecyclerView.ViewHolder {
    private RecycleviewLibraryBinding binding;
    private long id;

    public LibraryRecycleViewHolder(RecycleviewLibraryBinding itemBinding) {
        super(itemBinding.getRoot());
        this.binding = itemBinding;
    }

    public void bind(long id, String name, int brightness, int red, int green, int blue) {
        binding.name.setText(name);
        binding.preview.setBackground(new ColorDrawable(Color.argb(brightness, red, green, blue)));
    }
}
