package com.deeprows.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private WebView webView;
    private FrameLayout fullscreenContainer;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        fullscreenContainer = findViewById(R.id.fullscreenContainer);

        /*
         * =========================================================
         * WEBVIEW SETTINGS
         * =========================================================
         */

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        /*
         * Allow video players to start without requiring
         * an additional user gesture.
         */
        settings.setMediaPlaybackRequiresUserGesture(false);

        /*
         * Settings required by some embedded players and iframes.
         */
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);

        /*
         * Some embedded players load resources from another
         * HTTP/HTTPS origin.
         */
        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.LOLLIPOP) {

            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            );
        }

        /*
         * Cookies are important for some video players,
         * embedded services and login/session-based content.
         */
        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );

        /*
         * Keep multiple-window behavior disabled for now.
         * We are focusing only on getting the iframe/player
         * working correctly.
         */
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        /*
         * =========================================================
         * WEBVIEW CLIENT
         * =========================================================
         */

        webView.setWebViewClient(new WebViewClient());

        /*
         * =========================================================
         * WEB CHROME CLIENT
         * =========================================================
         *
         * Handles HTML5 video fullscreen.
         */

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onShowCustomView(
                    View view,
                    CustomViewCallback callback) {

                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }

                customView = view;
                customViewCallback = callback;

                fullscreenContainer.addView(
                        customView,
                        new FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                        )
                );

                fullscreenContainer.setVisibility(
                        View.VISIBLE
                );

                webView.setVisibility(
                        View.GONE
                );

                setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                );

                getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                );
            }

            @Override
            public void onHideCustomView() {
                hideFullscreen();
            }
        });

        /*
         * =========================================================
         * DOWNLOAD HANDLING
         * =========================================================
         */

        webView.setDownloadListener(
                new DownloadListener() {

                    @Override
                    public void onDownloadStart(
                            String url,
                            String userAgent,
                            String contentDisposition,
                            String mimeType,
                            long contentLength) {

                        if (url == null || url.trim().isEmpty()) {
                            return;
                        }

                        try {

                            DownloadManager.Request request =
                                    new DownloadManager.Request(
                                            Uri.parse(url)
                                    );

                            String cookies =
                                    CookieManager
                                            .getInstance()
                                            .getCookie(url);

                            if (cookies != null &&
                                    !cookies.isEmpty()) {

                                request.addRequestHeader(
                                        "Cookie",
                                        cookies
                                );
                            }

                            if (userAgent != null &&
                                    !userAgent.isEmpty()) {

                                request.addRequestHeader(
                                        "User-Agent",
                                        userAgent
                                );
                            }

                            String fileName =
                                    URLUtil.guessFileName(
                                            url,
                                            contentDisposition,
                                            mimeType
                                    );

                            request.setTitle(fileName);

                            request.setDescription(
                                    "Downloading from Deeprows"
                            );

                            if (mimeType != null &&
                                    !mimeType.isEmpty()) {

                                request.setMimeType(
                                        mimeType
                                );
                            }

                            request.setNotificationVisibility(
                                    DownloadManager.Request
                                            .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                            );

                            request.setDestinationInExternalPublicDir(
                                    Environment.DIRECTORY_DOWNLOADS,
                                    fileName
                            );

                            DownloadManager manager =
                                    (DownloadManager)
                                            getSystemService(
                                                    Context.DOWNLOAD_SERVICE
                                            );

                            if (manager != null) {
                                manager.enqueue(request);
                            }

                        } catch (Exception ignored) {
                            // Prevent download errors from crashing the app.
                        }
                    }
                }
        );

        /*
         * =========================================================
         * LOAD WEBSITE
         * =========================================================
         */

        webView.loadUrl(
                "https://deeprowss.com"
        );
    }

    /*
     * =========================================================
     * EXIT FULLSCREEN VIDEO
     * =========================================================
     */

    private void hideFullscreen() {

        if (customView == null) {
            return;
        }

        fullscreenContainer.removeView(
                customView
        );

        fullscreenContainer.setVisibility(
                View.GONE
        );

        customView = null;

        webView.setVisibility(
                View.VISIBLE
        );

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        );

        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        if (customViewCallback != null) {

            customViewCallback.onCustomViewHidden();

            customViewCallback = null;
        }
    }

    /*
     * =========================================================
     * BACK BUTTON
     * =========================================================
     */

    @Override
    public void onBackPressed() {

        /*
         * Exit fullscreen first.
         */
        if (customView != null) {
            hideFullscreen();
            return;
        }

        /*
         * Otherwise go back through WebView history.
         */
        if (webView != null &&
                webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }
}
