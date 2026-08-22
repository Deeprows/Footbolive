package com.deeprows.football;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import androidx.core.splashscreen.SplashScreen;

public class MainActivity extends Activity {

    private static final String WEBSITE_URL =
            "https://deeprowss.com";

    /*
     * =========================================================
     * EXTERNAL LINK EXCEPTIONS
     * =========================================================
     *
     * These links bypass the in-app popup WebView and are opened
     * externally by Android.
     *
     * Telegram is included so the Telegram app/browser can handle
     * the link normally.
     *
     * Replace OTHER_EXTERNAL_URL with your second link.
     */
    private static final String TELEGRAM_URL =
            "https://t.me/deeprows";

    private static final int POPUP_BAR_HEIGHT_DP = 58;

    private static final int BG_COLOR =
            Color.rgb(7, 9, 13);

    private FrameLayout rootLayout;

    private RefreshableWebViewContainer refreshContainer;

    private WebView mainWebView;

    private WebView popupWebView;
    private final List<WebView> popupWebViewStack = new ArrayList<>();

    private FrameLayout popupContainer;

    private View customVideoView;

    private WebChromeClient.CustomViewCallback customViewCallback;

    private int popupBarHeight;

    private boolean showingOfflinePage = false;

    /*
     * =========================================================
     * SPLASH SCREEN
     * =========================================================
     *
     * The Android splash screen remains visible until the
     * WebView has committed its first visible page content.
     *
     * There is NO fixed splash delay.
     */
    private boolean webPageVisible = false;

    /*
     * Custom splash overlay.
     *
     * The complete deeprowss_splash.png is shown centered at a normal size
     * instead of being treated as the Android 12 splash icon.
     */
    private View customSplashView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*
         * Install the AndroidX splash screen BEFORE super.onCreate().
         */
        SplashScreen splashScreen =
                SplashScreen.installSplashScreen(this);

        /*
         * Keep the splash screen visible while the WebView is
         * loading the first visible webpage.
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
         * CUSTOM DEEPROWSS SPLASH
         * =====================================================
         *
         * Android 12+ treats windowSplashScreenAnimatedIcon as an icon.
         * Our deeprowss_splash.png is a complete splash composition, so show
         * the whole image in a centered ImageView instead.
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
         * Load Deeprowss.
         *
         * The splash screen stays visible until the WebView
         * reports that visible webpage content is ready.
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
                new android.widget.ImageView(this);

        splashImage.setImageResource(
                getResources().getIdentifier(
                        "deeprowss_splash",
                        "drawable",
                        getPackageName()
                )
        );

        splashImage.setScaleType(
                android.widget.ImageView.ScaleType.CENTER_INSIDE
        );

        splashImage.setAdjustViewBounds(true);

        splashImage.setBackgroundColor(
                BG_COLOR
        );

        /*
         * Keep the complete square splash centered with margins so it
         * cannot become an oversized full-screen image.
         */
        int margin = dp(32);

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

        customSplashView = splashImage;

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

        View splash = customSplashView;

        customSplashView = null;

        splash.animate()
                .alpha(0f)
                .setDuration(180)
                .withEndAction(
                        () -> {
                            if (rootLayout != null) {
                                rootLayout.removeView(splash);
                            }
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
                                request.getUrl().toString();


                        if (showingOfflinePage) {

                            showingOfflinePage =
                                    false;

                            view.loadUrl(url);

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

                            view.loadUrl(url);

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


                        /*
                         * Never show white while the website
                         * or a new page is loading.
                         */

                        view.setBackgroundColor(
                                BG_COLOR
                        );
                    }


                    /*
                     * =================================================
                     * SPLASH RELEASE
                     * =================================================
                     *
                     * onPageCommitVisible() is used instead of
                     * onPageFinished() because it indicates that the
                     * page has committed content that is about to be
                     * drawn by the WebView.
                     *
                     * This removes the splash based on actual WebView
                     * visibility rather than an arbitrary timer.
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
                                        "https://deeprowss.com"
                                )) {

                            showingOfflinePage =
                                    false;
                        }
                    }


                    /*
                     * Only show offline page if the MAIN
                     * document fails.
                     *
                     * Also release the splash here so the app
                     * does not remain stuck on the splash screen
                     * when there is no internet connection.
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


                        if (request != null &&
                                request.isForMainFrame()) {

                            webPageVisible =
                                    true;

                            hideCustomSplash();

                            showOfflinePage();
                        }
                    }


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
                                new Runnable() {

                                    @Override
                                    public void run() {

                                        if (refreshContainer != null) {

                                            refreshContainer
                                                    .stopRefreshing();
                                        }
                                    }
                                },
                                900
                        );
                    }
                }
        );


        /*
         * Add WebView.
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
         * Zoom controls OFF.
         */

        settings.setBuiltInZoomControls(
                false
        );

        settings.setDisplayZoomControls(
                false
        );


        /*
         * =====================================================
         * IMPORTANT WEB VIEW SCALING
         * =====================================================
         *
         * Do NOT force Android to resize the website.
         *
         * The HTML viewport and website CSS control the
         * responsive layout.
         */

        settings.setLoadWithOverviewMode(
                true
        );

        settings.setUseWideViewPort(
                true
        );


        /*
         * Keep website text at its real CSS size.
         */

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
         * =====================================================
         * IMPORTANT CACHE SETTING
         * =====================================================
         *
         * This prevents an old style.css or script.js from
         * remaining in the WebView cache after you update
         * the GitHub website.
         */

        settings.setCacheMode(
                WebSettings.LOAD_NO_CACHE
        );


        /*
         * =====================================================
         * MIXED CONTENT
         * =====================================================
         */

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.LOLLIPOP) {

            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            );
        }


