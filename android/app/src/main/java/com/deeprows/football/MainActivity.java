package com.deeprows.football;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
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

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private RefreshableWebViewContainer refreshContainer;

    private final String WEBSITE_URL =
            "https://deeprows.github.io/Footbolive/";

    private static final int POPUP_BAR_HEIGHT = 56;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * FULL SCREEN
         *
         * Status bar hidden.
         * Navigation bar remains visible.
         */
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

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

        /*
         * Create WebView.
         */
        mainWebView = new WebView(this);

        WebSettings settings =
                mainWebView.getSettings();

        /*
         * JavaScript.
         */
        settings.setJavaScriptEnabled(true);

        /*
         * Website storage.
         */
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);

        /*
         * Media.
         */
        settings.setMediaPlaybackRequiresUserGesture(false);

        /*
         * New windows / target="_blank".
         */
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        /*
         * Content access.
         */
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        /*
         * Disable WebView zoom controls.
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
         * Main website navigation.
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
                     * Handle window.open()
                     * and target="_blank".
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
                         * Add fullscreen video.
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
                        refreshContainer.setVisibility(
                                View.GONE
                        );

                        /*
                         * Landscape.
                         */
                        setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        );

                        /*
                         * Status bar remains hidden.
                         * Navigation bar remains visible.
                         */
                        keepFullScreen();
                    }

                    /*
                     * Exit video fullscreen.
                     */
                    @Override
                    public void onHideCustomView() {

                        if (customView == null) {
                            return;
                        }

                        rootLayout.removeView(
                                customView
                        );

                        customView = null;

                        if (customViewCallback != null) {

                            customViewCallback
                                    .onCustomViewHidden();

                            customViewCallback = null;
                        }

                        /*
                         * Show website again.
                         */
                        refreshContainer.setVisibility(
                                View.VISIBLE
                        );

                        /*
                         * Portrait.
                         */
                        setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        );

                        /*
                         * Keep status bar hidden.
                         * Keep navigation bar visible.
                         */
                        keepFullScreen();
                    }
                }
        );

        /*
         * Native pull-to-refresh container.
         */
        refreshContainer =
                new RefreshableWebViewContainer(this);

        refreshContainer.setBackgroundColor(
                Color.rgb(7, 9, 13)
        );

        refreshContainer.setWebView(
                mainWebView
        );

        /*
         * Refresh callback.
         */
        refreshContainer.setOnRefreshListener(
                () -> {

                    if (mainWebView != null) {
                        mainWebView.reload();
                    }

                    /*
                     * Small delay so the refresh
                     * indicator doesn't disappear
                     * before the reload begins.
                     */
                    mainWebView.postDelayed(
                            () -> {

                                if (refreshContainer != null) {
                                    refreshContainer.stopRefreshing();
                                }

                            },
                            700
                    );
                }
        );

        /*
         * Add WebView to refresh container.
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
         * Keep Deeprowss pages inside
         * the main WebView.
         */
        if (url.startsWith(
                "https://deeprows.github.io/"
        )) {

            mainWebView.loadUrl(url);

            return;
        }

        /*
         * External/on-click navigation
         * opens inside the popup.
         */
        openPopup(url);
    }

    private WebView createPopupWebView() {

        popupWebView = new WebView(this);

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
         * popup WebView.
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
         * Only one popup at a time.
         */
        if (popupContainer != null) {
            return;
        }

        /*
         * Popup container.
         */
        popupContainer =
                new FrameLayout(this);

        popupContainer.setBackgroundColor(
                Color.BLACK
        );

        /*
         * Top control bar.
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
         * BACK BUTTON.
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
         * TITLE.
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
         * CLOSE BUTTON.
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
         * Back button.
         */
        topBar.addView(
                backButton,
                new LinearLayout.LayoutParams(
                        POPUP_BAR_HEIGHT,
                        POPUP_BAR_HEIGHT
                )
        );

        /*
         * Title.
         */
        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        POPUP_BAR_HEIGHT,
                        1
                );

        topBar.addView(
                title,
                titleParams
        );

        /*
         * X button.
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
         * It starts below the top bar.
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
         * Add popup over website.
         */
        rootLayout.addView(
                popupContainer,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        /*
         * Hide main website while popup
         * is active.
         */
        refreshContainer.setVisibility(
                View.INVISIBLE
        );
    }

    private void closePopup() {

        if (popupContainer == null) {
            return;
        }

        /*
         * Stop and destroy popup.
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
         * Restore main website.
         */
        refreshContainer.setVisibility(
                View.VISIBLE
        );

        /*
         * Keep status bar hidden.
         */
        keepFullScreen();
    }

    /*
     * Hide STATUS BAR.
     *
     * Navigation BAR remains visible.
     */
    private void keepFullScreen() {

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        /*
         * IMPORTANT:
         *
         * We intentionally DO NOT use:
         *
         * SYSTEM_UI_FLAG_HIDE_NAVIGATION
         * SYSTEM_UI_FLAG_IMMERSIVE_STICKY
         *
         * Therefore the navigation bar remains visible.
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

            if (customViewCallback != null) {

                customViewCallback
                        .onCustomViewHidden();
            }

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

        /*
         * Make sure status bar remains hidden.
         */
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
     * NATIVE PULL-DOWN-TO-REFRESH CONTAINER
     * =========================================================
     *
     * This avoids AndroidX completely.
     *
     * Pull down when the WebView is already at
     * the top of the page.
     */
    private static class RefreshableWebViewContainer
            extends FrameLayout {

        private WebView webView;

        private float startY;

        private boolean dragging = false;

        private boolean refreshing = false;

        private final int TRIGGER_DISTANCE = 180;

        private final int MAX_PULL_DISTANCE = 300;

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
                    new ProgressBar(
                            context
                    );

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

        public void
