package it.davidepalladino.lumenio.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import it.davidepalladino.lumenio.R;

public class LibraryRecycleViewHolder extends RecyclerView.ViewHolder {
    private final TextView textView;

    public LibraryRecycleViewHolder(@NonNull View itemView) {
        super(itemView);
        this.textView = itemView.findViewById(R.id.textView);
    }

    public void bind(String text) {
        textView.setText(text);
    }

    static LibraryRecycleViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleview_library, parent, false);
        return new LibraryRecycleViewHolder(view);
    }

}