        /*
         * =====================================================
         * DOWNLOAD SUPPORT
         * =====================================================
         */

        /*
         * Make every WebView a normal interactive browser surface.
         */
        webView.setClickable(true);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.setEnabled(true);

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
         * Cookies.
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
     * WEB DOWNLOAD
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

                request.setMimeType(mimeType);
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

                manager.enqueue(request);
            }

        } catch (Exception ignored) {

            // Do not crash the app if a website uses an unsupported download scheme.
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
         * =========================================================
         * EXTERNAL LINK EXCEPTIONS
         * =========================================================
         *
         * These specific links are NOT opened inside the app's
         * popup WebView. Android handles them externally.
         *
         * Everything else keeps the existing behavior.
         */
        if (isExternalExceptionUrl(url)) {

            openExternalUrl(url);

            return;
        }


        /*
         * =========================================================
         * NORMAL DEEPROWSS NAVIGATION
         * =========================================================
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
         * All other links keep using the existing
         * in-app popup WebView.
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


    private boolean isHttpUrl(
            String url
    ) {

        if (url == null) {
            return false;
        }

        String value =
                url.trim().toLowerCase(
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

            startActivity(intent);

        } catch (Exception ignored) {

            /*
             * If no external application can handle the URL,
             * keep the link inside the existing popup WebView
             * rather than crashing the app.
             */
            openPopup(url);
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
     * OPEN POPUP / BROWSER WINDOW
     * =========================================================
     *
     * This method is intentionally kept as the single entry point
     * for opening a normal URL inside the app browser.
     *
     * It creates a NEW WebView window and loads the URL into it.
     * The existing browser windows are NOT destroyed.
     */
    private void openPopup(String url) {

        if (url == null || url.trim().isEmpty()) {
            return;
        }

        String targetUrl = url.trim();

        /*
         * Do not try to render Android-only schemes as webpages.
         * Let Android handle them instead.
         */
        if (!isHttpUrl(targetUrl)) {
            openExternalUrl(targetUrl);
            return;
        }

        WebView browserWindow = createPopupWebView();

        /*
         * IMPORTANT:
         * createPopupWebView() attaches the window to the browser
         * container and makes it the active/focused window.
         */
        browserWindow.loadUrl(targetUrl);
    }


    /*
     * =========================================================
     * POPUP WEBVIEW
     * =========================================================
     */

    private WebView createPopupWebView() {

        /*
         * IMPORTANT:
         *
         * Every window gets its OWN WebView.
         * We never destroy the current WebView just because a
         * website asks for another window.
         *
         * This is much closer to Chrome's window behavior.
         */
        final WebView newWebView =
                new WebView(this);

        newWebView.setBackgroundColor(Color.BLACK);

        /*
         * Make the WebView a real interactive/focusable child.
         * Some popup-heavy websites depend on focus for touch,
         * JavaScript controls, forms and scrolling.
         */
        newWebView.setClickable(true);
        newWebView.setFocusable(true);
        newWebView.setFocusableInTouchMode(true);
        newWebView.setEnabled(true);
        newWebView.setHapticFeedbackEnabled(true);

        configureWebView(newWebView);

        /*
         * Give external pages a normal modern Chrome-like user agent.
         * This prevents sites from serving a crippled WebView version
         * of their page when their normal mobile page works correctly.
         */
        try {
            String defaultUa =
                    newWebView.getSettings().getUserAgentString();

            if (defaultUa != null &&
                    !defaultUa.toLowerCase(
                            java.util.Locale.US
                    ).contains("chrome/")) {

                String chromeUa =
                        defaultUa +
                        " Chrome/131.0.0.0 Mobile Safari/537.36";

                newWebView.getSettings().setUserAgentString(
                        chromeUa
                );
            }
        } catch (Exception ignored) {
        }

        /*
         * Keep normal browser navigation INSIDE this WebView.
         *
         * Do not intercept ordinary HTTPS links here.
         * Returning false is important: WebView itself performs
         * the navigation and preserves the website's JavaScript,
         * forms, redirects, history and touch behavior.
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
                                request.getUrl().toString();

                        /*
                         * Only non-web/application schemes are sent
                         * to Android. Normal HTTP/HTTPS stays inside
                         * the browser window.
                         */
                        if (!isHttpUrl(url)) {

                            openExternalUrl(url);

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

                            openExternalUrl(url);

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

                        view.setBackgroundColor(Color.BLACK);

                        /*
                         * Make sure the active WebView remains above
                         * every other popup/browser layer.
                         */
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

                        view.setBackgroundColor(Color.BLACK);

                        /*
                         * Restore focus after page navigation.
                         * This is especially useful on pages that
                         * replace their DOM after loading.
                         */
                        view.setFocusable(true);
                        view.setFocusableInTouchMode(true);
                        view.requestFocus(
                                View.FOCUS_DOWN
                        );
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
         * Each window gets its own Chrome client.
         *
         * window.open()/target=_blank therefore creates another
         * WebView without destroying the existing page.
         */
        newWebView.setWebChromeClient(
                createChromeClient()
        );

        /*
         * Download buttons work through Android DownloadManager.
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
         * Add this WebView to the browser window stack.
         */
        popupWebViewStack.add(newWebView);
        popupWebView = newWebView;

        showPopupContainer();

        /*
         * The container is already on screen at this point.
         * Add the new window above the previous window.
         */
        attachPopupWebView(newWebView);

        newWebView.setVisibility(View.VISIBLE);
        newWebView.bringToFront();

        /*
         * Explicitly request touch focus.
         */
        newWebView.post(
                () -> {
                    newWebView.setFocusable(true);
                    newWebView.setFocusableInTouchMode(true);
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
     * ATTACH POPUP/BROWSER WINDOW
     * =========================================================
     */

    private void attachPopupWebView(
            WebView webView
    ) {

        if (popupContainer == null ||
                webView == null) {

            return;
        }

        /*
         * Remove the WebView from any previous parent first.
         */
        if (webView.getParent() instanceof android.view.ViewGroup) {

            ((android.view.ViewGroup) webView.getParent())
                    .removeView(webView);
        }

        /*
         * Hide previous browser windows.
         * They remain alive in the stack so their history/session
         * is preserved, just like switching browser windows.
         */
        for (WebView existing : popupWebViewStack) {

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
                popupContainer.removeView(current);
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
         * BACK BUTTON
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
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        if (popupWebView != null &&
                                popupWebView.canGoBack()) {

                            popupWebView.goBack();

                        } else {

                            switchToPreviousPopupWindow();
                        }
                    }
                }
        );


        /*
         * TITLE
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
         * CLOSE
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
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        closePopup();
                    }
                }
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


        FrameLayout.LayoutParams webParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );


        webParams.topMargin =
                popupBarHeight;



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
         * Destroy every browser window, not just the active one.
         */
        for (WebView browserWindow :
                new ArrayList<>(popupWebViewStack)) {

            try {

                if (popupContainer != null) {
                    popupContainer.removeView(browserWindow);
                }

                browserWindow.stopLoading();
                browserWindow.onPause();
                browserWindow.destroy();

            } catch (Exception ignored) {
            }
        }

        popupWebViewStack.clear();

        popupWebView = null;


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
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
    }


    /*
     * =========================================================
     * BACK BUTTON
     * =========================================================
     */

    @Override
    public void onBackPressed() {

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
                new ArrayList<>(popupWebViewStack)) {

            try {
                browserWindow.stopLoading();
                browserWindow.destroy();
            } catch (Exception ignored) {
            }
        }

        popupWebViewStack.clear();
        popupWebView = null;


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
                    new ProgressBar(context);


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
                                dp(getContext(), 70)
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
