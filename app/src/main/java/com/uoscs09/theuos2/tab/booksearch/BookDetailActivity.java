package com.uoscs09.theuos2.tab.booksearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookDetailActivity extends BaseActivity {

    public static void start(Activity activity, View sharedView,BookItem bookItem) {
        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedView, activity.getString(R.string.tab_book_image_transition_name));
        ActivityCompat.startActivity(activity, new Intent(activity, BookDetailActivity.class).putExtra("book", bookItem), transitionActivityOptions.toBundle());
    }

    @BindView(R.id.book)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_detail);
        ButterKnife.bind(this);

        BookItem item = getIntent().getParcelableExtra("book");

        if (item != null && !TextUtils.isEmpty(item.coverSrc))
            Glide.with(this)
                    .load(item.coverSrc)
                    .error(R.drawable.noimg_en)
                    .into(imageView);

    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "BookDetailActivity";
    }
}
