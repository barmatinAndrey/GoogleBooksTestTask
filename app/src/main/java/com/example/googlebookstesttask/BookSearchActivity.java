package com.example.googlebookstesttask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.widget.Toast;

import com.example.googlebookstesttask.Model.BooksApiResponse;
import com.example.googlebookstesttask.Utils.IRefreshAccessToken;
import com.example.googlebookstesttask.Utils.RetrofitService;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.googlebookstesttask.MainActivity.accessToken;
import static com.example.googlebookstesttask.Utils.AnyUtils.getNewAccessToken;


public class BookSearchActivity extends AppCompatActivity implements IRefreshAccessToken {
    protected static TabLayout tabs;
    protected static CompositeDisposable mCompositeDisposable;
    protected static RetrofitService retrofitService;
    private ViewPager2 pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);
        tabs = findViewById(R.id.tabs);
        pager = findViewById(R.id.pager);

        mCompositeDisposable = new CompositeDisposable();
        retrofitService = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitService.class);

        loadFavourites();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
    }

    private void loadFavourites() {
        mCompositeDisposable.add(retrofitService.getFavouriteBooks("Bearer " + accessToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(BooksApiResponse booksApiResponse) {
        pager.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            if (position==0)
                tab.setText(getResources().getStringArray(R.array.tabs)[position]);
            else if (position==1)
                if (booksApiResponse.getItems()!=null)
                    tab.setText(getResources().getStringArray(R.array.tabs)[position]+" ("+booksApiResponse.getItems().size()+")");
                else
                    tab.setText(getResources().getStringArray(R.array.tabs)[position]+" (0)");
        })
                .attach();
    }

    private void handleError(Throwable error) {
        if (error instanceof HttpException) {
            HttpException exception = (HttpException) error;
            if (exception.code() == 401)
                getNewAccessToken(this, this);
            else
                Toast.makeText(this, "Error "+error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void tokenRefreshed() {
        loadFavourites();
    }
}