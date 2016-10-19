package com.uoscs09.theuos2.tab.booksearch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.api.UosApiService;
import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.AppUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BookDetailActivity extends BaseActivity {
    private static final int COLOR_AVAILABLE = Color.rgb(114, 213, 114) //R.color.material_green_200
            , COLOR_NOT_AVAILABLE = Color.rgb(246, 153, 136);

    public static void start(Activity activity, View sharedView, BookItem bookItem) {
        ActivityOptionsCompat transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedView, activity.getString(R.string.tab_book_image_transition_name));
        ActivityCompat.startActivity(activity, new Intent(activity, BookDetailActivity.class).putExtra("book", bookItem), transitionActivityOptions.toBundle());
    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.book_detail_info_layout)
    LinearLayout infoLayout;
    @BindView(R.id.book)
    ImageView imageView;

    @BindView(R.id.book_detail_related_info_layout)
    LinearLayout relatedInfoLayout;
    @BindView(R.id.book_detail_related_info_layout_title)
    TextView relatedInfoLayoutTitle;

    @BindView(R.id.book_detail_fab)
    FloatingActionButton fab;

    @BindView(R.id.progress_wheel)
    ProgressWheel progressWheel;

    @BindView(android.R.id.text1)
    TextView errorTextView;

    BookItem bookItem;
    BookDetailItem bookDetailItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_detail);
        ButterKnife.bind(this);

        bookItem = getIntent().getParcelableExtra("book");

        if (bookItem == null) {
            finish();
            return;
        }

        if (!TextUtils.isEmpty(bookItem.coverSrc)) {
            Glide.with(this)
                    .load(bookItem.coverSrc)
                    .error(R.drawable.noimg_en)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.noimg_en);
        }


        toolbar.setTitle(bookItem.title);
        toolbar.setNavigationIcon(AppUtil.getAttrValue(this, R.attr.menu_theme_ic_action_navigation_arrow_back));
        toolbar.setNavigationOnClickListener(v -> finish());

        load();
    }

    @OnClick(R.id.book_detail_fab)
    void toWeb(View v){
        Intent intent = AppUtil.getWebPageIntent("http://mlibrary.uos.ac.kr" + bookItem.url);
        AnimUtil.startActivityWithScaleUp(BookDetailActivity.this, intent, v);
    }

    private void load() {
        progressWheel.spin();

        UosApiService.libraryApi().bookDetailItem("http://mlibrary.uos.ac.kr" + bookItem.url).getAsync(
                result -> {
                    if (isFinishing())
                        return;

                    progressWheel.stopSpinning();
                    progressWheel.setVisibility(View.GONE);

                    errorTextView.setVisibility(View.GONE);

                    bookDetailItem = result;
                    drawLayout();
                },
                throwable -> {
                    throwable.printStackTrace();

                    if (isFinishing())
                        return;

                    progressWheel.stopSpinning();
                    progressWheel.setVisibility(View.GONE);

                    errorTextView.setText(R.string.progress_fail);
                }
        );
    }

    private void drawLayout() {
        fab.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Pair<String, Object> pair : bookDetailItem.detailInfoList) {
            View v = inflater.inflate(R.layout.view_book_detail_info_list, infoLayout, false);
            TextView textView1 = (TextView) v.findViewById(android.R.id.text1);
            textView1.setText(pair.first);

            LinearLayout layout = (LinearLayout) v.findViewById(android.R.id.text2);
            if (pair.second instanceof String) {
                TextView textView = new TextView(this);
                textView.setText((String) pair.second);
                textView.setTextIsSelectable(true);
                layout.addView(textView);
            } else if (pair.second instanceof List) {
                //noinspection unchecked
                List<Object> list = (List<Object>) pair.second;
                for (int i = 0; i < list.size(); i++) {
                    Object o = list.get(i);
                    if (o instanceof BookDetailItem.UrlObject) {
                        final BookDetailItem.UrlObject urlObject = (BookDetailItem.UrlObject) o;
                        Spannable styledText = new Spannable.Factory().newSpannable(urlObject.info);
                        styledText.setSpan(new URLSpan(urlObject.url), 0, urlObject.info.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        styledText.setSpan(new StyleSpan(Typeface.ITALIC), 0, urlObject.info.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                        TextView textView = new TextView(this);
                        textView.setText(styledText);
                        textView.setFocusable(true);
                        textView.setFocusableInTouchMode(true);

                        int dp = getResources().getDimensionPixelSize(R.dimen.dp4);
                        textView.setPadding(0, dp, 0, dp);

                        textView1.setPadding(0, dp, 0, dp);

                        layout.addView(textView);

                        textView.setOnClickListener(v1 -> {
                            Intent intent = AppUtil.getWebPageIntent(urlObject.url);
                            AnimUtil.startActivityWithScaleUp(BookDetailActivity.this, intent, v1);
                        });
                    } else if (o instanceof String) {
                        TextView textView = new TextView(this);
                        textView.setText((String) o);
                        textView.setTextIsSelectable(true);
                        layout.addView(textView);
                    }
                }
            }

            infoLayout.addView(v);
        }

        if(bookDetailItem.relationInfo == null){
            relatedInfoLayout.setVisibility(View.GONE);
            return;
        }


        List<BookDetailItem.SubRelationInfo> subRelationInfoList = bookDetailItem.relationInfo.subRelationInfoList;

        if (subRelationInfoList == null || subRelationInfoList.isEmpty()) {
            relatedInfoLayout.setVisibility(View.GONE);
            return;
        }

        relatedInfoLayoutTitle.setText(bookDetailItem.relationInfo.title);

        for (BookDetailItem.SubRelationInfo subRelationInfo : bookDetailItem.relationInfo.subRelationInfoList) {
            if (subRelationInfo instanceof BookDetailItem.LocationInfo) {
                BookDetailItem.LocationInfo locationInfo = (BookDetailItem.LocationInfo) subRelationInfo;

                View v = inflater.inflate(R.layout.view_book_detail_related_sub_location, relatedInfoLayout, false);

                TextView textView1 = ButterKnife.findById(v, android.R.id.text1);
                textView1.setText(locationInfo.title);


                StringBuilder sb = new StringBuilder();
                for (String s : locationInfo.infoList) {
                    sb.append(s).append('\n');
                }
                TextView textView2 = ButterKnife.findById(v, android.R.id.text2);
                textView2.setText(sb.toString());

                TextView stateText = ButterKnife.findById(v, R.id.state);

                String stateString = getString(locationInfo.bookStateStringRes());
                Spannable styledText = new Spannable.Factory().newSpannable(stateString);

                styledText.setSpan(new ForegroundColorSpan(locationInfo.isBookAvailable() ? COLOR_AVAILABLE : COLOR_NOT_AVAILABLE),
                        0, styledText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

                stateText.setText(styledText);

                if(!TextUtils.isEmpty(locationInfo.link)){
                    View reservation = ButterKnife.findById(v, R.id.button);
                    reservation.setVisibility(View.VISIBLE);
                    reservation.setOnClickListener(v1 -> {
                        Intent intent = AppUtil.getWebPageIntent(locationInfo.link);
                        AnimUtil.startActivityWithScaleUp(BookDetailActivity.this, intent, v1);
                    });
                }

                relatedInfoLayout.addView(v);
            }
        }
    }


    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "BookDetailActivity";
    }
}
