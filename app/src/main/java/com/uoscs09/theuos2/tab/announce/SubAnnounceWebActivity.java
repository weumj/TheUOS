package com.uoscs09.theuos2.tab.announce;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.base.AbsArrayAdapter;
import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.customview.NonLeakingWebView;
import com.uoscs09.theuos2.util.AnimUtil;
import com.uoscs09.theuos2.util.AppRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.CollectionUtil;
import com.uoscs09.theuos2.util.PrefHelper;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Subscription;

public class SubAnnounceWebActivity extends BaseActivity {
    private static final int REQUEST_PERMISSION_FILE = 40;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.webview)
    WebView mWebView;
    WebSettings settings;
    @BindView(R.id.progress_wheel)
    ProgressWheel progressWheel;
    @BindView(R.id.error)
    TextView errorTextView;
    Unbinder unbinder;

    private String url;
    private AnnounceItem mItem;
    private List<Pair<String, String>> attachedFileUrlPairList;
    private Subscription subscription;


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        showWebPageFromIntent(intent);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_announce_subweb);
        unbinder = ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        settings = mWebView.getSettings();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        getSupportActionBar().setTitle(R.string.title_tab_announce);

        showWebPageFromIntent(getIntent());

        mWebView.setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            settings = null;
            mWebView.clearCache(true);
            mWebView.loadUrl("about:blank");
            //mWebView.destroy();
            mWebView = null;
        }
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
        if (unbinder != null)
            unbinder.unbind();

        super.onDestroy();
    }


    @OnClick(R.id.error)
    void retry() {
        showWebPageFromIntent(getIntent());
    }

    private void showWebPageFromIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }

        mItem = intent.getParcelableExtra(TabAnnounceFragment.ITEM);
        int category = intent.getIntExtra(TabAnnounceFragment.INDEX_CATEGORY, 0);
        switch (category) {
            case 3:
                url = mItem.pageURL;
                break;
            case 4:
            case 2:
            case 1:
                url = mItem.pageURL + AnnounceItem.Category.fromIndex(category - 1).tag;
                break;
            default:
                supportFinishAfterTransition();
                return;
        }

        mWebView.setInitialScale(100);

        // settings.setJavaScriptEnabled(true);
        // mWebView.setWebViewClient(new AnnounceWebViewClient(this));
        // mWebView.loadUrl(url);

        errorTextView.setVisibility(View.INVISIBLE);
        progressWheel.setVisibility(View.VISIBLE);
        progressWheel.spin();

        subscription = AppRequests.Announces.announceInfo(category, url)
                .subscribe(
                        this::setScreenWithItem,
                        throwable -> {
                            hideProgress();
                            if (errorTextView != null)
                                errorTextView.setVisibility(View.VISIBLE);
                            if (mWebView != null)
                                mWebView.loadDataWithBaseURL(null, "page not found", null, null, null);
                            throwable.printStackTrace();
                        });


        mWebView.setWebViewClient(new NonLeakingWebView.NonLeakingWebViewClient(this) {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideProgress();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                hideProgress();
            }

        });

    }

    private void hideProgress() {
        if (progressWheel != null) {
            progressWheel.stopSpinning();
            progressWheel.setVisibility(View.INVISIBLE);
        }
    }


    void setScreenWithItem(AnnounceDetailItem announceDetailItem) {
        if (mWebView == null || isFinishing())
            return;

        mWebView.loadDataWithBaseURL("http://www.uos.ac.kr/", announceDetailItem.page, "text/html", "UTF-8", "");
        this.attachedFileUrlPairList = announceDetailItem.fileNameUrlPairList;

        if (attachedFileUrlPairList != null && !attachedFileUrlPairList.isEmpty()) {
            View v = findViewById(R.id.tab_announce_subweb_fab);
            v.setVisibility(View.VISIBLE);
            v.setOnClickListener(v1 -> showDownloadDialog());
        }

        //hideProgress();
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "SubAnnounceActivity";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tab_anounce_sub, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;

            case R.id.action_web:
                try {
                    startActivity(AppUtil.getWebPageIntent(url));
                    sendClickEvent("to web");
                } catch (ActivityNotFoundException e) {
                    //e.printStackTrace();
                    AppUtil.showToast(this, R.string.error_no_related_activity_found);
                } catch (Exception e) {
                    AppUtil.showErrorToast(this, e, true);
                }
                return true;

            case R.id.action_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mItem.title);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mItem.title + " - \'" + url + "\'");

                try {
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.action_share)));
                    sendClickEvent("share");
                } catch (ActivityNotFoundException e) {
                    //e.printStackTrace();
                    AppUtil.showToast(this, R.string.error_no_related_activity_found);
                } catch (Exception e) {
                    AppUtil.showErrorToast(this, e, true);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSION_FILE:
                if (checkPermissionResultAndShowToastIfFailed(permissions, grantResults, R.string.tab_announce_download_permission_reject)) {
                    showDownloadDialog();
                }
                break;

            default:
                break;
        }
    }

    private void showDownloadDialog() {

        if (!checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissionsCompat(REQUEST_PERMISSION_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }

        if (CollectionUtil.isEmpty(attachedFileUrlPairList)) {
            AppUtil.showToast(this, R.string.tab_announce_no_download_link);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.tab_announce_download_select_file)
                    .setIconAttribute(R.attr.color_theme_ic_action_file_folder)
                    .setAdapter(new AbsArrayAdapter.SimpleAdapter<Pair<String, String>>(this, android.R.layout.simple_list_item_1, attachedFileUrlPairList) {
                                    @Override
                                    public String getTextFromItem(int position, Pair<String, String> item) {
                                        return String.format(Locale.getDefault(), "(%d)  ", position + 1) + item.first;
                                    }
                                },
                            (dialog, which) -> downloadFile(which))
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }

    private void downloadFile(int position) {
        String attachedFileUrl;
        Pair<String, String> pair = null;
        if (CollectionUtil.isEmpty(attachedFileUrlPairList)) {
            attachedFileUrl = null;
        } else {
            pair = attachedFileUrlPairList.get(position);
            attachedFileUrl = pair.second;
        }

        if (TextUtils.isEmpty(attachedFileUrl)) {
            AppUtil.showToast(this, R.string.tab_announce_no_download_link);
            return;
        }

        Dialog progressDialog = AppUtil.getProgressDialog(this, false, getString(R.string.progress_downloading), null);

        //noinspection ResourceType
        final String docPath = PrefHelper.Data.getDocumentPath();
        final Subscription fileDownloadSubs = AppRequests.Announces.attachedFileDownload(attachedFileUrl, docPath, pair.first)
                .subscribe(result -> {
                            String docDir = docPath.substring(docPath.lastIndexOf('/') + 1);
                            Snackbar.make(mWebView, getString(R.string.saved_file, docDir, result.getName()), Snackbar.LENGTH_LONG)
                                    .setAction(R.string.action_open, v -> {

                                        sendClickEvent("open file");

                                        // fixme api25
                                        Uri fileUri = Uri.fromFile(result);
                                        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
                                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);

                                        Intent intent = new Intent()
                                                .setAction(Intent.ACTION_VIEW)
                                                .setDataAndType(fileUri, mimeType);
                                        try {
                                            AnimUtil.startActivityWithScaleUp(SubAnnounceWebActivity.this, intent, v);
                                        } catch (ActivityNotFoundException e) {
                                            AppUtil.showToast(SubAnnounceWebActivity.this, R.string.error_no_activity_found_to_handle_file);
                                        } catch (Exception e) {
                                            AppUtil.showErrorToast(SubAnnounceWebActivity.this, e, true);
                                        }
                                    })
                                    .show();
                        },
                        e -> {
                            AppUtil.showErrorToast(SubAnnounceWebActivity.this, e, true);
                            progressDialog.dismiss();
                            progressDialog.setOnCancelListener(null);
                        },
                        () -> {
                            progressDialog.dismiss();
                            progressDialog.setOnCancelListener(null);
                        }
                );

        progressDialog.setOnCancelListener(dialog -> fileDownloadSubs.unsubscribe());
        progressDialog.show();

        sendClickEvent("download file");
    }
}
