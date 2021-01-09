package com.example.googlebookstesttask;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlebookstesttask.Model.BooksApiResponse;
import com.example.googlebookstesttask.Utils.IRefreshAccessToken;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

import static com.example.googlebookstesttask.BookSearchActivity.mCompositeDisposable;
import static com.example.googlebookstesttask.BookSearchActivity.retrofitService;
import static com.example.googlebookstesttask.MainActivity.accessToken;
import static com.example.googlebookstesttask.Utils.AnyUtils.getNewAccessToken;

public class BookListFragment extends Fragment implements IRefreshAccessToken {
    private Integer counter;
    private RecyclerView recyclerView;
    private BookListAdapter adapter;
    private SearchView searchView;
    private String textToSearch;

    public BookListFragment() {
    }

    public static BookListFragment newInstance(Integer counter) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        args.putInt("counter", counter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            counter = getArguments().getInt("counter");
        if (counter==0)
            setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (counter==0 && textToSearch!=null && !textToSearch.isEmpty())
            search(textToSearch);
        else if (counter==1)
            loadFavourites();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_book_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem itemSearch = menu.findItem(R.id.item_search);
        searchView = (SearchView) itemSearch.getActionView();
        searchView.setQueryHint(getString(R.string.enter_text_to_search));
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                textToSearch = query;
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void search(String textToSearch) {
        Observable<BooksApiResponse> search = retrofitService.getBooks("Bearer "+accessToken, textToSearch)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
        Observable<BooksApiResponse> favourites = retrofitService.getFavouriteBooks("Bearer " + accessToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());

        mCompositeDisposable.add(Observable.zip(search, favourites, (searchResponse, favouritesResponse) -> {
            List<String> favouritesIds = new ArrayList<>();
            if (favouritesResponse.getItems()!=null && !favouritesResponse.getItems().isEmpty()) {
                for (BooksApiResponse.BookItem item : favouritesResponse.getItems())
                    favouritesIds.add(item.getId());
            }
            for (BooksApiResponse.BookItem item1: searchResponse.getItems()) {
                if (favouritesIds.contains(item1.getId()))
                    item1.setIfInFavourites(true);
            }
            return searchResponse;
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void loadFavourites() {
        mCompositeDisposable.add(retrofitService.getFavouriteBooks("Bearer " + accessToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseFavourites, this::handleError));
    }

    private void handleResponse(BooksApiResponse booksApiResponse) {
        if (booksApiResponse.getItems()==null)
            booksApiResponse = new BooksApiResponse();
        if (adapter == null) {
            adapter = new BookListAdapter(getContext(), booksApiResponse);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(booksApiResponse);
            adapter.notifyDataSetChanged();
        }
    }

    private void handleResponseFavourites(BooksApiResponse booksApiResponse) {
        if (booksApiResponse.getItems()!=null && !booksApiResponse.getItems().isEmpty()) {
            for (BooksApiResponse.BookItem item : booksApiResponse.getItems()) {
                item.setIfInFavourites(true);
            }
        }
        handleResponse(booksApiResponse);
    }

    private void handleError(Throwable error) {
        if (error instanceof HttpException) {
            HttpException exception = (HttpException) error;
            if (exception.code() == 401)
                getNewAccessToken(getContext(), this);
            else
                Toast.makeText(getContext(), "Error "+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void tokenRefreshed() {
        if (counter==0)
            search(searchView.getQuery().toString());
        else if (counter==1)
            loadFavourites();
    }
}

