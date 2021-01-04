package com.example.googlebookstesttask.Utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlebookstesttask.Model.BooksApiResponse;
import com.example.googlebookstesttask.R;

import java.util.List;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.ViewHolder> {
    private List<BooksApiResponse.items> itemsList;

    public BookListAdapter(BooksApiResponse booksApiResponse) {
        itemsList = booksApiResponse.getItemsList();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(itemsList.get(position).getVolumeInfo().getTitle());
        String authors = "";
        for (String author: itemsList.get(position).getVolumeInfo().getAuthors()) {
            authors = authors + author + ", ";
        }
        holder.author.setText(authors.substring(0, authors.length()-2));

    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail;
        private TextView title;
        private TextView author;
        private TextView preview;

        public ViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.thumbnail);
            title = view.findViewById(R.id.title);
            author = view.findViewById(R.id.author);
            preview = view.findViewById(R.id.preview);

        }

    }




}
