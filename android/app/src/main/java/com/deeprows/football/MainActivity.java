package com.deeprows.football;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
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

import java.util.Locale;

public class MainActivity extends Activity {

    private static final String WEBSITE_URL = "https://deeprowss.com";
    private static final int POPUP_BAR_HEIGHT_DP = 58;
    private static final int BG_COLOR = Color.rgb(7, 9, 13);

    private FrameLayout rootLayout;
    private RefreshableWebViewContainer refreshContainer;
    private WebView mainWebView;

    private WebView popupWebView;
    private FrameLayout popupContainer;

    private View customVideoView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private int popupBarHeight;
    private boolean showingOfflinePage = false;
    private boolean webPageVisible = false;
    private View customSplashView;

    private ValueCallback<Uri[]> filePathCallback;
    private static final int FILE_CHOOSER_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen =
                SplashScreen.installSplashScreen(this);

        splashScreen.setKeepOnScreenCondition(
                () -> !webPageVisible
        );

        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawable(
                new android.graphics.drawable.ColorDrawable(BG_COLOR)
        );
        getWindow().setNavigationBarColor(BG_COLOR);
        getWindow().setStatusBarColor(BG_COLOR);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        hideStatusBar();
        popupBarHeight = dp(POPUP_BAR_HEIGHT_DP);

        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(BG_COLOR);
        setContentView(rootLayout);

        showCustomSplash();
        createMainWebView();

