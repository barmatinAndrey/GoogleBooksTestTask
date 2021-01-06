package com.example.googlebookstesttask.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlebookstesttask.Model.BooksApiResponse;
import com.example.googlebookstesttask.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.ViewHolder> {
    private Context context;
    private List<BooksApiResponse.BookItem> bookItemList;

    public BookListAdapter(Context context, BooksApiResponse booksApiResponse) {
        this.context = context;
        bookItemList = booksApiResponse.getItems();
    }

    public void setItems(BooksApiResponse booksApiResponse) {
        bookItemList = booksApiResponse.getItems();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (bookItemList.get(position).getVolumeInfo().getImageLinks()!=null) {
            Picasso.with(context)
                    .load(bookItemList.get(position).getVolumeInfo().getImageLinks().getThumbnail())
                    .into(holder.thumbnail);
        }
        holder.title.setText(bookItemList.get(position).getVolumeInfo().getTitle());
        String authors = "";
        if (bookItemList.get(position).getVolumeInfo().getAuthors()!=null) {
            for (String author : bookItemList.get(position).getVolumeInfo().getAuthors()) {
                authors = authors + author + ", ";
            }
            holder.author.setText(authors.substring(0, authors.length() - 2));
        }

    }

    @Override
    public int getItemCount() {
        return bookItemList.size();
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
