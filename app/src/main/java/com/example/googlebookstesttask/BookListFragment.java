package com.example.googlebookstesttask;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.googlebookstesttask.Model.BooksApiResponse;
import com.example.googlebookstesttask.Utils.BookListAdapter;
import com.example.googlebookstesttask.Utils.RetrofitService;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookListFragment extends Fragment {
    private Integer counter;
    private CompositeDisposable mCompositeDisposable;
    private RecyclerView recyclerView;

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
        recyclerView = view.findViewById(R.id.recycler);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RetrofitService retrofitService = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitService.class);

        mCompositeDisposable.add(retrofitService.getBooks("ya29.a0AfH6SMACFYtOxbhMPXFi2mGCIeIha9SbKiqTjNRIF_aIqv9NHIUZLuRAeRgPwQMJYSasVQE_Hck3FOJOoQF3krjbEprhT1r_yqJFguJt28cjzNX1hpPbYsASTNkjd5QPFF06al-8WTR2CEWzWi_1HBd_cPeFcZS91-jEdtCDRfKI", "flowers")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(BooksApiResponse booksApiResponse) {
        BookListAdapter adapter = new BookListAdapter(booksApiResponse);
        recyclerView.setAdapter(adapter);
    }

    private void handleError(Throwable error) {
        Toast.makeText(getContext(), "Error "+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }

}

