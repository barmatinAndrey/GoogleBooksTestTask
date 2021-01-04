package com.example.googlebookstesttask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import io.reactivex.disposables.CompositeDisposable;


public class BookSearchActivity extends AppCompatActivity {
    private TabLayout tabs;
    private ViewPager2 pager;

    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_search);
        tabs = findViewById(R.id.tabs);
        pager = findViewById(R.id.pager);

        pager.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(tabs, pager, (tab, position) -> tab.setText(getResources().getStringArray(R.array.tabs)[position])).attach();

    }
}