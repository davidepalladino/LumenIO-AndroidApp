package it.davidepalladino.lumenio.view;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import androidx.recyclerview.widget.RecyclerView;

import it.davidepalladino.lumenio.databinding.RecycleViewLibraryBinding;

public class LibraryRecycleViewHolder extends RecyclerView.ViewHolder {
    private RecycleViewLibraryBinding binding;

    public LibraryRecycleViewHolder(RecycleViewLibraryBinding itemBinding) {
        super(itemBinding.getRoot());
        this.binding = itemBinding;
    }

    public void bind(long id, String name, int brightness, int red, int green, int blue) {
        binding.name.setText(name);
        binding.preview.setBackground(new ColorDrawable(Color.argb(brightness, red, green, blue)));
    }
}
