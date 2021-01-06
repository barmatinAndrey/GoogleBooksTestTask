package com.example.googlebookstesttask;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlebookstesttask.Model.BooksApiResponse;
import com.example.googlebookstesttask.Utils.BookListAdapter;
import com.example.googlebookstesttask.Utils.IRefreshAccessToken;
import com.example.googlebookstesttask.Utils.RetrofitService;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.googlebookstesttask.MainActivity.accessToken;
import static com.example.googlebookstesttask.Utils.AnyUtils.getNewAccessToken;

public class BookListFragment extends Fragment implements IRefreshAccessToken {
    private Integer counter;
    private CompositeDisposable mCompositeDisposable;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private BookListAdapter adapter;

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
        searchView = view.findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void search(String textToSearch) {
        RetrofitService retrofitService = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitService.class);

        mCompositeDisposable.add(retrofitService.getBooks(accessToken, textToSearch)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(BooksApiResponse booksApiResponse) {
        if (adapter==null) {
            adapter = new BookListAdapter(getContext(), booksApiResponse);
            recyclerView.setAdapter(adapter);
        }
        else {
            adapter.setItems(booksApiResponse);
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

    }
}