        mainWebView.loadUrl(WEBSITE_URL);
    }

    private void showCustomSplash() {
        if (rootLayout == null || customSplashView != null) return;

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
        splashImage.setBackgroundColor(BG_COLOR);

        int margin = dp(32);

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        params.setMargins(margin, margin, margin, margin);

        customSplashView = splashImage;
        rootLayout.addView(customSplashView, params);
        customSplashView.bringToFront();
    }

    private void hideCustomSplash() {
        if (customSplashView == null) return;

        View splash = customSplashView;
        customSplashView = null;

        splash.animate()
                .alpha(0f)
                .setDuration(180)
                .withEndAction(() -> {
                    if (rootLayout != null) {
                        rootLayout.removeView(splash);
                    }
                })
                .start();
    }

    private void createMainWebView() {

        mainWebView = new WebView(this);
        mainWebView.setBackgroundColor(BG_COLOR);

        configureWebView(mainWebView);

        /*
         * IMPORTANT:
         * Normal HTTP/HTTPS navigation stays inside the SAME WebView.
         * This makes the app behave much more like Chrome.
         */
        mainWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                if (request == null || request.getUrl() == null) {
                    return false;
                }

                String url = request.getUrl().toString();

                if (showingOfflinePage) {
                    showingOfflinePage = false;
                    view.loadUrl(url);
                    return true;
                }

                if (isTelegramUrl(url)) {
                    openTelegram(url);
                    return true;
                }

                if (isHttpUrl(url)) {
                    return false;
                }

                openSpecialUrl(url);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {
                if (url == null || url.trim().isEmpty()) {
                    return false;
                }

                if (showingOfflinePage) {
                    showingOfflinePage = false;
                    view.loadUrl(url);
                    return true;
                }

                if (isTelegramUrl(url)) {
                    openTelegram(url);
                    return true;
                }

                if (isHttpUrl(url)) {
                    return false;
                }

                openSpecialUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(
                    WebView view,
                    String url,
                    android.graphics.Bitmap favicon
            ) {
                super.onPageStarted(view, url, favicon);
                view.setBackgroundColor(BG_COLOR);
            }

            @Override
            public void onPageCommitVisible(
                    WebView view,
                    String url
            ) {
                super.onPageCommitVisible(view, url);

                if (!webPageVisible) {
                    webPageVisible = true;
                    hideCustomSplash();
                }
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                if (url != null && url.startsWith(WEBSITE_URL)) {
                    showingOfflinePage = false;
                }
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
            ) {
                super.onReceivedError(view, request, error);

                if (request != null && request.isForMainFrame()) {
                    webPageVisible = true;
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
                    webPageVisible = true;
                    hideCustomSplash();
                    showOfflinePage();
                }
            }
        });

        mainWebView.setWebChromeClient(createChromeClient());

        refreshContainer =
                new RefreshableWebViewContainer(this);

        refreshContainer.setBackgroundColor(BG_COLOR);
        refreshContainer.setWebView(mainWebView);

        refreshContainer.setOnRefreshListener(() -> {

            if (mainWebView == null) return;

            if (showingOfflinePage) {
                showWebsiteAgain();
            } else {
                mainWebView.reload();
            }

            new Handler(Looper.getMainLooper()).postDelayed(
                    () -> {
                        if (refreshContainer != null) {
                            refreshContainer.stopRefreshing();
                        }
                    },
                    900
            );
        });

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

    private void configureWebView(WebView webView) {

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(true);

        settings.setTextZoom(100);
        settings.setDefaultTextEncodingName("UTF-8");

        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);

        /*
         * Use normal browser caching instead of LOAD_NO_CACHE.
         */
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.LOLLIPOP) {

            settings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            );
        }

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setDownloadListener(
                (url, userAgent, contentDisposition, mimeType, contentLength) ->
                        handleWebDownload(
                                url,
                                userAgent,
                                contentDisposition,
                                mimeType
                        )
        );
    }

    private void handleWebDownload(
            String url,
            String userAgent,
            String contentDisposition,
            String mimeType
    ) {

        if (url == null || url.trim().isEmpty()) return;

        if (isTelegramUrl(url)) {
            openTelegram(url);
            return;
        }

        try {
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();

            if (scheme == null ||
                    (!scheme.equalsIgnoreCase("http") &&
                     !scheme.equalsIgnoreCase("https"))) {

                openSpecialUrl(url);
                return;
            }

            DownloadManager.Request request =
                    new DownloadManager.Request(uri);

            if (mimeType != null && !mimeType.trim().isEmpty()) {
                request.setMimeType(mimeType);
            }

            if (userAgent != null && !userAgent.trim().isEmpty()) {
                request.addRequestHeader("User-Agent", userAgent);
            }

            String cookie =
                    CookieManager.getInstance().getCookie(url);

            if (cookie != null && !cookie.trim().isEmpty()) {
                request.addRequestHeader("Cookie", cookie);
            }

            request.setDescription("Downloading from Deeprowss");

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

            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "deeprowss_download";
            }

            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName
            );

            DownloadManager manager =
                    (DownloadManager) getSystemService(
                            DOWNLOAD_SERVICE
                    );

            if (manager != null) {
                manager.enqueue(request);
            }

        } catch (Exception ignored) {
        }
    }

    private boolean isHttpUrl(String url) {
        if (url == null) return false;

        String lower = url.toLowerCase(Locale.US);

        return lower.startsWith("http://") ||
                lower.startsWith("https://");
    }

    private boolean isTelegramUrl(String url) {

        if (url == null || url.trim().isEmpty()) return false;

        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();

            if (host == null) return false;

            host = host.toLowerCase(Locale.US);

            return host.equals("t.me") ||
                    host.equals("telegram.me") ||
                    host.equals("www.telegram.me");

        } catch (Exception ignored) {
            return false;
        }
    }

    private void openTelegram(String url) {

        try {
            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(intent);

        } catch (Exception ignored) {

            /*
             * If Telegram isn't installed, Android may still have a
             * browser capable of opening the t.me URL.
             */
            try {
                Intent browserIntent =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url)
                        );

                startActivity(browserIntent);

            } catch (Exception ignoredAgain) {
            }
        }
    }

    private void openSpecialUrl(String url) {

        try {
            Intent intent =
                    new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(url)
                    );

            startActivity(intent);

        } catch (Exception ignored) {
        }
    }

    private void showOfflinePage() {

        if (mainWebView == null || showingOfflinePage) return;

        showingOfflinePage = true;

        String offlineHtml =
                "<!DOCTYPE html>" +
                "<html><head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width," +
                "initial-scale=1.0,maximum-scale=1.0,user-scalable=no'>" +
                "<style>" +
                "html,body{margin:0;padding:0;width:100%;height:100%;" +
                "background:#07090d;color:#fff;font-family:Arial,sans-serif;" +
                "overflow:hidden;}" +
                "body{display:flex;align-items:center;justify-content:center;" +
                "text-align:center;}" +
                ".box{width:88%;max-width:420px;padding:30px 20px;" +
                "box-sizing:border-box;}" +
                ".logo{width:72px;height:72px;margin:0 auto 22px;" +
                "border-radius:20px;background:#ff1744;display:flex;" +
                "align-items:center;justify-content:center;font-size:32px;" +
                "font-weight:800;color:#fff;}" +
                "h1{font-size:25px;font-weight:700;margin:0 0 12px;}" +
                "p{font-size:15px;line-height:1.6;color:#9299a8;" +
                "margin:0 0 28px;}" +
                "button{border:0;outline:none;border-radius:12px;" +
                "background:#ff1744;color:#fff;font-size:15px;" +
                "font-weight:700;padding:14px 30px;min-width:150px;}" +
                "</style></head><body>" +
                "<div class='box'>" +
                "<div class='logo'>D</div>" +
                "<h1>You're offline</h1>" +
                "<p>We couldn't connect to Deeprowss right now." +
                "<br>Please check your internet connection and try again.</p>" +
                "<button onclick='location.href=\"" +
                WEBSITE_URL +
                "\"'>TRY AGAIN</button>" +
                "</div></body></html>";

        mainWebView.setBackgroundColor(BG_COLOR);

        mainWebView.loadDataWithBaseURL(
                WEBSITE_URL,
                offlineHtml,
                "text/html",
                "UTF-8",
                null
        );
    }

    private void showWebsiteAgain() {

        showingOfflinePage = false;

        if (mainWebView != null) {
            mainWebView.setBackgroundColor(BG_COLOR);
            mainWebView.loadUrl(WEBSITE_URL);
        }
    }

    private WebChromeClient createChromeClient() {

        return new WebChromeClient() {

            @Override
            public boolean onCreateWindow(
                    WebView view,
                    boolean isDialog,
                    boolean isUserGesture,
                    android.os.Message resultMsg
            ) {

                /*
                 * target=_blank and window.open() are handled in the
                 * SAME WebView. This prevents the old popup-WebView
                 * behavior that caused secondary pages to stop behaving
                 * correctly.
                 */
                WebView.WebViewTransport transport =
                        (WebView.WebViewTransport) resultMsg.obj;

                transport.setWebView(view);
                resultMsg.sendToTarget();

                return true;
            }

            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> callback,
                    FileChooserParams params
            ) {

                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }

                filePathCallback = callback;

                try {
                    Intent intent = params.createIntent();

                    startActivityForResult(
                            intent,
                            FILE_CHOOSER_REQUEST
                    );

                    return true;

                } catch (Exception ignored) {
                    filePathCallback = null;
                    return false;
                }
            }

            @Override
            public void onShowCustomView(
                    View view,
                    CustomViewCallback callback
            ) {
                showVideoFullscreen(view, callback);
            }

            @Override
            public void onHideCustomView() {
                exitVideoFullscreen();
            }
        };
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        if (requestCode == FILE_CHOOSER_REQUEST) {

            if (filePathCallback != null) {

                Uri[] results =
                        WebChromeClient.FileChooserParams
                                .parseResult(
                                        resultCode,
                                        data
                                );

                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }

            return;
        }

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );
    }

    private void showVideoFullscreen(
            View view,
            WebChromeClient.CustomViewCallback callback
    ) {

        if (customVideoView != null) {
            callback.onCustomViewHidden();
            return;
        }

        customVideoView = view;
        customViewCallback = callback;

        if (refreshContainer != null) {
            refreshContainer.setVisibility(View.GONE);
        }

        if (popupContainer != null) {
            popupContainer.setVisibility(View.GONE);
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

        if (customVideoView == null) return;

        rootLayout.removeView(customVideoView);
        customVideoView = null;

        if (customViewCallback != null) {
            customViewCallback.onCustomViewHidden();
            customViewCallback = null;
        }

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );

        if (popupContainer != null) {
            popupContainer.setVisibility(View.VISIBLE);
        } else if (refreshContainer != null) {
            refreshContainer.setVisibility(View.VISIBLE);
        }

        hideStatusBar();
    }

    /*
     * Legacy popup methods retained for compatibility.
     * Normal browsing no longer routes pages through this popup.
     */
    private WebView createPopupWebView() {

        if (popupWebView != null) {

            try {
                popupWebView.stopLoading();
                popupWebView.destroy();
            } catch (Exception ignored) {
            }

            popupWebView = null;
        }

        popupWebView = new WebView(this);
        popupWebView.setBackgroundColor(Color.BLACK);
        configureWebView(popupWebView);
        popupWebView.setWebChromeClient(createChromeClient());

        popupWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {

                if (request == null || request.getUrl() == null) {
                    return false;
                }

                String url = request.getUrl().toString();

                if (isTelegramUrl(url)) {
                    openTelegram(url);
                    return true;
                }

                return isHttpUrl(url) ? false : openSpecialAndReturn(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {

                if (url == null || url.trim().isEmpty()) {
                    return false;
                }

                if (isTelegramUrl(url)) {
                    openTelegram(url);
                    return true;
                }

                return isHttpUrl(url) ? false : openSpecialAndReturn(url);
            }
        });

        showPopupContainer();

        return popupWebView;
    }

    private boolean openSpecialAndReturn(String url) {
        openSpecialUrl(url);
        return true;
    }

    private void openPopup(String url) {
        if (url == null || url.trim().isEmpty()) return;

        WebView popup = createPopupWebView();
        popup.loadUrl(url);
    }

    private void showPopupContainer() {

        if (popupContainer != null) return;

        popupContainer = new FrameLayout(this);
        popupContainer.setBackgroundColor(Color.BLACK);

        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(4), 0, dp(4), 0);
        topBar.setBackgroundColor(Color.rgb(15, 18, 24));

        ImageButton backButton = new ImageButton(this);
        backButton.setImageResource(android.R.drawable.ic_media_previous);
        backButton.setBackgroundColor(Color.TRANSPARENT);
        backButton.setColorFilter(Color.WHITE);
        backButton.setContentDescription("Back");

        backButton.setOnClickListener(v -> {
            if (popupWebView != null &&
                    popupWebView.canGoBack()) {
                popupWebView.goBack();
            } else {
                closePopup();
            }
        });

        TextView title = new TextView(this);
        title.setText("Deeprowss");
        title.setTextColor(Color.WHITE);
        title.setTextSize(15);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setSingleLine(true);
        title.setPadding(dp(8), 0, dp(8), 0);

        ImageButton closeButton = new ImageButton(this);
        closeButton.setImageResource(
                android.R.drawable.ic_menu_close_clear_cancel
        );
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setColorFilter(Color.WHITE);
        closeButton.setContentDescription("Close");
        closeButton.setOnClickListener(v -> closePopup());

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

        popupContainer.addView(topBar, barParams);

        FrameLayout.LayoutParams webParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        webParams.topMargin = popupBarHeight;

        if (popupWebView != null) {
            popupContainer.addView(popupWebView, webParams);
        }

        rootLayout.addView(
                popupContainer,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        if (refreshContainer != null) {
            refreshContainer.setVisibility(View.INVISIBLE);
        }

        hideStatusBar();
    }

    private void closePopup() {

        if (popupContainer == null) return;

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

            popupWebView = null;
        }

        rootLayout.removeView(popupContainer);
        popupContainer = null;

        if (refreshContainer != null) {
            refreshContainer.setVisibility(View.VISIBLE);
        }

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );

        hideStatusBar();
    }

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

        if (filePathCallback != null) {
            filePathCallback.onReceiveValue(null);
            filePathCallback = null;
        }

        if (customSplashView != null) {

            try {
                rootLayout.removeView(customSplashView);
            } catch (Exception ignored) {
            }

            customSplashView = null;
        }

        if (customVideoView != null) {

            try {
                rootLayout.removeView(customVideoView);
            } catch (Exception ignored) {
            }

            customVideoView = null;
        }

        if (popupWebView != null) {

            try {
                popupWebView.stopLoading();
                popupWebView.destroy();
            } catch (Exception ignored) {
            }

            popupWebView = null;
        }

        if (mainWebView != null) {

            try {
                mainWebView.stopLoading();
                mainWebView.destroy();
            } catch (Exception ignored) {
            }

            mainWebView = null;
        }

        super.onDestroy();
    }

    private int dp(int value) {

        float density =
                getResources()
                        .getDisplayMetrics()
                        .density;

        return (int) (value * density + 0.5f);
    }

    private static class RefreshableWebViewContainer
            extends FrameLayout {

        private WebView webView;
        private float startY;
        private boolean dragging;
        private boolean refreshing;
        private ProgressBar progressBar;
        private OnRefreshListener listener;

        private static final float TRIGGER_DISTANCE = 180f;
        private static final float MAX_PULL_DISTANCE = 300f;

        interface OnRefreshListener {
            void onRefresh();
        }

        RefreshableWebViewContainer(Context context) {

            super(context);

            setClipChildren(false);

            progressBar = new ProgressBar(context);
            progressBar.setVisibility(View.GONE);

            LayoutParams progressParams =
                    new LayoutParams(
                            dp(context, 42),
                            dp(context, 42)
                    );

            progressParams.gravity =
                    Gravity.TOP | Gravity.CENTER_HORIZONTAL;

            progressParams.topMargin =
                    dp(context, 16);

            addView(progressBar, progressParams);
        }

        void setWebView(WebView webView) {
            this.webView = webView;
        }

        void setOnRefreshListener(
                OnRefreshListener listener
        ) {
            this.listener = listener;
        }

        void stopRefreshing() {

            refreshing = false;
            dragging = false;

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
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

            if (webView == null || refreshing) {
                return false;
            }

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    dragging = false;
                    break;

                case MotionEvent.ACTION_MOVE:

                    float distance =
                            event.getY() - startY;

                    if (distance > 0 &&
                            webView.getScrollY() <= 0 &&
                            distance > 15) {

                        dragging = true;
                        return true;
                    }

                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    break;
            }

            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (webView == null || refreshing) {
                return true;
            }

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    startY = event.getY();
                    dragging = true;
                    return true;

                case MotionEvent.ACTION_MOVE:

                    float distance =
                            event.getY() - startY;

                    if (distance < 0) distance = 0;

                    if (distance > MAX_PULL_DISTANCE) {
                        distance = MAX_PULL_DISTANCE;
                    }

                    if (distance > 0) {

                        float offset = distance * 0.55f;

                        webView.setTranslationY(offset);

                        if (distance >=
                                TRIGGER_DISTANCE * 0.55f) {

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
                            event.getY() - startY;

                    if (finalDistance >= TRIGGER_DISTANCE) {
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

            if (refreshing) return;

            refreshing = true;

            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
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

            return (int) (value * density + 0.5f);
        }
    }
}
