package com.example.googlebookstesttask;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.googlebookstesttask.Model.BooksApiResponse;
import com.example.googlebookstesttask.Utils.IRefreshAccessToken;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.squareup.picasso.Picasso;

import java.util.List;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import static com.example.googlebookstesttask.BookSearchActivity.mCompositeDisposable;
import static com.example.googlebookstesttask.BookSearchActivity.retrofitService;
import static com.example.googlebookstesttask.MainActivity.accessToken;
import static com.example.googlebookstesttask.Utils.AnyUtils.getNewAccessToken;

public class BookListAdapter extends RecyclerView.Adapter<BookListAdapter.ViewHolder> implements IRefreshAccessToken {
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
        final SpannableStringBuilder text = new SpannableStringBuilder(holder.preview.getText());
        text.setSpan(new URLSpan(""), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.preview.setText(text);
        holder.preview.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bookItemList.get(position).getVolumeInfo().getPreviewLink()));
            context.startActivity(browserIntent);
        });

        holder.favorites_button.setChecked(bookItemList.get(position).isIfInFavourites());
        holder.favorites_button.setOnClickListener(v -> {
            if (holder.favorites_button.isChecked()) {
                mCompositeDisposable.add(retrofitService.addToFavorites("Bearer " + accessToken, bookItemList.get(position).getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleComplete));
            }
            else {
                mCompositeDisposable.add(retrofitService.removeFromFavorites("Bearer " + accessToken, bookItemList.get(position).getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleComplete));
            }
        });
    }

    private void handleComplete() {
        mCompositeDisposable.add(retrofitService.getFavouriteBooks("Bearer " + accessToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(BooksApiResponse booksApiResponse) {
        if (booksApiResponse.getItems()!=null)
            BookSearchActivity.tabs.getTabAt(1).setText(context.getResources().getStringArray(R.array.tabs)[1]+" ("+booksApiResponse.getItems().size()+")");
        else
            BookSearchActivity.tabs.getTabAt(1).setText(context.getResources().getStringArray(R.array.tabs)[1]+" (0)");
    }

    private void handleError(Throwable error) {
        if (error instanceof HttpException) {
            HttpException exception = (HttpException) error;
            if (exception.code() == 401)
                getNewAccessToken(context, this);
            else
                Toast.makeText(context, "Error "+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void tokenRefreshed() {
        handleComplete();
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
        private ToggleButton favorites_button;

        public ViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.thumbnail);
            title = view.findViewById(R.id.title);
            author = view.findViewById(R.id.author);
            preview = view.findViewById(R.id.preview);
            favorites_button = view.findViewById(R.id.favorites_button);

        }

    }




}
