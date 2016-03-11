package com.uoscs09.theuos2.tab.announce;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.common.WebViewActivity;
import com.uoscs09.theuos2.http.NetworkRequests;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefHelper;

import java.io.File;

import mj.android.utils.task.Task;

public class SubAnnounceWebActivity extends WebViewActivity {
    private static final int REQUEST_PERMISSION_FILE = 40;

    private String url;
    private AnnounceItem mItem;
    private int category;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        showWebPageFromIntent(intent);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        getSupportActionBar().setTitle(R.string.title_tab_announce);

        showWebPageFromIntent(getIntent());

    }

    private void showWebPageFromIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }

        mItem = intent.getParcelableExtra(TabAnnounceFragment.ITEM);
        category = intent.getIntExtra(TabAnnounceFragment.INDEX_CATEGORY, 0);
        switch (category) {
            case 3:
                url = mItem.pageURL;
                break;
            case 4:
            case 2:
            case 1:
                url = mItem.pageURL + NetworkRequests.Announces.Category.values()[category - 1].tag;
                break;
            default:
                supportFinishAfterTransition();
                return;
        }

        mWebView.setInitialScale(100);

        //settings.setJavaScriptEnabled(true);
        //mWebView.setWebViewClient(new AnnounceWebViewClient(this, selection));
        mWebView.loadUrl(url);
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
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_web:
                startActivity(AppUtil.getWebPageIntent(url));
                return true;

            case R.id.action_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mItem.title);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mItem.title + " - \'" + url + "\'");

                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.action_share)));
                return true;

            case R.id.action_download:
                downloadFile();
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
                if (checkPermissionResultAndShowToastIfFailed(permissions, grantResults, getString(R.string.tab_announce_download_permission_reject))) {
                    downloadFile();
                }
                break;

            default:
                break;
        }
    }

    private void downloadFile() {
        if (TextUtils.isEmpty(mItem.attachedFileUrl)) {
            AppUtil.showToast(this, R.string.tab_announce_no_download_link);
            return;
        }

        if (!checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermissionsCompat(REQUEST_PERMISSION_FILE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }

        Dialog progressDialog = AppUtil.getProgressDialog(this, false, getString(R.string.progress_downloading), null);

        String url;
        switch (category) {
            case 3:
                url = mItem.attachedFileUrl;
                break;
            case 4:
            case 2:
            case 1:
                url = mItem.attachedFileUrl + NetworkRequests.Announces.Category.values()[category - 1].tag;
                break;
            default:
                return;
        }

        //noinspection ResourceType
        final String docPath = PrefHelper.Data.getDocumentPath();
        final Task<File> task = NetworkRequests.Announces.attachedFileDownloadRequest(url, docPath);
        task.getAsync(result -> {
                    progressDialog.dismiss();
                    progressDialog.setOnCancelListener(null);

                    String docDir = docPath.substring(docPath.lastIndexOf('/') + 1);
                    Snackbar.make(mWebView, getString(R.string.saved_file, docDir, result.getName()), Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_open, v -> {

                                sendClickEvent("open file");

                                Uri fileUri = Uri.fromFile(result);
                                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
                                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);

                                Intent intent = new Intent()
                                        .setAction(Intent.ACTION_VIEW)
                                        .setDataAndType(fileUri, mimeType);
                                try {
                                    AppUtil.startActivityWithScaleUp(SubAnnounceWebActivity.this, intent, v);
                                } catch (ActivityNotFoundException e) {
                                    //e.printStackTrace();
                                    AppUtil.showToast(SubAnnounceWebActivity.this, R.string.error_no_activity_found_to_handle_file);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    AppUtil.showErrorToast(SubAnnounceWebActivity.this, e, true);
                                }
                            })
                            .show();
                },
                e -> {
                    progressDialog.dismiss();
                    progressDialog.setOnCancelListener(null);

                    AppUtil.showErrorToast(SubAnnounceWebActivity.this, e, true);
                }
        );

        progressDialog.setOnCancelListener(dialog -> task.cancel());
        progressDialog.show();

        sendClickEvent("download file");
    }

/*
    private class AnnounceWebViewClient extends NonLeakingWebView.NonLeakingWebViewClient {

        private final int selection;
        private boolean firstLoading = true;
        public AnnounceWebViewClient(Activity activity, int selection) {
            super(activity);
            this.selection = selection;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if(firstLoading) {
                if (selection != 3) {
                    view.loadUrl(" javascript:(function() { " +
                            "var viewType = document.getElementsByClassName('viewType01')[0]; " +
                            "document.body.removeChild(document.getElementById('container'));" +
                            "document.body.removeChild(document.getElementById('footer'));" +
                            "document.body.style.backgroundImage = '';" +
                            "document.clear();" +
                            "document.body.appendChild(viewType);" +
                            "})()");
                } else {
                    view.loadUrl(" javascript:(function() { " +
                            "var viewType = document.getElementsByClassName('notice_tb')[0]; " +
                            "document.body.removeChild(document.getElementById('all_wrap'));" +
                            "document.body.style.backgroundImage = '';" +
                            "document.clear();" +
                            "document.body.appendChild(viewType);" +
                            "})()");
                    view.setInitialScale(100);
                }
                firstLoading = false;
            }

        }
    }
    */
}
