/*
 * REQUIRED Gradle dependency:
 *
 * implementation "androidx.media3:media3-exoplayer:1.8.0"
 * implementation "androidx.media3:media3-ui:1.8.0"
 *
 * Keep your other existing dependencies.
 */

package com.deeprows.football;

import android.app.Activity;
import android.app.DownloadManager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import android.graphics.Color;
import android.graphics.Bitmap;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.splashscreen.SplashScreen;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;

import androidx.media3.exoplayer.ExoPlayer;

import androidx.media3.ui.PlayerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {


    /*
     * =========================================================
     * WEBSITE
     * =========================================================
     */

    private static final String WEBSITE_URL =
            "https://deeprowss.com";


    /*
     * =========================================================
     * EXTERNAL URL EXCEPTIONS
     * =========================================================
     */

    private static final String TELEGRAM_URL =
            "https://t.me/deeprows";


    /*
     * =========================================================
     * UI
     * =========================================================
     */

    private static final int POPUP_BAR_HEIGHT_DP = 58;


    private static final int BG_COLOR =
            Color.rgb(
                    7,
                    9,
                    13
            );


    /*
     * =========================================================
     * ROOT
     * =========================================================
     */

    private FrameLayout rootLayout;


    private RefreshableWebViewContainer
            refreshContainer;


    /*
     * =========================================================
     * MAIN WEBVIEW
     * =========================================================
     */

    private WebView mainWebView;


    /*
     * =========================================================
     * POPUP WEBVIEWS
     * =========================================================
     */

    private WebView popupWebView;


    private final List<WebView>
            popupWebViewStack =
            new ArrayList<>();


    private FrameLayout popupContainer;


    /*
     * =========================================================
     * VIDEO FULLSCREEN
     * =========================================================
     */

    private View customVideoView;


    private WebChromeClient.CustomViewCallback
            customViewCallback;


    /*
     * =========================================================
     * NATIVE MEDIA PLAYER
     * =========================================================
     */

    private ExoPlayer nativePlayer;


    private PlayerView nativePlayerView;


    private boolean nativePlayerFullscreen =
            false;


    /*
     * =========================================================
     * OTHER
     * =========================================================
     */

    private int popupBarHeight;


    private boolean showingOfflinePage =
            false;


    private boolean webPageVisible =
            false;


    private View customSplashView;


    /*
     * =========================================================
     * ACTIVITY CREATE
     * =========================================================
     */

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {


        SplashScreen splashScreen =
                SplashScreen.installSplashScreen(
                        this
                );


        splashScreen.setKeepOnScreenCondition(
                () -> !webPageVisible
        );


        super.onCreate(
                savedInstanceState
        );


        /*
         * =====================================================
         * WINDOW
         * =====================================================
         */

        getWindow().setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(
                        BG_COLOR
                )
        );


        getWindow().setNavigationBarColor(
                BG_COLOR
        );


        getWindow().setStatusBarColor(
                BG_COLOR
        );


        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );


        /*
         * STATUS BAR IS VISIBLE
         * DURING NORMAL APP USE.
         */

        showStatusBar();


        popupBarHeight =
                dp(
                        POPUP_BAR_HEIGHT_DP
                );


        /*
         * =====================================================
         * ROOT LAYOUT
         * =====================================================
         */

        rootLayout =
                new FrameLayout(
                        this
                );


        rootLayout.setBackgroundColor(
                BG_COLOR
        );


        setContentView(
                rootLayout
        );


        /*
         * =====================================================
         * CUSTOM SPLASH
         * =====================================================
         */

        showCustomSplash();


        /*
         * =====================================================
         * CREATE MAIN WEBVIEW
         * =====================================================
         */

        createMainWebView();


        if (mainWebView != null) {

            mainWebView.setBackgroundColor(
                    BG_COLOR
            );
        }


        /*
         * =====================================================
         * LOAD WEBSITE
         * =====================================================
         */

        mainWebView.loadUrl(
                WEBSITE_URL
        );
    }


    /*
     * =========================================================
     * CUSTOM SPLASH
     * =========================================================
     */

    private void showCustomSplash() {


        if (rootLayout == null) {

            return;
        }


        if (customSplashView != null) {

            return;
        }


        android.widget.ImageView splashImage =
                new android.widget.ImageView(
                        this
                );


        int splashId =
                getResources().getIdentifier(
                        "deeprowss_splash",
                        "drawable",
                        getPackageName()
                );


        if (splashId != 0) {

            splashImage.setImageResource(
                    splashId
            );
        }


        splashImage.setScaleType(
                android.widget.ImageView.ScaleType.CENTER_INSIDE
        );


        splashImage.setAdjustViewBounds(
                true
        );


        splashImage.setBackgroundColor(
                BG_COLOR
        );


        int margin =
                dp(
                        32
                );


        FrameLayout.LayoutParams splashParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );


        splashParams.setMargins(
                margin,
                margin,
                margin,
                margin
        );


        customSplashView =
                splashImage;


        rootLayout.addView(
                customSplashView,
                splashParams
        );


        customSplashView.bringToFront();
    }


    private void hideCustomSplash() {


        if (customSplashView == null) {

            return;
        }


        View splash =
                customSplashView;


        customSplashView =
                null;


        splash.animate()
                .alpha(
                        0f
                )
                .setDuration(
                        180
                )
                .withEndAction(
                        () -> {

                            if (rootLayout != null) {

                                rootLayout.removeView(
                                        splash
                                );
                            }

                        }
                )
                .start();
    }


    /*
     * =========================================================
     * CREATE MAIN WEBVIEW
     * =========================================================
     */

    private void createMainWebView() {


        mainWebView =
                new WebView(
                        this
                );


        mainWebView.setBackgroundColor(
                BG_COLOR
        );


        configureWebView(
                mainWebView
        );


        mainWebView.setWebViewClient(
                new WebViewClient() {


                    /*
                     * =============================================
                     * NAVIGATION
                     * =============================================
                     */

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request
                    ) {


                        if (request == null ||
                                request.getUrl() == null) {

                            return false;
                        }


                        String url =
                                request.getUrl()
                                        .toString();


                        return handleMainUrl(
                                view,
                                url
                        );
                    }


                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            String url
                    ) {


                        if (url == null) {

                            return false;
                        }


                        return handleMainUrl(
                                view,
                                url
                        );
                    }


                    /*
                     * =============================================
                     * PAGE START
                     * =============================================
                     */

                    @Override
                    public void onPageStarted(
                            WebView view,
                            String url,
                            Bitmap favicon
                    ) {


                        super.onPageStarted(
                                view,
                                url,
                                favicon
                        );


                        view.setBackgroundColor(
                                BG_COLOR
                        );
                    }


                    /*
                     * =============================================
                     * PAGE VISIBLE
                     * =============================================
                     */

                    @Override
                    public void onPageCommitVisible(
                            WebView view,
                            String url
                    ) {


                        super.onPageCommitVisible(
                                view,
                                url
                        );


                        if (!webPageVisible) {

                            webPageVisible =
                                    true;


                            hideCustomSplash();
                        }
                    }


                    /*
                     * =============================================
                     * PAGE FINISHED
                     * =============================================
                     */

                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url
                    ) {


                        super.onPageFinished(
                                view,
                                url
                        );


                        if (url != null &&
                                url.startsWith(
                                        WEBSITE_URL
                                )) {

                            showingOfflinePage =
                                    false;
                        }
                    }


                    /*
                     * =============================================
                     * ERROR HANDLING
                     * =============================================
                     */

                    @Override
                    public void onReceivedError(
                            WebView view,
                            WebResourceRequest request,
                            WebResourceError error
                    ) {


                        super.onReceivedError(
                                view,
                                request,
                                error
                        );


                        if (request == null) {

                            return;
                        }


                        /*
                         * ONLY HANDLE THE MAIN DOCUMENT HERE.
                         *
                         * IMPORTANT:
                         *
                         * Failed iframe requests should NOT
                         * replace the whole Deeprowss website.
                         */

                        if (!request.isForMainFrame()) {

                            return;
                        }


                        webPageVisible =
                                true;


                        hideCustomSplash();


                        view.stopLoading();


                        view.post(
                                () -> showOfflinePage()
                        );
                    }


                    /*
                     * =============================================
                     * OLD ANDROID ERROR HANDLING
                     * =============================================
                     */

                    @Override
                    public void onReceivedError(
                            WebView view,
                            int errorCode,
                            String description,
                            String failingUrl
                    ) {


                        super.onReceivedError(
                                view,
                                errorCode,
                                description,
                                failingUrl
                        );


                        if (android.os.Build.VERSION.SDK_INT < 23) {


                            webPageVisible =
                                    true;


                            hideCustomSplash();


                            showOfflinePage();
                        }
                    }


                    /*
                     * =============================================
                     * WEBVIEW RENDERER CRASH PROTECTION
                     * =============================================
                     */

                    @Override
                    public boolean onRenderProcessGone(
                            WebView view,
                            RenderProcessGoneDetail detail
                    ) {


                        /*
                         * A heavy page caused the WebView
                         * rendering process to stop.
                         *
                         * Returning true prevents Android
                         * from terminating the whole app.
                         */


                        try {

                            if (view != null) {

                                view.destroy();
                            }

                        } catch (Exception ignored) {
                        }


                        webPageVisible =
                                true;


                        hideCustomSplash();


                        new Handler(
                                Looper.getMainLooper()
                        ).postDelayed(
                                () -> rebuildMainWebView(),
                                300
                        );


                        return true;
                    }
                }
        );


        /*
         * =============================================
         * CHROME CLIENT
         * =============================================
         */

        mainWebView.setWebChromeClient(
                createChromeClient()
        );


        /*
         * =============================================
         * PULL TO REFRESH
         * =============================================
         */

        refreshContainer =
                new RefreshableWebViewContainer(
                        this
                );


        refreshContainer.setBackgroundColor(
                BG_COLOR
        );


        refreshContainer.setWebView(
                mainWebView
        );


        refreshContainer.setOnRefreshListener(
                () -> {


                    if (mainWebView == null) {

                        return;
                    }


                    if (showingOfflinePage) {

                        showWebsiteAgain();

                    } else {

                        mainWebView.reload();
                    }


                    new Handler(
                            Looper.getMainLooper()
                    ).postDelayed(
                            () -> {

                                if (refreshContainer != null) {

                                    refreshContainer.stopRefreshing();
                                }

                            },
                            900
                    );
                }
        );


        /*
         * =============================================
         * ADD WEBVIEW
         * =============================================
         */

        refreshContainer.addView(
                mainWebView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );


        rootLayout.addView(
                refreshContainer,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );
    }


    /*
     * =========================================================
     * REBUILD MAIN WEBVIEW
     * =========================================================
     */

    private void rebuildMainWebView() {


        try {

            if (refreshContainer != null &&
                    mainWebView != null) {

                refreshContainer.removeView(
                        mainWebView
                );
            }

        } catch (Exception ignored) {
        }


        try {

            if (mainWebView != null) {

                mainWebView.stopLoading();

                mainWebView.destroy();
            }

        } catch (Exception ignored) {
        }


        mainWebView =
                null;


        if (refreshContainer != null) {

            rootLayout.removeView(
                    refreshContainer
            );

            refreshContainer =
                    null;
        }


        createMainWebView();


        if (mainWebView != null) {

            mainWebView.loadUrl(
                    WEBSITE_URL
            );
        }
    }


    /*
     * =========================================================
     * MAIN URL HANDLER
     * =========================================================
     */

    private boolean handleMainUrl(
            WebView view,
            String url
    ) {


        if (url == null ||
                url.trim().isEmpty()) {

            return false;
        }


        if (showingOfflinePage) {


            showingOfflinePage =
                    false;


            view.loadUrl(
                    url
            );


            return true;
        }


        /*
         * MAIN WEBSITE LINKS STAY
         * IN THE MAIN WEBVIEW.
         */

        if (isDeeprowssUrl(url)) {

            return false;
        }


        /*
         * TELEGRAM OPENS EXTERNALLY.
         */

        if (isExternalExceptionUrl(url)) {

            openExternalUrl(
                    url
            );


            return true;
        }


        /*
         * NON-HTTP SCHEMES
         */

        if (!isHttpUrl(url)) {

            openExternalUrl(
                    url
            );


            return true;
        }


        /*
         * NORMAL EXTERNAL HTTP LINKS
         * OPEN IN POPUP WEBVIEW.
         */

        openPopup(
                url
        );


        return true;
    }


    /*
     * =========================================================
     * DEEPROWSS URL CHECK
     * =========================================================
     */

    private boolean isDeeprowssUrl(
            String url
    ) {


        if (url == null) {

            return false;
        }


        try {

            Uri uri =
                    Uri.parse(
                            url
                    );


            String host =
                    uri.getHost();


            if (host == null) {

                return false;
            }


            host =
                    host.toLowerCase(
                            Locale.US
                    );


            return host.equals(
                    "deeprowss.com"
            )
                    ||
                    host.equals(
                            "www.deeprowss.com"
                    )
                    ||
                    host.equals(
                            "deeprows.github.io"
                    );

        } catch (Exception ignored) {

            return false;
        }
    }


    /*
     * =========================================================
     * WEBVIEW SETTINGS
     * =========================================================
     */

    private void configureWebView(
            WebView webView
    ) {


        WebSettings settings =
                webView.getSettings();


        settings.setJavaScriptEnabled(
                true
        );


        settings.setDomStorageEnabled(
                true
        );


        settings.setDatabaseEnabled(
                true
        );


        settings.setJavaScriptCanOpenWindowsAutomatically(
                true
        );


        settings.setSupportMultipleWindows(
                true
        );


        settings.setMediaPlaybackRequiresUserGesture(
                false
        );


        settings.setAllowFileAccess(
                true
        );


        settings.setAllowContentAccess(
                true
        );


        settings.setBuiltInZoomControls(
                false
        );


        settings.setDisplayZoomControls(
                false
        );


        settings.setLoadWithOverviewMode(
                true
        );


        settings.setUseWideViewPort(
                true
        );


        settings.setTextZoom(
                100
        );


        settings.setDefaultTextEncodingName(
                "UTF-8"
        );


        settings.setSupportZoom(
                false
        );


        /*
         * Prevent old cached website files.
         */

        settings.setCacheMode(
                WebSettings.LOAD_NO_CACHE
        );


        /*
         * MIXED CONTENT SUPPORT
         */

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.LOLLIPOP) {

            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );
        }


        webView.setClickable(
                true
        );


        webView.setFocusable(
                true
        );


        webView.setFocusableInTouchMode(
                true
        );


        webView.setEnabled(
                true
        );


        /*
         * DOWNLOADS
         */

        webView.setDownloadListener(
                new DownloadListener() {

                    @Override
                    public void onDownloadStart(
                            String url,
                            String userAgent,
                            String contentDisposition,
                            String mimeType,
                            long contentLength
                    ) {

                        handleWebDownload(
                                url,
                                userAgent,
                                contentDisposition,
                                mimeType
                        );
                    }
                }
        );


        /*
         * COOKIES
         */

        CookieManager cookieManager =
                CookieManager.getInstance();


        cookieManager.setAcceptCookie(
                true
        );


        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.LOLLIPOP) {

            cookieManager.setAcceptThirdPartyCookies(
                    webView,
                    true
            );
        }
    }


    /*
     * =========================================================
     * DOWNLOAD HANDLER
     * =========================================================
     */

    private void handleWebDownload(
            String url,
            String userAgent,
            String contentDisposition,
            String mimeType
    ) {


        if (url == null ||
                url.trim().isEmpty()) {

            return;
        }


        try {


            if (!isHttpUrl(url)) {

                openExternalUrl(
                        url
                );

                return;
            }


            DownloadManager.Request request =
                    new DownloadManager.Request(
                            Uri.parse(
                                    url
                            )
                    );


            if (mimeType != null &&
                    !mimeType.trim().isEmpty()) {

                request.setMimeType(
                        mimeType
                );
            }


            if (userAgent != null &&
                    !userAgent.trim().isEmpty()) {

                request.addRequestHeader(
                        "User-Agent",
                        userAgent
                );
            }


            String cookies =
                    CookieManager.getInstance()
                            .getCookie(
                                    url
                            );


            if (cookies != null &&
                    !cookies.trim().isEmpty()) {

                request.addRequestHeader(
                        "Cookie",
                        cookies
                );
            }


            request.setDescription(
                    "Downloading from Deeprowss"
            );


            request.setNotificationVisibility(
                    DownloadManager.Request
                            .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );


            String fileName =
                    android.webkit.URLUtil.guessFileName(
                            url,
                            contentDisposition,
                            mimeType
                    );


            if (fileName == null ||
                    fileName.trim().isEmpty()) {

                fileName =
                        "deeprowss_download";
            }


            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
            );


            DownloadManager manager =
                    (DownloadManager)
                            getSystemService(
                                    DOWNLOAD_SERVICE
                            );


            if (manager != null) {

                manager.enqueue(
                        request
                );
            }

        } catch (Exception ignored) {

            /*
             * Never crash the app
             * because of an unsupported download.
             */
        }
    }


    /*
     * =========================================================
     * OFFLINE PAGE
     * =========================================================
     */

    private void showOfflinePage() {


        if (mainWebView == null) {

            return;
        }


        if (showingOfflinePage) {

            return;
        }


        showingOfflinePage =
                true;


        String offlineHtml =

                "<!DOCTYPE html>" +

                "<html>" +

                "<head>" +

                "<meta charset='UTF-8'>" +

                "<meta name='viewport' " +
                "content='width=device-width," +
                "initial-scale=1.0," +
                "maximum-scale=1.0," +
                "user-scalable=no'>" +

                "<style>" +

                "html,body{" +
                "margin:0;" +
                "padding:0;" +
                "width:100%;" +
                "height:100%;" +
                "background:#07090d;" +
                "color:#fff;" +
                "font-family:Arial,sans-serif;" +
                "overflow:hidden;" +
                "}" +

                "body{" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "text-align:center;" +
                "}" +

                ".box{" +
                "width:88%;" +
                "max-width:420px;" +
                "padding:30px 20px;" +
                "box-sizing:border-box;" +
                "}" +

                ".logo{" +
                "width:72px;" +
                "height:72px;" +
                "margin:0 auto 22px;" +
                "border-radius:20px;" +
                "background:#ff1744;" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "font-size:32px;" +
                "font-weight:800;" +
                "color:#fff;" +
                "}" +

                "h1{" +
                "font-size:25px;" +
                "font-weight:700;" +
                "margin:0 0 12px;" +
                "}" +

                "p{" +
                "font-size:15px;" +
                "line-height:1.6;" +
                "color:#9299a8;" +
                "margin:0 0 28px;" +
                "}" +

                "button{" +
                "border:0;" +
                "outline:none;" +
                "border-radius:12px;" +
                "background:#ff1744;" +
                "color:#fff;" +
                "font-size:15px;" +
                "font-weight:700;" +
                "padding:14px 30px;" +
                "min-width:150px;" +
                "}" +

                "</style>" +

                "</head>" +

                "<body>" +

                "<div class='box'>" +

                "<div class='logo'>D</div>" +

                "<h1>Connection problem</h1>" +

                "<p>" +

                "We couldn't connect to Deeprowss right now." +

                "<br>" +

                "Please check your internet connection " +
                "and try again." +

                "</p>" +

                "<button " +

                "onclick='location.href=\"" +
                WEBSITE_URL +
                "\"'>" +

                "TRY AGAIN" +

                "</button>" +

                "</div>" +

                "</body>" +

                "</html>";


        mainWebView.setBackgroundColor(
                BG_COLOR
        );


        mainWebView.loadDataWithBaseURL(
                WEBSITE_URL,
                offlineHtml,
                "text/html",
                "UTF-8",
                null
        );
    }


    /*
     * =========================================================
     * TRY WEBSITE AGAIN
     * =========================================================
     */

    private void showWebsiteAgain() {


        showingOfflinePage =
                false;


        if (mainWebView != null) {

            mainWebView.setBackgroundColor(
                    BG_COLOR
            );


            mainWebView.loadUrl(
                    WEBSITE_URL
            );
        }
    }


    /*
     * =========================================================
     * HTTP URL CHECK
     * =========================================================
     */

    private boolean isHttpUrl(
            String url
    ) {


        if (url == null) {

            return false;
        }


        String value =
                url.trim()
                        .toLowerCase(
                                Locale.US
                        );


        return value.startsWith(
                "http://"
        )
                ||
                value.startsWith(
                        "https://"
                );
    }


    /*
     * =========================================================
     * EXTERNAL URL CHECK
     * =========================================================
     */

    private boolean isExternalExceptionUrl(
            String url
    ) {


        if (url == null ||
                url.trim().isEmpty()) {

            return false;
        }


        try {


            Uri uri =
                    Uri.parse(
                            url
                    );


            String host =
                    uri.getHost();


            if (host == null) {

                return false;
            }


            host =
                    host.toLowerCase(
                            Locale.US
                    );


            return host.equals(
                    "t.me"
            )
                    ||
                    host.equals(
                            "telegram.me"
                    )
                    ||
                    host.equals(
                            "www.telegram.me"
                    );

        } catch (Exception ignored) {

            return false;
        }
    }


    /*
     * =========================================================
     * OPEN EXTERNAL URL
     * =========================================================
     */

    private void openExternalUrl(
            String url
    ) {


        if (url == null ||
                url.trim().isEmpty()) {

            return;
        }


        try {


            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                    url
                            )
                    );


            startActivity(
                    intent
            );

        } catch (Exception ignored) {

            /*
             * IMPORTANT:
             *
             * Do NOT call openPopup() here.
             *
             * This prevents a recursive loop for
             * unsupported schemes.
             */
        }
    }


    /*
     * =========================================================
     * NATIVE MEDIA URL CHECK
     * =========================================================
     */

    private boolean isDirectMediaUrl(
            String url
    ) {


        if (url == null ||
                url.trim().isEmpty()) {

            return false;
        }


        String u =
                url.toLowerCase(
                        Locale.US
                );


        int queryIndex =
                u.indexOf(
                        '?'
                );


        if (queryIndex >= 0) {

            u =
                    u.substring(
                            0,
                            queryIndex
                    );
        }


        return u.endsWith(
                ".mp4"
        )
                ||
                u.endsWith(
                        ".m4v"
                )
                ||
                u.endsWith(
                        ".webm"
                )
                ||
                u.endsWith(
                        ".m3u8"
                )
                ||
                u.endsWith(
                        ".mpd"
                );
    }


    /*
     * =========================================================
     * NATIVE MEDIA PLAYER
     * =========================================================
     */

    private void playNativeMedia(
            String url
    ) {


        if (url == null ||
                url.trim().isEmpty()) {

            return;
        }


        releaseNativePlayer();


        nativePlayer =
                new ExoPlayer.Builder(
                        this
                ).build();


        nativePlayerView =
                new PlayerView(
                        this
                );


        nativePlayerView.setUseController(
                true
        );


        nativePlayerView.setPlayer(
                nativePlayer
        );


        nativePlayerView.setBackgroundColor(
                Color.BLACK
        );


        MediaItem mediaItem =
                MediaItem.fromUri(
                        Uri.parse(
                                url
                        )
                );


        nativePlayer.setMediaItem(
                mediaItem
        );


        nativePlayer.prepare();


        nativePlayer.setPlayWhenReady(
                true
        );


        nativePlayerFullscreen =
                true;


        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.GONE
            );
        }


        if (popupContainer != null) {

            popupContainer.setVisibility(
                    View.GONE
            );
        }


        rootLayout.addView(
                nativePlayerView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );


        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        );


        hideSystemBarsForFullscreen();


        nativePlayer.addListener(
                new Player.Listener() {

                    @Override
                    public void onPlaybackStateChanged(
                            int playbackState
                    ) {

                        if (playbackState ==
                                Player.STATE_ENDED) {

                            exitNativeMedia();
                        }
                    }
                }
        );
    }


    private void exitNativeMedia() {


        if (!nativePlayerFullscreen &&
                nativePlayerView == null) {

            return;
        }


        releaseNativePlayer();


        nativePlayerFullscreen =
                false;


        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );


        if (popupContainer != null) {

            popupContainer.setVisibility(
                    View.VISIBLE
            );

        } else if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.VISIBLE
            );
        }


        showStatusBar();
    }


    private void releaseNativePlayer() {


        if (nativePlayer != null) {

            try {

                nativePlayer.stop();

            } catch (Exception ignored) {
            }


            nativePlayer.release();


            nativePlayer =
                    null;
        }


        if (nativePlayerView != null) {

            try {


                nativePlayerView.setPlayer(
                        null
                );


                rootLayout.removeView(
                        nativePlayerView
                );

            } catch (Exception ignored) {
            }


            nativePlayerView =
                    null;
        }
    }


    /*
     * =========================================================
     * CHROME CLIENT
     * =========================================================
     */

    private WebChromeClient createChromeClient() {


        return new WebChromeClient() {


            @Override
            public boolean onCreateWindow(
                    WebView view,
                    boolean isDialog,
                    boolean isUserGesture,
                    android.os.Message resultMsg
            ) {


                WebView popup =
                        createPopupWebView();


                WebView.WebViewTransport transport =
                        (WebView.WebViewTransport)
                                resultMsg.obj;


                transport.setWebView(
                        popup
                );


                resultMsg.sendToTarget();


                return true;
            }


            @Override
            public void onShowCustomView(
                    View view,
                    CustomViewCallback callback
            ) {


                showVideoFullscreen(
                        view,
                        callback
                );
            }


            @Override
            public void onHideCustomView() {

                exitVideoFullscreen();
            }
        };
    }


    /*
     * =========================================================
     * VIDEO FULLSCREEN
     * =========================================================
     */

    private void showVideoFullscreen(
            View view,
            WebChromeClient.CustomViewCallback callback
    ) {


        if (customVideoView != null) {

            callback.onCustomViewHidden();

            return;
        }


        customVideoView =
                view;


        customViewCallback =
                callback;


        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.GONE
            );
        }


        if (popupContainer != null) {

            popupContainer.setVisibility(
                    View.GONE
            );
        }


        rootLayout.addView(
                customVideoView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );


        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        );


        hideSystemBarsForFullscreen();
    }


    private void exitVideoFullscreen() {


        if (customVideoView == null) {

            return;
        }


        rootLayout.removeView(
                customVideoView
        );


        customVideoView =
                null;


        if (customViewCallback != null) {


            customViewCallback.onCustomViewHidden();


            customViewCallback =
                    null;
        }


        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );


        if (popupContainer != null) {

            popupContainer.setVisibility(
                    View.VISIBLE
            );

        } else if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.VISIBLE
            );
        }


        showStatusBar();
    }


    /*
     * =========================================================
     * OPEN POPUP
     * =========================================================
     */

    private void openPopup(
            String url
    ) {


        if (url == null ||
                url.trim().isEmpty()) {

            return;
        }


        String targetUrl =
                url.trim();


        /*
         * Unsupported schemes go to Android.
         */

        if (!isHttpUrl(targetUrl)) {

            openExternalUrl(
                    targetUrl
            );

            return;
        }


        WebView browserWindow =
                createPopupWebView();


        browserWindow.loadUrl(
                targetUrl
        );
    }


    /*
     * =========================================================
     * CREATE POPUP WEBVIEW
     * =========================================================
     */

    private WebView createPopupWebView() {


        final WebView newWebView =
                new WebView(
                        this
                );


        newWebView.setBackgroundColor(
                Color.BLACK
        );


        newWebView.setClickable(
                true
        );


        newWebView.setFocusable(
                true
        );


        newWebView.setFocusableInTouchMode(
                true
        );


        newWebView.setEnabled(
                true
        );


        configureWebView(
                newWebView
        );


        /*
         * Chrome-like user agent.
         */

        try {


            String defaultUa =
                    newWebView.getSettings()
                            .getUserAgentString();


            if (defaultUa != null &&
                    !defaultUa.toLowerCase(
                            Locale.US
                    ).contains(
                            "chrome/"
                    )) {


                String chromeUa =
                        defaultUa +
                                " Chrome/131.0.0.0 Mobile Safari/537.36";


                newWebView.getSettings()
                        .setUserAgentString(
                                chromeUa
                        );
            }

        } catch (Exception ignored) {
        }


        /*
         * =====================================================
         * POPUP WEBVIEW CLIENT
         * =====================================================
         */

        newWebView.setWebViewClient(
                new WebViewClient() {


                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request
                    ) {


                        if (request == null ||
                                request.getUrl() == null) {

                            return false;
                        }


                        String url =
                                request.getUrl()
                                        .toString();


                        if (!isHttpUrl(url)) {

                            openExternalUrl(
                                    url
                            );

                            return true;
                        }


                        return false;
                    }


                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            String url
                    ) {


                        if (url == null ||
                                url.trim().isEmpty()) {

                            return false;
                        }


                        if (!isHttpUrl(url)) {

                            openExternalUrl(
                                    url
                            );

                            return true;
                        }


                        return false;
                    }


                    @Override
                    public void onPageStarted(
                            WebView view,
                            String url,
                            Bitmap favicon
                    ) {


                        super.onPageStarted(
                                view,
                                url,
                                favicon
                        );


                        view.setBackgroundColor(
                                Color.BLACK
                        );


                        view.bringToFront();
                    }


                    @Override
                    public void onPageFinished(
                            WebView view,
                            String url
                    ) {


                        super.onPageFinished(
                                view,
                                url
                        );


                        view.setBackgroundColor(
                                Color.BLACK
                        );


                        view.setFocusable(
                                true
                        );


                        view.setFocusableInTouchMode(
                                true
                        );


                        view.requestFocus(
                                View.FOCUS_DOWN
                        );
                    }


                    /*
                     * =============================================
                     * POPUP MAIN FRAME ERROR
                     * =============================================
                     *
                     * This prevents Android WebView's error page
                     * from displaying the failed video/page URL.
                     */

                    @Override
                    public void onReceivedError(
                            WebView view,
                            WebResourceRequest request,
                            WebResourceError error
                    ) {


                        super.onReceivedError(
                                view,
                                request,
                                error
                        );


                        if (request == null ||
                                !request.isForMainFrame()) {

                            return;
                        }


                        view.stopLoading();


                        view.post(
                                () -> showPopupErrorPage(
                                        view
                                )
                        );
                    }


                    /*
                     * =============================================
                     * OLD ANDROID ERROR HANDLING
                     * =============================================
                     */

                    @Override
                    public void onReceivedError(
                            WebView view,
                            int errorCode,
                            String description,
                            String failingUrl
                    ) {


                        super.onReceivedError(
                                view,
                                errorCode,
                                description,
                                failingUrl
                        );


                        if (android.os.Build.VERSION.SDK_INT < 23) {

                            showPopupErrorPage(
                                    view
                            );
                        }
                    }


                    /*
                     * =============================================
                     * WEBVIEW RENDERER CRASH
                     * =============================================
                     *
                     * Important for heavy streaming websites.
                     */

                    @Override
                    public boolean onRenderProcessGone(
                            WebView view,
                            RenderProcessGoneDetail detail
                    ) {


                        handlePopupRendererCrash(
                                view
                        );


                        return true;
                    }


                    @Override
                    public boolean shouldOverrideKeyEvent(
                            WebView view,
                            android.view.KeyEvent event
                    ) {

                        return false;
                    }
                }
        );


        newWebView.setWebChromeClient(
                createChromeClient()
        );


        popupWebViewStack.add(
                newWebView
        );


        popupWebView =
                newWebView;


        showPopupContainer();


        attachPopupWebView(
                newWebView
        );


        newWebView.setVisibility(
                View.VISIBLE
        );


        newWebView.bringToFront();


        newWebView.post(
                () -> {


                    newWebView.requestFocus(
                            View.FOCUS_DOWN
                    );


                    newWebView.bringToFront();
                }
        );


        return newWebView;
    }


    /*
     * =========================================================
     * POPUP ERROR PAGE
     * =========================================================
     */

    private void showPopupErrorPage(
            WebView webView
    ) {


        if (webView == null) {

            return;
        }


        String errorHtml =

                "<!DOCTYPE html>" +

                "<html>" +

                "<head>" +

                "<meta charset='UTF-8'>" +

                "<meta name='viewport' " +
                "content='width=device-width,initial-scale=1.0'>" +

                "<style>" +

                "html,body{" +
                "margin:0;" +
                "padding:0;" +
                "width:100%;" +
                "height:100%;" +
                "background:#000;" +
                "color:#fff;" +
                "font-family:Arial,sans-serif;" +
                "}" +

                "body{" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "text-align:center;" +
                "}" +

                ".box{" +
                "padding:30px;" +
                "max-width:420px;" +
                "}" +

                ".icon{" +
                "font-size:38px;" +
                "margin-bottom:15px;" +
                "}" +

                "h2{" +
                "font-size:22px;" +
                "margin:0 0 12px;" +
                "}" +

                "p{" +
                "font-size:15px;" +
                "line-height:1.6;" +
                "color:#a0a0a0;" +
                "margin:0 0 24px;" +
                "}" +

                "button{" +
                "border:0;" +
                "border-radius:10px;" +
                "background:#ff1744;" +
                "color:white;" +
                "padding:13px 28px;" +
                "font-weight:bold;" +
                "font-size:15px;" +
                "}" +

                "</style>" +

                "</head>" +

                "<body>" +

                "<div class='box'>" +

                "<div class='icon'>⚠</div>" +

                "<h2>Unable to load channel</h2>" +

                "<p>" +

                "Please check your internet connection " +
                "and try again."

                +

                "</p>" +

                "<button onclick='location.reload()'>" +

                "TRY AGAIN" +

                "</button>" +

                "</div>" +

                "</body>" +

                "</html>";


        webView.loadDataWithBaseURL(
                null,
                errorHtml,
                "text/html",
                "UTF-8",
                null
        );
    }


    /*
     * =========================================================
     * HANDLE POPUP RENDERER CRASH
     * =========================================================
     */

    private void handlePopupRendererCrash(
            WebView failedWebView
    ) {


        new Handler(
                Looper.getMainLooper()
        ).post(
                () -> {


                    if (failedWebView == null) {

                        return;
                    }


                    try {

                        if (popupContainer != null) {

                            popupContainer.removeView(
                                    failedWebView
                            );
                        }

                    } catch (Exception ignored) {
                    }


                    popupWebViewStack.remove(
                            failedWebView
                    );


                    try {

                        failedWebView.stopLoading();

                        failedWebView.destroy();

                    } catch (Exception ignored) {
                    }


                    if (!popupWebViewStack.isEmpty()) {


                        popupWebView =
                                popupWebViewStack.get(
                                        popupWebViewStack.size() - 1
                                );


                        attachPopupWebView(
                                popupWebView
                        );


                    } else {

                        closePopup();
                    }

                }
        );
    }


    /*
     * =========================================================
     * ATTACH POPUP WEBVIEW
     * =========================================================
     */

    private void attachPopupWebView(
            WebView webView
    ) {


        if (popupContainer == null ||
                webView == null) {

            return;
        }


        if (webView.getParent()
                instanceof android.view.ViewGroup) {


            ((android.view.ViewGroup)
                    webView.getParent())

                    .removeView(
                            webView
                    );
        }


        for (WebView existing :
                popupWebViewStack) {


            if (existing != webView) {

                existing.setVisibility(
                        View.GONE
                );
            }
        }


        FrameLayout.LayoutParams webParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );


        webParams.topMargin =
                popupBarHeight;


        popupContainer.addView(
                webView,
                webParams
        );


        webView.setVisibility(
                View.VISIBLE
        );


        webView.bringToFront();
    }


    /*
     * =========================================================
     * PREVIOUS POPUP WINDOW
     * =========================================================
     */

    private void switchToPreviousPopupWindow() {


        if (popupWebViewStack.size() <= 1) {

            closePopup();

            return;
        }


        WebView current =
                popupWebViewStack.remove(
                        popupWebViewStack.size() - 1
                );


        try {


            if (popupContainer != null) {

                popupContainer.removeView(
                        current
                );
            }


            current.stopLoading();

            current.onPause();

            current.destroy();

        } catch (Exception ignored) {
        }


        popupWebView =
                popupWebViewStack.get(
                        popupWebViewStack.size() - 1
                );


        attachPopupWebView(
                popupWebView
        );


        popupWebView.onResume();


        popupWebView.post(
                () -> {


                    popupWebView.requestFocus(
                            View.FOCUS_DOWN
                    );


                    popupWebView.bringToFront();
                }
        );
    }


    /*
     * =========================================================
     * POPUP CONTAINER
     * =========================================================
     */

    private void showPopupContainer() {


        if (popupContainer != null) {

            return;
        }


        popupContainer =
                new FrameLayout(
                        this
                );


        popupContainer.setBackgroundColor(
                Color.BLACK
        );


        LinearLayout topBar =
                new LinearLayout(
                        this
                );


        topBar.setOrientation(
                LinearLayout.HORIZONTAL
        );


        topBar.setGravity(
                Gravity.CENTER_VERTICAL
        );


        topBar.setPadding(
                dp(4),
                0,
                dp(4),
                0
        );


        topBar.setBackgroundColor(
                Color.rgb(
                        15,
                        18,
                        24
                )
        );


        /*
         * BACK BUTTON
         */

        ImageButton backButton =
                new ImageButton(
                        this
                );


        backButton.setImageResource(
                android.R.drawable.ic_media_previous
        );


        backButton.setBackgroundColor(
                Color.TRANSPARENT
        );


        backButton.setColorFilter(
                Color.WHITE
        );


        backButton.setContentDescription(
                "Back"
        );


        backButton.setOnClickListener(
                v -> {


                    if (popupWebView != null &&
                            popupWebView.canGoBack()) {

                        popupWebView.goBack();

                    } else {

                        switchToPreviousPopupWindow();
                    }
                }
        );


        /*
         * TITLE
         */

        TextView title =
                new TextView(
                        this
                );


        title.setText(
                "Deeprowss"
        );


        title.setTextColor(
                Color.WHITE
        );


        title.setTextSize(
                15
        );


        title.setGravity(
                Gravity.CENTER_VERTICAL
        );


        title.setSingleLine(
                true
        );


        title.setPadding(
                dp(8),
                0,
                dp(8),
                0
        );


        /*
         * CLOSE BUTTON
         */

        ImageButton closeButton =
                new ImageButton(
                        this
                );


        closeButton.setImageResource(
                android.R.drawable.ic_menu_close_clear_cancel
        );


        closeButton.setBackgroundColor(
                Color.TRANSPARENT
        );


        closeButton.setColorFilter(
                Color.WHITE
        );


        closeButton.setContentDescription(
                "Close"
        );


        closeButton.setOnClickListener(
                v -> closePopup()
        );


        topBar.addView(
                backButton,
                new LinearLayout.LayoutParams(
                        popupBarHeight,
                        popupBarHeight
                )
        );


        topBar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        popupBarHeight,
                        1
                )
        );


        topBar.addView(
                closeButton,
                new LinearLayout.LayoutParams(
                        popupBarHeight,
                        popupBarHeight
                )
        );


        FrameLayout.LayoutParams barParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        popupBarHeight,
                        Gravity.TOP
                );


        popupContainer.addView(
                topBar,
                barParams
        );


        FrameLayout.LayoutParams popupParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );


        rootLayout.addView(
                popupContainer,
                popupParams
        );


        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.INVISIBLE
            );
        }


        /*
         * STATUS BAR REMAINS VISIBLE.
         */

        showStatusBar();
    }


    /*
     * =========================================================
     * CLOSE POPUP
     * =========================================================
     */

    private void closePopup() {


        if (popupContainer == null) {

            return;
        }


        if (customVideoView != null) {

            exitVideoFullscreen();
        }


        for (WebView browserWindow :
                new ArrayList<>(
                        popupWebViewStack
                )) {


            try {


                if (popupContainer != null) {

                    popupContainer.removeView(
                            browserWindow
                    );
                }


                browserWindow.stopLoading();

                browserWindow.onPause();

                browserWindow.destroy();

            } catch (Exception ignored) {
            }
        }


        popupWebViewStack.clear();


        popupWebView =
                null;


        rootLayout.removeView(
                popupContainer
        );


        popupContainer =
                null;


        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.VISIBLE
            );
        }


        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );


        showStatusBar();
    }


    /*
     * =========================================================
     * STATUS BAR
     * =========================================================
     */

    private void showStatusBar() {


        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );


        getWindow()
                .getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );


        getWindow().setStatusBarColor(
                BG_COLOR
        );


        getWindow().setNavigationBarColor(
                BG_COLOR
        );
    }


    /*
     * =========================================================
     * FULLSCREEN VIDEO
     * =========================================================
     */

    private void hideSystemBarsForFullscreen() {


        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );


        getWindow()
                .getDecorView()
                .setSystemUiVisibility(

                        View.SYSTEM_UI_FLAG_FULLSCREEN

                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                );
    }


    /*
     * =========================================================
     * BACK BUTTON
     * =========================================================
     */

    @Override
    public void onBackPressed() {


        if (nativePlayerFullscreen) {

            exitNativeMedia();

            return;
        }


        if (customVideoView != null) {

            exitVideoFullscreen();

            return;
        }


        if (popupContainer != null) {


            if (popupWebView != null &&
                    popupWebView.canGoBack()) {

                popupWebView.goBack();

            } else {

                switchToPreviousPopupWindow();
            }


            return;
        }


        if (mainWebView != null &&
                mainWebView.canGoBack()) {

            mainWebView.goBack();

        } else {

            super.onBackPressed();
        }
    }


    /*
     * =========================================================
     * LIFECYCLE
     * =========================================================
     */

    @Override
    protected void onResume() {


        super.onResume();


        if (!nativePlayerFullscreen &&
                customVideoView == null) {

            showStatusBar();
        }


        if (mainWebView != null) {

            mainWebView.onResume();
        }


        if (popupWebView != null) {

            popupWebView.onResume();
        }
    }


    @Override
    protected void onPause() {


        if (mainWebView != null) {

            mainWebView.onPause();
        }


        if (popupWebView != null) {

            popupWebView.onPause();
        }


        super.onPause();
    }


    @Override
    protected void onDestroy() {


        if (customSplashView != null) {


            try {

                rootLayout.removeView(
                        customSplashView
                );

            } catch (Exception ignored) {
            }


            customSplashView =
                    null;
        }


        releaseNativePlayer();


        if (customVideoView != null) {


            try {

                rootLayout.removeView(
                        customVideoView
                );

            } catch (Exception ignored) {
            }


            customVideoView =
                    null;
        }


        for (WebView browserWindow :
                new ArrayList<>(
                        popupWebViewStack
                )) {


            try {

                browserWindow.stopLoading();

                browserWindow.destroy();

            } catch (Exception ignored) {
            }
        }


        popupWebViewStack.clear();


        popupWebView =
                null;


        if (mainWebView != null) {


            try {

                mainWebView.stopLoading();

                mainWebView.destroy();

            } catch (Exception ignored) {
            }


            mainWebView =
                    null;
        }


        super.onDestroy();
    }


    /*
     * =========================================================
     * DP
     * =========================================================
     */

    private int dp(
            int value
    ) {


        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;


        return (int) (
                value * density +
                        0.5f
        );
    }


    /*
     * =========================================================
     * PULL TO REFRESH
     * =========================================================
     */

    private static class RefreshableWebViewContainer
            extends FrameLayout {


        private WebView webView;


        private float startY;


        private boolean dragging;


        private boolean refreshing;


        private ProgressBar progressBar;


        private OnRefreshListener listener;


        private static final float TRIGGER_DISTANCE =
                180f;


        private static final float MAX_PULL_DISTANCE =
                300f;


        interface OnRefreshListener {

            void onRefresh();
        }


        RefreshableWebViewContainer(
                Context context
        ) {


            super(
                    context
            );


            setClipChildren(
                    false
            );


            progressBar =
                    new ProgressBar(
                            context
                    );


            progressBar.setVisibility(
                    View.GONE
            );


            LayoutParams progressParams =
                    new LayoutParams(
                            dp(
                                    context,
                                    42
                            ),
                            dp(
                                    context,
                                    42
                            )
                    );


            progressParams.gravity =
                    Gravity.TOP
                            |
                            Gravity.CENTER_HORIZONTAL;


            progressParams.topMargin =
                    dp(
                            context,
                            16
                    );


            addView(
                    progressBar,
                    progressParams
            );
        }


        void setWebView(
                WebView webView
        ) {

            this.webView =
                    webView;
        }


        void setOnRefreshListener(
                OnRefreshListener listener
        ) {

            this.listener =
                    listener;
        }


        void stopRefreshing() {


            refreshing =
                    false;


            dragging =
                    false;


            if (progressBar != null) {

                progressBar.setVisibility(
                        View.GONE
                );
            }


            if (webView != null) {

                webView.animate()
                        .translationY(
                                0
                        )
                        .setDuration(
                                180
                        )
                        .start();
            }
        }


        @Override
        public boolean onInterceptTouchEvent(
                MotionEvent event
        ) {


            if (webView == null ||
                    refreshing) {

                return false;
            }


            switch (
                    event.getActionMasked()
            ) {


                case MotionEvent.ACTION_DOWN:


                    startY =
                            event.getY();


                    dragging =
                            false;


                    break;


                case MotionEvent.ACTION_MOVE:


                    float distance =
                            event.getY()
                                    -
                                    startY;


                    if (distance > 0 &&
                            webView.getScrollY() <= 0) {


                        if (distance > 15) {

                            dragging =
                                    true;


                            return true;
                        }
                    }


                    break;


                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_CANCEL:


                    dragging =
                            false;


                    break;
            }


            return false;
        }


        @Override
        public boolean onTouchEvent(
                MotionEvent event
        ) {


            if (webView == null ||
                    refreshing) {

                return true;
            }


            switch (
                    event.getActionMasked()
            ) {


                case MotionEvent.ACTION_DOWN:


                    startY =
                            event.getY();


                    dragging =
                            true;


                    return true;


                case MotionEvent.ACTION_MOVE:


                    float distance =
                            event.getY()
                                    -
                                    startY;


                    if (distance < 0) {

                        distance =
                                0;
                    }


                    if (distance >
                            MAX_PULL_DISTANCE) {

                        distance =
                                MAX_PULL_DISTANCE;
                    }


                    if (distance > 0) {


                        float offset =
                                distance *
                                        0.55f;


                        webView.setTranslationY(
                                offset
                        );


                        if (distance >=
                                TRIGGER_DISTANCE *
                                        0.55f) {


                            progressBar.setVisibility(
                                    View.VISIBLE
                            );

                        } else {


                            progressBar.setVisibility(
                                    View.GONE
                            );
                        }
                    }


                    return true;


                case MotionEvent.ACTION_UP:


                    float finalDistance =
                            event.getY()
                                    -
                                    startY;


                    if (finalDistance >=
                            TRIGGER_DISTANCE) {

                        startRefreshing();

                    } else {

                        stopRefreshing();
                    }


                    return true;


                case MotionEvent.ACTION_CANCEL:


                    stopRefreshing();


                    return true;
            }


            return true;
        }


        private void startRefreshing() {


            if (refreshing) {

                return;
            }


            refreshing =
                    true;


            if (progressBar != null) {

                progressBar.setVisibility(
                        View.VISIBLE
                );
            }


            if (webView != null) {

                webView.animate()
                        .translationY(
                                dp(
                                        getContext(),
                                        70
                                )
                        )
                        .setDuration(
                                150
                        )
                        .start();
            }


            if (listener != null) {

                listener.onRefresh();
            }
        }


        private static int dp(
                Context context,
                int value
        ) {


            float density =
                    context.getResources()
                            .getDisplayMetrics()
                            .density;


            return (int) (
                    value * density +
                            0.5f
            );
        }
    }
}
