/*
 * REQUIRED Gradle dependencies:
 *
 * implementation "androidx.media3:media3-exoplayer:1.8.0"
 * implementation "androidx.media3:media3-ui:1.8.0"
 *
 * Keep the rest of your existing dependencies.
 */

package com.deeprows.football;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
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
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.core.splashscreen.SplashScreen;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private static final String WEBSITE_URL =
            "https://deeprowss.com";

    private static final String TELEGRAM_URL =
            "https://t.me/deeprows";

    private static final int POPUP_BAR_HEIGHT_DP = 58;

    private static final int BG_COLOR =
            Color.rgb(7, 9, 13);

    private FrameLayout rootLayout;

    private RefreshableWebViewContainer refreshContainer;

    private WebView mainWebView;

    private WebView popupWebView;

    private final List<WebView> popupWebViewStack =
            new ArrayList<>();

    private FrameLayout popupContainer;

    private View customVideoView;

    private WebChromeClient.CustomViewCallback customViewCallback;

    /*
     * Native Media3 player.
     */
    private ExoPlayer nativePlayer;

    private PlayerView nativePlayerView;

    private boolean nativePlayerFullscreen = false;

    private int popupBarHeight;

    private boolean showingOfflinePage = false;

    /*
     * =========================================================
     * SPLASH
     * =========================================================
     */

    private boolean webPageVisible = false;

    private View customSplashView;

    private TextView splashLoadingText;

    private ProgressBar splashLoadingProgress;

    private Handler splashAnimationHandler;

    private Runnable splashAnimationRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*
         * Install AndroidX splash screen BEFORE super.onCreate().
         */
        SplashScreen splashScreen =
                SplashScreen.installSplashScreen(this);

        /*
         * Keep Android splash alive until the WebView has
         * committed its first visible page.
         */
        splashScreen.setKeepOnScreenCondition(
                () -> !webPageVisible
        );

        super.onCreate(savedInstanceState);


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

        hideStatusBar();

        popupBarHeight =
                dp(POPUP_BAR_HEIGHT_DP);


        /*
         * =====================================================
         * ROOT
         * =====================================================
         */

        rootLayout =
                new FrameLayout(this);

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
         * MAIN WEBVIEW
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


        /*
         * =====================================================
         * SPLASH CONTAINER
         * =====================================================
         */

        FrameLayout splashContainer =
                new FrameLayout(this);

        splashContainer.setBackgroundColor(
                BG_COLOR
        );


        /*
         * =====================================================
         * SPLASH IMAGE
         * =====================================================
         */

        ImageView splashImage =
                new ImageView(this);

        int splashResource =
                getResources().getIdentifier(
                        "deeprowss_splash",
                        "drawable",
                        getPackageName()
                );

        if (splashResource != 0) {

            splashImage.setImageResource(
                    splashResource
            );
        }

        splashImage.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        splashImage.setAdjustViewBounds(
                true
        );

        splashImage.setBackgroundColor(
                BG_COLOR
        );


        /*
         * Keep splash image at a controlled size.
         */

        FrameLayout.LayoutParams imageParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        dp(330),
                        Gravity.CENTER
                );

        imageParams.setMargins(
                dp(32),
                dp(20),
                dp(32),
                dp(90)
        );

        splashContainer.addView(
                splashImage,
                imageParams
        );


        /*
         * =====================================================
         * LOADING LAYOUT
         * =====================================================
         */

        LinearLayout loadingLayout =
                new LinearLayout(this);

        loadingLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        loadingLayout.setGravity(
                Gravity.CENTER
        );


        /*
         * =====================================================
         * LOADING SPINNER
         * =====================================================
         */

        splashLoadingProgress =
                new ProgressBar(
                        this
                );

        splashLoadingProgress.setIndeterminate(
                true
        );

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        dp(32),
                        dp(32)
                );

        progressParams.gravity =
                Gravity.CENTER;

        loadingLayout.addView(
                splashLoadingProgress,
                progressParams
        );


        /*
         * =====================================================
         * LOADING TEXT
         * =====================================================
         */

        splashLoadingText =
                new TextView(this);

        splashLoadingText.setText(
                "Loading..."
        );

        splashLoadingText.setTextColor(
                Color.WHITE
        );

        splashLoadingText.setTextSize(
                14
        );

        splashLoadingText.setGravity(
                Gravity.CENTER
        );

        splashLoadingText.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        textParams.gravity =
                Gravity.CENTER;

        textParams.topMargin =
                dp(10);

        loadingLayout.addView(
                splashLoadingText,
                textParams
        );


        /*
         * =====================================================
         * LOADING POSITION
         * =====================================================
         */

        FrameLayout.LayoutParams loadingParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_HORIZONTAL |
                        Gravity.BOTTOM
                );

        loadingParams.bottomMargin =
                dp(55);

        splashContainer.addView(
                loadingLayout,
                loadingParams
        );


        /*
         * =====================================================
         * SAVE SPLASH
         * =====================================================
         */

        customSplashView =
                splashContainer;

        rootLayout.addView(
                customSplashView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        customSplashView.bringToFront();


        /*
         * =====================================================
         * LOADING TEXT ANIMATION
         * =====================================================
         *
         * The spinner itself is native Android.
         *
         * The text gently fades in/out to give the loading
         * indicator a polished animated appearance.
         */

        splashAnimationHandler =
                new Handler(
                        Looper.getMainLooper()
                );

        splashAnimationRunnable =
                new Runnable() {

                    @Override
                    public void run() {

                        if (customSplashView == null ||
                                splashLoadingText == null) {

                            return;
                        }

                        splashLoadingText
                                .animate()
                                .alpha(0.35f)
                                .setDuration(550)
                                .withEndAction(
                                        () -> {

                                            if (customSplashView == null ||
                                                    splashLoadingText == null) {

                                                return;
                                            }

                                            splashLoadingText
                                                    .animate()
                                                    .alpha(1f)
                                                    .setDuration(550)
                                                    .withEndAction(
                                                            () -> {

                                                                if (customSplashView != null &&
                                                                        splashAnimationHandler != null &&
                                                                        splashAnimationRunnable != null) {

                                                                    splashAnimationHandler
                                                                            .postDelayed(
                                                                                    splashAnimationRunnable,
                                                                                    50
                                                                            );
                                                                }
                                                            }
                                                    )
                                                    .start();
                                        }
                                )
                                .start();
                    }
                };

        splashAnimationHandler.post(
                splashAnimationRunnable
        );
    }


    /*
     * =========================================================
     * HIDE CUSTOM SPLASH
     * =========================================================
     */

    private void hideCustomSplash() {

        if (customSplashView == null) {
            return;
        }

        /*
         * Stop loading animation.
         */

        if (splashAnimationHandler != null &&
                splashAnimationRunnable != null) {

            splashAnimationHandler.removeCallbacks(
                    splashAnimationRunnable
            );
        }

        View splash =
                customSplashView;

        customSplashView =
                null;

        splash.animate()
                .alpha(0f)
                .setDuration(180)
                .withEndAction(
                        () -> {

                            if (rootLayout != null) {

                                rootLayout.removeView(
                                        splash
                                );
                            }

                            splashLoadingText =
                                    null;

                            splashLoadingProgress =
                                    null;

                            splashAnimationRunnable =
                                    null;

                            splashAnimationHandler =
                                    null;
                        }
                )
                .start();
    }


    /*
     * =========================================================
     * MAIN WEBVIEW
     * =========================================================
     */

    private void createMainWebView() {

        mainWebView =
                new WebView(this);

        mainWebView.setBackgroundColor(
                BG_COLOR
        );

        configureWebView(
                mainWebView
        );


        /*
         * =====================================================
         * WEBSITE NAVIGATION
         * =====================================================
         */

        mainWebView.setWebViewClient(
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


                        if (showingOfflinePage) {

                            showingOfflinePage =
                                    false;

                            view.loadUrl(
                                    url
                            );

                            return true;
                        }


                        handleMainNavigation(
                                url
                        );

                        return true;
                    }


                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            String url
                    ) {

                        if (url == null) {

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


                        handleMainNavigation(
                                url
                        );

                        return true;
                    }


                    @Override
                    public void onPageStarted(
                            WebView view,
                            String url,
                            android.graphics.Bitmap favicon
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
                     * =================================================
                     * SPLASH RELEASE
                     * =================================================
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
                     * =================================================
                     * NETWORK / IFRAME ERROR HANDLING
                     * =================================================
                     *
                     * IMPORTANT:
                     *
                     * Main document failure:
                     *     -> Deeprowss offline page.
                     *
                     * Player/iframe failure:
                     *     -> Only the player area gets an error.
                     *
                     * The failed external URL is NEVER displayed.
                     */

                    @Override
                    public void onReceivedError(
                            WebView view,
                            WebResourceRequest request,
                            android.webkit.WebResourceError error
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
                         * =================================================
                         * MAIN WEBSITE FAILURE
                         * =================================================
                         */

                        if (request.isForMainFrame()) {

                            webPageVisible =
                                    true;

                            hideCustomSplash();

                            view.stopLoading();

                            view.post(
                                    () -> showOfflinePage()
                            );

                            return;
                        }


                        /*
                         * =================================================
                         * PLAYER / IFRAME FAILURE
                         * =================================================
                         */

                        if (isPlayerEmbedUrl(request)) {

                            String failedUrl =
                                    request.getUrl() != null
                                            ? request.getUrl().toString()
                                            : "";

                            view.post(
                                    () -> showPlayerConnectionError(
                                            view,
                                            failedUrl
                                    )
                            );
                        }
                    }


                    /*
                     * Android versions below API 23.
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
                }
        );


        /*
         * =====================================================
         * CHROME CLIENT
         * =====================================================
         */

        mainWebView.setWebChromeClient(
                createChromeClient()
        );


        /*
         * =====================================================
         * PULL TO REFRESH
         * =====================================================
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
                new RefreshableWebViewContainer.OnRefreshListener() {

                    @Override
                    public void onRefresh() {

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

                                        refreshContainer
                                                .stopRefreshing();
                                    }

                                },
                                900
                        );
                    }
                }
        );


        /*
         * Add main WebView.
         */

        refreshContainer.addView(
                mainWebView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );


        /*
         * Add refresh container.
         */

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
     * PLAYER / IFRAME URL DETECTION
     * =========================================================
     */

    private boolean isPlayerEmbedUrl(
            WebResourceRequest request
    ) {

        if (request == null ||
                request.getUrl() == null) {

            return false;
        }

        String url =
                request.getUrl()
                        .toString()
                        .toLowerCase(
                                java.util.Locale.US
                        );

        /*
         * Detect player/movie/stream embed pages.
         */

        return url.contains("/embed/");
    }


    /*
     * =========================================================
     * PLAYER CONNECTION ERROR
     * =========================================================
     *
     * IMPORTANT:
     *
     * The failed URL is used internally only to locate the
     * corresponding iframe.
     *
     * It is NEVER printed to the user.
     */

    private void showPlayerConnectionError(
            WebView webView,
            String failedUrl
    ) {

        if (webView == null) {
            return;
        }


        if (failedUrl == null ||
                failedUrl.trim().isEmpty()) {

            return;
        }


        /*
         * Escape the URL for JavaScript.
         *
         * This URL is NOT shown anywhere.
         */

        String escapedUrl =
                failedUrl
                        .replace(
                                "\\",
                                "\\\\"
                        )
                        .replace(
                                "'",
                                "\\'"
                        )
                        .replace(
                                "\n",
                                ""
                        )
                        .replace(
                                "\r",
                                ""
                        );


        /*
         * =====================================================
         * JAVASCRIPT
         * =====================================================
         *
         * Find the failed iframe.
         *
         * Hide it.
         *
         * Put a local Deeprowss error message in its place.
         */

        String javascript =
                "(function() {" +

                "var frames = document.getElementsByTagName('iframe');" +

                "for (var i = 0; i < frames.length; i++) {" +

                "    var frame = frames[i];" +

                "    var src = frame.getAttribute('src') || '';" +

                "    if (src === '" +
                escapedUrl +
                "' || " +

                "        src.indexOf('" +
                escapedUrl +
                "') === 0 || " +

                "        '" +
                escapedUrl +
                "'.indexOf(src) === 0) {" +

                "        frame.style.display = 'none';" +

                "        var parent = frame.parentElement;" +

                "        if (!parent) continue;" +

                "        parent.style.position = 'relative';" +

                "        var old = parent.querySelector('.deeprowss-player-error');" +

                "        if (old) continue;" +

                "        var box = document.createElement('div');" +

                "        box.className = 'deeprowss-player-error';" +

                "        box.style.cssText = " +

                "'position:absolute;" +
                "left:0;" +
                "top:0;" +
                "width:100%;" +
                "height:100%;" +
                "min-height:220px;" +
                "background:#07090d;" +
                "color:#fff;" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "text-align:center;" +
                "z-index:999999;" +
                "box-sizing:border-box;" +
                "padding:25px;';" +

                "        box.innerHTML =" +

                "'<div style=\"max-width:380px;\">' +" +

                "'<div style=\"font-size:21px;" +
                "font-weight:700;" +
                "margin-bottom:10px;\">" +

                "Unable to connect at this time" +

                "</div>' +" +

                "'<div style=\"font-size:14px;" +
                "line-height:1.6;" +
                "color:#9299a8;" +
                "margin-bottom:20px;\">" +

                "Check your network connection and refresh the page." +

                "</div>' +" +

                "'<button onclick=\"location.reload()\" " +

                "style=\"border:0;" +
                "border-radius:10px;" +
                "background:#ff1744;" +
                "color:#fff;" +
                "padding:12px 25px;" +
                "font-size:14px;" +
                "font-weight:700;\">" +

                "REFRESH" +

                "</button>' +" +

                "'</div>';" +

                "        parent.appendChild(box);" +

                "    }" +

                "}" +

                "})();";


        webView.evaluateJavascript(
                javascript,
                null
        );
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


        /*
         * JavaScript.
         */

        settings.setJavaScriptEnabled(
                true
        );


        /*
         * DOM.
         */

        settings.setDomStorageEnabled(
                true
        );

        settings.setDatabaseEnabled(
                true
        );


        /*
         * Multiple windows.
         */

        settings.setJavaScriptCanOpenWindowsAutomatically(
                true
        );

        settings.setSupportMultipleWindows(
                true
        );


        /*
         * Media.
         */

        settings.setMediaPlaybackRequiresUserGesture(
                false
        );


        /*
         * Content access.
         */

        settings.setAllowFileAccess(
                true
        );

        settings.setAllowContentAccess(
                true
        );


        /*
         * Zoom OFF.
         */

        settings.setBuiltInZoomControls(
                false
        );

        settings.setDisplayZoomControls(
                false
        );

        settings.setSupportZoom(
                false
        );


        /*
         * Website scaling.
         */

        settings.setLoadWithOverviewMode(
                true
        );

        settings.setUseWideViewPort(
                true
        );


        /*
         * Text.
         */

        settings.setTextZoom(
                100
        );

        settings.setDefaultTextEncodingName(
                "UTF-8"
        );


        /*
         * Cache.
         */

        settings.setCacheMode(
                WebSettings.LOAD_NO_CACHE
        );


        /*
         * Mixed content.
         */

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.LOLLIPOP) {

            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );
        }


        /*
         * =====================================================
         * INTERACTION
         * =====================================================
         */

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
         * =====================================================
         * DOWNLOAD
         * =====================================================
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
         * =====================================================
         * COOKIES
         * =====================================================
         */

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(
                true
        );

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );
    }


    /*
     * =========================================================
     * DOWNLOAD
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

            if (isExternalExceptionUrl(url)) {

                openExternalUrl(url);

                return;
            }


            DownloadManager.Request request =
                    new DownloadManager.Request(
                            Uri.parse(url)
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
                    CookieManager
                            .getInstance()
                            .getCookie(url);


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
                    DownloadManager
                            .Request
                            .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );


            String fileName =
                    android.webkit.URLUtil
                            .guessFileName(
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
             * Never crash the app because of a website download.
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

                "<h1>You're offline</h1>" +

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
     * TRY AGAIN
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
     * MAIN NAVIGATION
     * =========================================================
     */

    private void handleMainNavigation(
            String url
    ) {

        if (url == null ||
                url.trim().isEmpty()) {

            return;
        }


        /*
         * External exceptions.
         */

        if (isExternalExceptionUrl(url)) {

            openExternalUrl(url);

            return;
        }


        /*
         * Deeprowss GitHub pages remain inside main WebView.
         */

        if (url.startsWith(
                "https://deeprows.github.io/"
        )) {

            if (mainWebView != null) {

                mainWebView.loadUrl(
                        url
                );
            }

            return;
        }


        /*
         * Everything else uses popup WebView.
         */

        openPopup(
                url
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
                    Uri.parse(url);

            String host =
                    uri.getHost();


            if (host == null) {

                return false;
            }


            host =
                    host.toLowerCase(
                            java.util.Locale.US
                    );


            return host.equals("t.me") ||
                    host.equals("telegram.me") ||
                    host.equals("www.telegram.me");

        } catch (Exception ignored) {

            return false;
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
                                java.util.Locale.US
                        );


        return value.startsWith("http://") ||
                value.startsWith("https://");
    }


    /*
     * =========================================================
     * OPEN EXTERNAL URL
     * =========================================================
     */

    private void openExternalUrl(
            String url
    ) {

        try {

            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(
                    intent
            );

        } catch (Exception ignored) {

            /*
             * If Android cannot handle the scheme,
             * keep it inside popup.
             */

            openPopup(
                    url
            );
        }
    }


    /*
     * =========================================================
     * NATIVE MEDIA3 PLAYER
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
                        java.util.Locale.US
                );


        int queryIndex =
                u.indexOf('?');


        if (queryIndex >= 0) {

            u =
                    u.substring(
                            0,
                            queryIndex
                    );
        }


        return u.endsWith(".mp4") ||
                u.endsWith(".m4v") ||
                u.endsWith(".webm") ||
                u.endsWith(".m3u8") ||
                u.endsWith(".mpd");
    }


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
                        Uri.parse(url)
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


        hideStatusBar();


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


        hideStatusBar();
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


        hideStatusBar();
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


        hideStatusBar();
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
                new WebView(this);


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

        newWebView.setHapticFeedbackEnabled(
                true
        );


        configureWebView(
                newWebView
        );


        /*
         * Modern Chrome-like User Agent.
         */

        try {

            String defaultUa =
                    newWebView
                            .getSettings()
                            .getUserAgentString();


            if (defaultUa != null &&
                    !defaultUa
                            .toLowerCase(
                                    java.util.Locale.US
                            )
                            .contains("chrome/")) {

                String chromeUa =
                        defaultUa +
                        " Chrome/131.0.0.0 Mobile Safari/537.36";


                newWebView
                        .getSettings()
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
                            android.graphics.Bitmap favicon
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
                     * =================================================
                     * POPUP ERROR HANDLING
                     * =================================================
                     *
                     * Never allow Android to display the failing URL.
                     */

                    @Override
                    public void onReceivedError(
                            WebView view,
                            WebResourceRequest request,
                            android.webkit.WebResourceError error
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
                         * Main popup document failed.
                         */

                        if (request.isForMainFrame()) {

                            view.stopLoading();

                            view.post(
                                    () -> showPopupConnectionError(
                                            view
                                    )
                            );

                            return;
                        }


                        /*
                         * Embedded player failed.
                         */

                        if (isPlayerEmbedUrl(request)) {

                            String failedUrl =
                                    request.getUrl() != null
                                            ? request.getUrl().toString()
                                            : "";


                            view.post(
                                    () -> showPlayerConnectionError(
                                            view,
                                            failedUrl
                                    )
                            );
                        }
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


        /*
         * Chrome client.
         */

        newWebView.setWebChromeClient(
                createChromeClient()
        );


        /*
         * Downloads.
         */

        newWebView.setDownloadListener(
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
         * Add popup to stack.
         */

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

                    newWebView.setFocusable(
                            true
                    );

                    newWebView.setFocusableInTouchMode(
                            true
                    );

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
     * POPUP CONNECTION ERROR
     * =========================================================
     */

    private void showPopupConnectionError(
            WebView webView
    ) {

        if (webView == null) {
            return;
        }


        String html =
                "<!DOCTYPE html>" +

                "<html>" +

                "<head>" +

                "<meta name='viewport' " +
                "content='width=device-width," +
                "initial-scale=1.0'>" +

                "<style>" +

                "html,body{" +
                "margin:0;" +
                "padding:0;" +
                "width:100%;" +
                "height:100%;" +
                "background:#07090d;" +
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
                "width:88%;" +
                "max-width:420px;" +
                "}" +

                "h1{" +
                "font-size:23px;" +
                "margin:0 0 12px;" +
                "}" +

                "p{" +
                "font-size:14px;" +
                "line-height:1.6;" +
                "color:#9299a8;" +
                "margin:0 0 25px;" +
                "}" +

                "button{" +
                "border:0;" +
                "border-radius:10px;" +
                "background:#ff1744;" +
                "color:#fff;" +
                "padding:13px 26px;" +
                "font-size:14px;" +
                "font-weight:700;" +
                "}" +

                "</style>" +

                "</head>" +

                "<body>" +

                "<div class='box'>" +

                "<h1>" +
                "Unable to connect at this time" +
                "</h1>" +

                "<p>" +
                "Check your network connection and refresh the page." +
                "</p>" +

                "<button onclick='location.reload()'>" +
                "REFRESH" +
                "</button>" +

                "</div>" +

                "</body>" +

                "</html>";


        webView.loadDataWithBaseURL(
                WEBSITE_URL,
                html,
                "text/html",
                "UTF-8",
                null
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


        /*
         * Hide previous windows.
         */

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
     * SWITCH POPUP WINDOW
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
     * POPUP UI
     * =========================================================
     */

    private void showPopupContainer() {

        if (popupContainer != null) {

            return;
        }


        popupContainer =
                new FrameLayout(this);


        popupContainer.setBackgroundColor(
                Color.BLACK
        );


        LinearLayout topBar =
                new LinearLayout(this);


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
                Color.rgb(15, 18, 24)
        );


        /*
         * =====================================================
         * BACK
         * =====================================================
         */

        ImageButton backButton =
                new ImageButton(this);


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
         * =====================================================
         * TITLE
         * =====================================================
         */

        TextView title =
                new TextView(this);


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
         * =====================================================
         * CLOSE
         * =====================================================
         */

        ImageButton closeButton =
                new ImageButton(this);


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


        /*
         * Add buttons.
         */

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


        /*
         * =====================================================
         * TOP BAR
         * =====================================================
         */

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


        /*
         * =====================================================
         * POPUP CONTAINER
         * =====================================================
         */

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


        hideStatusBar();
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


        /*
         * Destroy every popup.
         */

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


        hideStatusBar();
    }


    /*
     * =========================================================
     * STATUS BAR
     * =========================================================
     */

    private void hideStatusBar() {

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

        hideStatusBar();


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

        /*
         * Stop splash animation.
         */

        if (splashAnimationHandler != null &&
                splashAnimationRunnable != null) {

            splashAnimationHandler.removeCallbacks(
                    splashAnimationRunnable
            );
        }


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


        /*
         * Destroy popup WebViews.
         */

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


        /*
         * Destroy main WebView.
         */

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
                value * density + 0.5f
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

            super(context);


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
                            dp(context, 42),
                            dp(context, 42)
                    );


            progressParams.gravity =
                    Gravity.TOP |
                    Gravity.CENTER_HORIZONTAL;


            progressParams.topMargin =
                    dp(context, 16);


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
                        .translationY(0)
                        .setDuration(180)
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
                            event.getY() -
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
                            event.getY() -
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
                                distance * 0.55f;


                        webView.setTranslationY(
                                offset
                        );


                        if (distance >=
                                TRIGGER_DISTANCE * 0.55f) {

                            progressBar
                                    .setVisibility(
                                            View.VISIBLE
                                    );

                        } else {

                            progressBar
                                    .setVisibility(
                                            View.GONE
                                    );
                        }
                    }


                    return true;


                case MotionEvent.ACTION_UP:

                    float finalDistance =
                            event.getY() -
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
                        .setDuration(150)
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
                    value * density + 0.5f
            );
        }
    }
}
