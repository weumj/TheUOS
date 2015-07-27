package com.uoscs09.theuos2.tab.announce;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;

import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.common.WebViewActivity;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.io.File;

public class SubAnnounceWebActivity extends WebViewActivity {
    private String url;
    private AnnounceItem mItem;
    private int category;
    private Dialog mProgressDialog;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_tab_announce);
        Intent intent = getIntent();

        mItem = intent.getParcelableExtra(TabAnnounceFragment.ITEM);
        category = intent.getIntExtra(TabAnnounceFragment.PAGE_NUM, 0);
        switch (category) {
            case 3:
                url = mItem.pageURL;
                break;
            case 2:
                url = mItem.pageURL + "FA2";
                break;
            case 1:
                url = mItem.pageURL + "FA1";
                break;
            default:
                supportFinishAfterTransition();
                return;
        }

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
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
                startActivity(AppUtil.setWebPageIntent(url));
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

    private void dismissProgressDialog() {
        mProgressDialog.dismiss();
        mProgressDialog.setOnCancelListener(null);
    }

    private void downloadFile() {
        if (mItem.attachedFileUrl.equals(StringUtil.NULL)) {
            AppUtil.showToast(this, R.string.tab_announce_no_download_link);
            return;
        }

        if (mProgressDialog == null)
            mProgressDialog = AppUtil.getProgressDialog(this, false, getString(R.string.progress_downloading), null);

        String url;
        switch (category) {
            case 3:
                url = mItem.attachedFileUrl;
                break;
            case 2:
                url = mItem.attachedFileUrl + "FA2";
                break;
            case 1:
                url = mItem.attachedFileUrl + "FA1";
                break;
            default:
                return;
        }

        final AsyncTask<Void, ?, File> task = HttpRequest.Builder.newConnectionRequestBuilder(url)
                .setHttpMethod(HttpRequest.HTTP_METHOD_POST)
                .build()
                .wrap(new HttpRequest.FileDownloadProcessor(new File(PrefUtil.getDocumentPath(this))))
                .getAsync(
                        new Request.ResultListener<File>() {
                            @Override
                            public void onResult(final File result) {
                                dismissProgressDialog();

                                Snackbar.make(mWebView, result.getName() + "\n" + getText(R.string.saved_file), Snackbar.LENGTH_LONG)
                                        .setAction(R.string.action_open, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

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
                                                    AppUtil.showToast(SubAnnounceWebActivity.this, R.string.tab_announce_no_activity_found_to_handle_file);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    AppUtil.showErrorToast(SubAnnounceWebActivity.this, e, true);
                                                }
                                            }
                                        })
                                        .show();
                            }
                        },
                        new Request.ErrorListener() {
                            @Override
                            public void onError(Exception e) {
                                dismissProgressDialog();
                                AppUtil.showErrorToast(SubAnnounceWebActivity.this, e, true);
                            }
                        }
                );

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                AsyncUtil.cancelTask(task);
            }
        });
        mProgressDialog.show();

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
