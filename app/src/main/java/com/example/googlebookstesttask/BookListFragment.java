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
import com.example.googlebookstesttask.Model.BooksApiResponseFavourites;
import com.example.googlebookstesttask.Utils.IRefreshAccessToken;
import com.example.googlebookstesttask.Utils.RetrofitService;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.googlebookstesttask.MainActivity.accessToken;
import static com.example.googlebookstesttask.Utils.AnyUtils.getNewAccessToken;

public class BookListFragment extends Fragment implements IRefreshAccessToken {
    private Integer counter;
    private RecyclerView recyclerView;
    private BookListAdapter adapter;
    private SearchView searchView;
    public static CompositeDisposable mCompositeDisposable;
    public static RetrofitService retrofitService;

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
        mCompositeDisposable = new CompositeDisposable();
        if (counter==0)
            setHasOptionsMenu(true);
        retrofitService = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitService.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (counter==1) {
            loadFavourites();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recycler);
        if (counter==1) {
            loadFavourites();
        }
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

        mCompositeDisposable.add(Observable.zip(search, favourites, new BiFunction<BooksApiResponse, BooksApiResponse, BooksApiResponseFavourites>() {
            @io.reactivex.annotations.NonNull
            @Override
            public BooksApiResponseFavourites apply(@NotNull BooksApiResponse searchResponse, @NotNull BooksApiResponse favouritesResponse) throws Exception {
                return new BooksApiResponseFavourites(searchResponse, favouritesResponse);
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));



//        mCompositeDisposable.add(retrofitService.getBooks("Bearer "+accessToken, textToSearch)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(this::handleResponse, this::handleError));
    }

    private void loadFavourites() {
//        mCompositeDisposable.add(retrofitService.getFavouriteBooks("Bearer " + accessToken)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(BooksApiResponseFavourites booksApiResponseFavourites) {
        if (adapter==null) {
            adapter = new BookListAdapter(getContext(), booksApiResponseFavourites);
            recyclerView.setAdapter(adapter);
        }
        else {
            adapter.setItems(booksApiResponseFavourites);
            adapter.notifyDataSetChanged();
        }
    }

    private void handleError(Throwable error) {
        Toast.makeText(getContext(), "Error "+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        if (error instanceof HttpException) {
            HttpException exception = (HttpException) error;
            if (exception.code() == 401) {
                getNewAccessToken(getContext(), this);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    @Override
    public void tokenRefreshed() {
        if (counter==0)
            search(searchView.getQuery().toString());
        else if (counter==1)
            loadFavourites();
    }
}

