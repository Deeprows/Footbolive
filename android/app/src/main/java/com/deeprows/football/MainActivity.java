package com.deeprows.football;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
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

    private static final String OTHER_EXTERNAL_URL =
            "https://REPLACE-WITH-YOUR-SECOND-LINK.com";

    private static final int POPUP_BAR_HEIGHT_DP = 58;

    private static final int BG_COLOR =
            Color.rgb(7, 9, 13);

    private FrameLayout rootLayout;

    private RefreshableWebViewContainer refreshContainer;

    private WebView mainWebView;

    private WebView popupWebView;

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

        if (url == null) {

            return false;
        }


        String normalizedUrl =
                url.trim().toLowerCase(
                        java.util.Locale.US
                );


        /*
         * Telegram.
         *
         * Handles:
         * https://t.me/deeprows
         * https://telegram.me/deeprows
         * Telegram links with additional path/query data.
         */
        if (normalizedUrl.startsWith(
                "https://t.me/deeprows"
        ) ||
                normalizedUrl.startsWith(
                        "http://t.me/deeprows"
                ) ||
                normalizedUrl.startsWith(
                        "https://telegram.me/deeprows"
                ) ||
                normalizedUrl.startsWith(
                        "http://telegram.me/deeprows"
                )) {

            return true;
        }


        /*
         * Second external-link exception.
         *
         * Replace OTHER_EXTERNAL_URL above with your real URL.
         */
        String otherUrl =
                OTHER_EXTERNAL_URL
                        .trim()
                        .toLowerCase(
                                java.util.Locale.US
                        );


        if (!otherUrl.contains(
                "REPLACE-WITH-YOUR-SECOND-LINK"
        ) &&
                !otherUrl.isEmpty() &&
                normalizedUrl.startsWith(
                        otherUrl
                )) {

            return true;
        }


        return false;
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
     * POPUP WEBVIEW
     * =========================================================
     */

    private WebView createPopupWebView() {

        if (popupWebView != null) {

            try {

                popupWebView.stopLoading();

                popupWebView.destroy();

            } catch (Exception ignored) {
            }

            popupWebView =
                    null;
        }


        popupWebView =
                new WebView(this);


        popupWebView.setBackgroundColor(
                Color.BLACK
        );


        configureWebView(
                popupWebView
        );


        popupWebView.setWebViewClient(
                new WebViewClient()
        );


        popupWebView.setWebChromeClient(
                createChromeClient()
        );


        showPopupContainer();


        return popupWebView;
    }


    private void openPopup(
            String url
    ) {

        if (url == null ||
                url.trim().isEmpty()) {

            return;
        }


        WebView popup =
                createPopupWebView();


        popup.loadUrl(
                url
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

                            closePopup();
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


        if (popupWebView != null) {

            popupContainer.addView(
                    popupWebView,
                    webParams
            );
        }


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


        if (popupWebView != null) {

            try {

                popupWebView.stopLoading();

                popupWebView.onPause();

                popupWebView.destroy();

            } catch (Exception ignored) {
            }

            popupWebView =
                    null;
        }


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

                closePopup();
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


        if (popupWebView != null) {

            try {

                popupWebView.stopLoading();

                popupWebView.destroy();

            } catch (Exception ignored) {
            }

            popupWebView =
                    null;
        }


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
