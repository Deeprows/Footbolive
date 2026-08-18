package com.deeprows.football;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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

public class MainActivity extends Activity {

    private WebView mainWebView;
    private WebView popupWebView;

    private FrameLayout rootLayout;
    private FrameLayout popupContainer;

    private RefreshableWebViewContainer refreshContainer;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private final String WEBSITE_URL =
            "https://deeprows.github.io/Footbolive/";

    private static final int POPUP_BAR_HEIGHT = 56;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Hide status bar.
         * Navigation bar remains visible.
         */
        keepFullScreen();

        /*
         * Hardware acceleration.
         */
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        /*
         * Root layout.
         */
        rootLayout = new FrameLayout(this);

        rootLayout.setBackgroundColor(
                Color.rgb(7, 9, 13)
        );

        setContentView(rootLayout);

        /*
         * Create main website.
         */
        createMainWebView();

        /*
         * Load website.
         */
        mainWebView.loadUrl(WEBSITE_URL);
    }

    private void createMainWebView() {

        mainWebView = new WebView(this);

        WebSettings settings =
                mainWebView.getSettings();

        /*
         * JavaScript.
         */
        settings.setJavaScriptEnabled(true);

        /*
         * Storage.
         */
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        /*
         * Media.
         */
        settings.setMediaPlaybackRequiresUserGesture(false);

        /*
         * Popup support.
         */
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        /*
         * Content access.
         */
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        /*
         * Zoom.
         */
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        /*
         * Cookies.
         */
        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                mainWebView,
                true
        );

        /*
         * Website navigation.
         */
        mainWebView.setWebViewClient(
                new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            WebResourceRequest request) {

                        String url =
                                request.getUrl().toString();

                        handleMainNavigation(url);

                        return true;
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(
                            WebView view,
                            String url) {

                        handleMainNavigation(url);

                        return true;
                    }
                }
        );

        /*
         * Chrome client.
         */
        mainWebView.setWebChromeClient(
                new WebChromeClient() {

                    /*
                     * Handle target="_blank"
                     * and window.open().
                     */
                    @Override
                    public boolean onCreateWindow(
                            WebView view,
                            boolean isDialog,
                            boolean isUserGesture,
                            android.os.Message resultMsg) {

                        WebView popup =
                                createPopupWebView();

                        WebView.WebViewTransport transport =
                                (WebView.WebViewTransport)
                                        resultMsg.obj;

                        transport.setWebView(popup);

                        resultMsg.sendToTarget();

                        return true;
                    }

                    /*
                     * Video fullscreen.
                     */
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

                        /*
                         * Put video above everything.
                         */
                        rootLayout.addView(
                                customView,
                                new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                )
                        );

                        /*
                         * Hide website.
                         */
                        if (refreshContainer != null) {

                            refreshContainer.setVisibility(
                                    View.GONE
                            );
                        }

                        /*
                         * Landscape.
                         */
                        setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        );

                        /*
                         * Status bar hidden.
                         * Navigation bar visible.
                         */
                        keepFullScreen();
                    }

                    /*
                     * Exit video fullscreen.
                     */
                    @Override
                    public void onHideCustomView() {

                        exitVideoFullscreen();
                    }
                }
        );

        /*
         * Create native pull-to-refresh container.
         */
        refreshContainer =
                new RefreshableWebViewContainer(this);

        refreshContainer.setWebView(
                mainWebView
        );

        refreshContainer.setOnRefreshListener(
                () -> {

                    if (mainWebView != null) {

                        mainWebView.reload();

                        /*
                         * Stop refresh indicator
                         * after reload starts.
                         */
                        new Handler(
                                Looper.getMainLooper()
                        ).postDelayed(
                                () -> {

                                    if (refreshContainer != null) {

                                        refreshContainer
                                                .stopRefreshing();
                                    }

                                },
                                800
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
         * Add refresh container to root.
         */
        rootLayout.addView(
                refreshContainer,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );
    }

    private void handleMainNavigation(String url) {

        /*
         * Deeprowss website pages remain
         * inside the main WebView.
         */
        if (url.startsWith(
                "https://deeprows.github.io/"
        )) {

            mainWebView.loadUrl(url);

            return;
        }

        /*
         * External/on-click links open
         * inside our popup.
         */
        openPopup(url);
    }

    private WebView createPopupWebView() {

        popupWebView =
                new WebView(this);

        WebSettings settings =
                popupWebView.getSettings();

        settings.setJavaScriptEnabled(true);

        settings.setDomStorageEnabled(true);

        settings.setDatabaseEnabled(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(
                true
        );

        settings.setSupportMultipleWindows(
                true
        );

        settings.setMediaPlaybackRequiresUserGesture(
                false
        );

        settings.setAllowFileAccess(true);

        settings.setAllowContentAccess(true);

        CookieManager.getInstance()
                .setAcceptThirdPartyCookies(
                        popupWebView,
                        true
                );

        /*
         * Keep popup navigation inside
         * the popup WebView.
         */
        popupWebView.setWebViewClient(
                new WebViewClient()
        );

        popupWebView.setWebChromeClient(
                new WebChromeClient()
        );

        showPopupContainer();

        return popupWebView;
    }

    private void openPopup(String url) {

        WebView popup =
                createPopupWebView();

        popup.loadUrl(url);
    }

    private void showPopupContainer() {

        /*
         * Prevent duplicate popup.
         */
        if (popupContainer != null) {
            return;
        }

        /*
         * Popup root.
         */
        popupContainer =
                new FrameLayout(this);

        popupContainer.setBackgroundColor(
                Color.BLACK
        );

        /*
         * Fixed top bar.
         */
        LinearLayout topBar =
                new LinearLayout(this);

        topBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topBar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        topBar.setPadding(
                4,
                0,
                4,
                0
        );

        topBar.setBackgroundColor(
                Color.rgb(15, 18, 24)
        );

        /*
         * Back button.
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

                        closePopup();
                    }
                }
        );

        /*
         * Title.
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

        title.setPadding(
                8,
                0,
                8,
                0
        );

        /*
         * X button.
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
         * Add back button.
         */
        topBar.addView(
                backButton,
                new LinearLayout.LayoutParams(
                        POPUP_BAR_HEIGHT,
                        POPUP_BAR_HEIGHT
                )
        );

        /*
         * Add title.
         */
        topBar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        POPUP_BAR_HEIGHT,
                        1
                )
        );

        /*
         * Add X.
         */
        topBar.addView(
                closeButton,
                new LinearLayout.LayoutParams(
                        POPUP_BAR_HEIGHT,
                        POPUP_BAR_HEIGHT
                )
        );

        /*
         * Add top bar.
         */
        FrameLayout.LayoutParams topBarParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        POPUP_BAR_HEIGHT,
                        Gravity.TOP
                );

        popupContainer.addView(
                topBar,
                topBarParams
        );

        /*
         * Popup WebView.
         *
         * IMPORTANT:
         * The WebView starts BELOW the top bar.
         * Therefore the X cannot be covered.
         */
        FrameLayout.LayoutParams webParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        webParams.topMargin =
                POPUP_BAR_HEIGHT;

        popupContainer.addView(
                popupWebView,
                webParams
        );

        /*
         * Put popup above main website.
         */
        rootLayout.addView(
                popupContainer,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        /*
         * Hide website while popup is open.
         */
        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.INVISIBLE
            );
        }
    }

    private void closePopup() {

        if (popupContainer == null) {
            return;
        }

        /*
         * Stop popup.
         */
        if (popupWebView != null) {

            popupWebView.stopLoading();

            popupWebView.destroy();

            popupWebView = null;
        }

        /*
         * Remove popup.
         */
        rootLayout.removeView(
                popupContainer
        );

        popupContainer = null;

        /*
         * Restore website.
         */
        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.VISIBLE
            );
        }

        keepFullScreen();
    }

    private void exitVideoFullscreen() {

        if (customView == null) {
            return;
        }

        /*
         * Remove fullscreen video.
         */
        rootLayout.removeView(
                customView
        );

        customView = null;

        /*
         * Notify video player.
         */
        if (customViewCallback != null) {

            customViewCallback
                    .onCustomViewHidden();

            customViewCallback = null;
        }

        /*
         * Restore website.
         */
        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.VISIBLE
            );
        }

        /*
         * Return portrait.
         */
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );

        /*
         * Status bar hidden.
         * Navigation bar visible.
         */
        keepFullScreen();
    }

    /*
     * Hide status bar only.
     *
     * Navigation bar is intentionally NOT hidden.
     */
    private void keepFullScreen() {

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        /*
         * Do NOT use:
         *
         * SYSTEM_UI_FLAG_HIDE_NAVIGATION
         * SYSTEM_UI_FLAG_IMMERSIVE_STICKY
         *
         * Navigation bar therefore remains visible.
         */
        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
    }

    @Override
    public void onBackPressed() {

        /*
         * Video fullscreen.
         */
        if (customView != null) {

            exitVideoFullscreen();

            return;
        }

        /*
         * Popup.
         */
        if (popupContainer != null) {

            if (popupWebView != null &&
                    popupWebView.canGoBack()) {

                popupWebView.goBack();

            } else {

                closePopup();
            }

            return;
        }

        /*
         * Main website.
         */
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

        keepFullScreen();
    }

    @Override
    protected void onDestroy() {

        if (popupWebView != null) {

            popupWebView.destroy();

            popupWebView = null;
        }

        if (mainWebView != null) {

            mainWebView.destroy();

            mainWebView = null;
        }

        super.onDestroy();
    }


    /*
     * =========================================================
     * PULL-DOWN-TO-REFRESH
     * =========================================================
     *
     * Native implementation.
     * No AndroidX dependency.
     */
    private static class RefreshableWebViewContainer
            extends FrameLayout {

        private WebView webView;

        private float startY;

        private boolean dragging = false;

        private boolean refreshing = false;

        private static final int TRIGGER_DISTANCE = 180;

        private static final int MAX_PULL_DISTANCE = 300;

        private ProgressBar progressBar;

        private OnRefreshListener listener;

        interface OnRefreshListener {
            void onRefresh();
        }

        public RefreshableWebViewContainer(
                Context context) {

            super(context);

            setClipChildren(false);

            /*
             * Refresh indicator.
             */
            progressBar =
                    new ProgressBar(context);

            progressBar.setVisibility(
                    View.GONE
            );

            LayoutParams progressParams =
                    new LayoutParams(
                            70,
                            70
                    );

            progressParams.gravity =
                    Gravity.TOP |
                    Gravity.CENTER_HORIZONTAL;

            progressParams.topMargin = 20;

            addView(
                    progressBar,
                    progressParams
            );
        }

        public void setWebView(
                WebView webView) {

            this.webView = webView;
        }

        public void setOnRefreshListener(
                OnRefreshListener listener) {

            this.listener = listener;
        }

        public void stopRefreshing() {

            refreshing = false;

            dragging = false;

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
                MotionEvent event) {

            if (webView == null ||
                    refreshing) {

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

                    /*
                     * Only activate when the
                     * WebView is already at top.
                     */
                    if (distance > 0 &&
                            webView.getScrollY() <= 0) {

                        if (distance > 15) {

                            dragging = true;

                            return true;
                        }
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
        public boolean onTouchEvent(
                MotionEvent event) {

            if (webView == null ||
                    refreshing) {

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

                    if (distance < 0) {
                        distance = 0;
                    }

                    if (distance >
                            MAX_PULL_DISTANCE) {

                        distance =
                                MAX_PULL_DISTANCE;
                    }

                    if (distance > 0) {

                        /*
                         * Resistance.
                         */
                        float offset =
                                distance * 0.55f;

                        webView.setTranslationY(
                                offset
                        );

                        /*
                         * Show indicator.
                         */
                        if (offset >=
                                TRIGGER_DISTANCE * 0.55f) {

                            progressBar.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }

                    return true;

                case MotionEvent.ACTION_UP:

                    float finalDistance =
                            event.getY() - startY;

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

            refreshing = true;

            if (progressBar != null) {

                progressBar.setVisibility(
                        View.VISIBLE
                );
            }

            if (webView != null) {

                webView.animate()
                        .translationY(70)
                        .setDuration(150)
                        .start();
            }

            if (listener != null) {

                listener.onRefresh();
            }
        }
    }
}
