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

    private static final String WEBSITE_URL =
            "https://deeprows.github.io/Footbolive/";

    private static final int POPUP_BAR_HEIGHT_DP = 58;

    private FrameLayout rootLayout;

    private RefreshableWebViewContainer refreshContainer;

    private WebView mainWebView;

    private WebView popupWebView;

    private FrameLayout popupContainer;

    private View customVideoView;

    private WebChromeClient.CustomViewCallback customViewCallback;

    private int popupBarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Status bar hidden.
         *
         * Navigation bar remains visible.
         */
        hideStatusBar();

        /*
         * Hardware acceleration.
         */
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        popupBarHeight = dp(POPUP_BAR_HEIGHT_DP);

        /*
         * Root.
         */
        rootLayout = new FrameLayout(this);

        rootLayout.setBackgroundColor(
                Color.rgb(7, 9, 13)
        );

        setContentView(rootLayout);

        /*
         * Main website.
         */
        createMainWebView();

        mainWebView.loadUrl(WEBSITE_URL);
    }

    /*
     * =========================================================
     * MAIN WEBVIEW
     * =========================================================
     */

    private void createMainWebView() {

        mainWebView = new WebView(this);

        configureWebView(mainWebView);

        /*
         * Main website navigation.
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

                        handleMainNavigation(
                                request.getUrl().toString()
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

                        handleMainNavigation(url);

                        return true;
                    }
                }
        );

        /*
         * Chrome client.
         */
        mainWebView.setWebChromeClient(
                createChromeClient()
        );

        /*
         * Pull-to-refresh.
         */
        refreshContainer =
                new RefreshableWebViewContainer(this);

        refreshContainer.setWebView(mainWebView);

        refreshContainer.setOnRefreshListener(
                new RefreshableWebViewContainer.OnRefreshListener() {

                    @Override
                    public void onRefresh() {

                        if (mainWebView != null) {

                            mainWebView.reload();

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
                }
        );

        /*
         * Add website.
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

    private void configureWebView(WebView webView) {

        WebSettings settings =
                webView.getSettings();

        settings.setJavaScriptEnabled(true);

        settings.setDomStorageEnabled(true);

        settings.setDatabaseEnabled(true);

        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setSupportMultipleWindows(true);

        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setAllowFileAccess(true);

        settings.setAllowContentAccess(true);

        settings.setBuiltInZoomControls(false);

        settings.setDisplayZoomControls(false);

        /*
         * Improve modern website rendering.
         */
        settings.setLoadWithOverviewMode(false);

        settings.setUseWideViewPort(false);

        /*
         * Cookies.
         */
        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);

        cookieManager.setAcceptThirdPartyCookies(
                webView,
                true
        );
    }

    /*
     * =========================================================
     * MAIN NAVIGATION
     * =========================================================
     */

    private void handleMainNavigation(String url) {

        if (url == null ||
                url.trim().isEmpty()) {

            return;
        }

        /*
         * Keep Deeprowss pages inside
         * the main website.
         */
        if (url.startsWith(
                "https://deeprows.github.io/"
        )) {

            if (mainWebView != null) {

                mainWebView.loadUrl(url);
            }

            return;
        }

        /*
         * External links / on-click ads
         * open inside the native popup.
         */
        openPopup(url);
    }

    /*
     * =========================================================
     * CHROME CLIENT
     * =========================================================
     */

    private WebChromeClient createChromeClient() {

        return new WebChromeClient() {

            /*
             * Handle window.open() and target="_blank".
             */
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
                    CustomViewCallback callback
            ) {

                showVideoFullscreen(
                        view,
                        callback
                );
            }

            /*
             * Exit video fullscreen.
             */
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

        customVideoView = view;

        customViewCallback = callback;

        /*
         * Hide normal website.
         */
        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.GONE
            );
        }

        /*
         * Hide popup if video is inside popup.
         */
        if (popupContainer != null) {

            popupContainer.setVisibility(
                    View.GONE
            );
        }

        /*
         * Put video above everything.
         */
        rootLayout.addView(
                customVideoView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        /*
         * Landscape.
         */
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        );

        /*
         * Status bar hidden.
         * Navigation bar remains visible.
         */
        hideStatusBar();
    }

    private void exitVideoFullscreen() {

        if (customVideoView == null) {

            return;
        }

        /*
         * Remove video.
         */
        rootLayout.removeView(
                customVideoView
        );

        customVideoView = null;

        /*
         * Notify player.
         */
        if (customViewCallback != null) {

            customViewCallback.onCustomViewHidden();

            customViewCallback = null;
        }

        /*
         * Return portrait.
         */
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );

        /*
         * Restore correct screen.
         */
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

        /*
         * If an old popup exists,
         * remove it first.
         */
        if (popupWebView != null) {

            try {

                popupWebView.stopLoading();

                popupWebView.destroy();

            } catch (Exception ignored) {
            }

            popupWebView = null;
        }

        popupWebView =
                new WebView(this);

        configureWebView(
                popupWebView
        );

        /*
         * Popup navigation stays inside
         * the popup.
         */
        popupWebView.setWebViewClient(
                new WebViewClient()
        );

        /*
         * IMPORTANT:
         *
         * The popup has its own ChromeClient,
         * including fullscreen video support.
         */
        popupWebView.setWebChromeClient(
                createChromeClient()
        );

        /*
         * Show popup before loading content.
         */
        showPopupContainer();

        return popupWebView;
    }

    private void openPopup(String url) {

        if (url == null ||
                url.trim().isEmpty()) {

            return;
        }

        WebView popup =
                createPopupWebView();

        popup.loadUrl(url);
    }

    /*
     * =========================================================
     * POPUP UI
     * =========================================================
     */

    private void showPopupContainer() {

        /*
         * Don't create a second container.
         */
        if (popupContainer != null) {

            return;
        }

        popupContainer =
                new FrameLayout(this);

        popupContainer.setBackgroundColor(
                Color.BLACK
        );

        /*
         * -----------------------------------------------------
         * TOP BAR
         * -----------------------------------------------------
         *
         * This bar is ABOVE the popup WebView.
         *
         * Therefore the X can never be covered
         * by the website/ad.
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
                dp(4),
                0,
                dp(4),
                0
        );

        topBar.setBackgroundColor(
                Color.rgb(15, 18, 24)
        );

        /*
         * BACK.
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

        title.setSingleLine(true);

        title.setPadding(
                dp(8),
                0,
                dp(8),
                0
        );

        /*
         * X CLOSE BUTTON.
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

        /*
         * Add BACK.
         */
        topBar.addView(
                backButton,
                new LinearLayout.LayoutParams(
                        popupBarHeight,
                        popupBarHeight
                )
        );

        /*
         * Add TITLE.
         */
        topBar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        popupBarHeight,
                        1
                )
        );

        /*
         * Add X.
         */
        topBar.addView(
                closeButton,
                new LinearLayout.LayoutParams(
                        popupBarHeight,
                        popupBarHeight
                )
        );

        /*
         * Top bar always stays at top.
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
         * -----------------------------------------------------
         * POPUP WEBVIEW
         * -----------------------------------------------------
         *
         * IMPORTANT:
         *
         * It starts below the top bar.
         *
         * This prevents the website from covering
         * the X button.
         */
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

        /*
         * Put popup above the website.
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

        /*
         * Hide website while popup is open.
         */
        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.INVISIBLE
            );
        }

        /*
         * Make sure status bar stays hidden.
         */
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

        /*
         * If popup video is fullscreen,
         * exit it first.
         */
        if (customVideoView != null) {

            exitVideoFullscreen();
        }

        /*
         * Stop and destroy popup WebView.
         */
        if (popupWebView != null) {

            try {

                popupWebView.stopLoading();

                popupWebView.onPause();

                popupWebView.destroy();

            } catch (Exception ignored) {
            }

            popupWebView = null;
        }

        /*
         * Remove popup container.
         */
        rootLayout.removeView(
                popupContainer
        );

        popupContainer = null;

        /*
         * Restore main website.
         */
        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.VISIBLE
            );
        }

        /*
         * Portrait.
         */
        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        );

        /*
         * Status bar hidden.
         */
        hideStatusBar();
    }

    /*
     * =========================================================
     * STATUS BAR
     * =========================================================
     *
     * Status bar:
     * HIDDEN
     *
     * Navigation bar:
     * VISIBLE
     */

    private void hideStatusBar() {

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        /*
         * Do NOT use:
         *
         * SYSTEM_UI_FLAG_HIDE_NAVIGATION
         * SYSTEM_UI_FLAG_IMMERSIVE
         * SYSTEM_UI_FLAG_IMMERSIVE_STICKY
         *
         * Navigation bar therefore remains visible.
         */
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

        /*
         * 1. Video fullscreen.
         */
        if (customVideoView != null) {

            exitVideoFullscreen();

            return;
        }

        /*
         * 2. Popup.
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
         * 3. Main website history.
         */
        if (mainWebView != null &&
                mainWebView.canGoBack()) {

            mainWebView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    /*
     * =========================================================
     * ACTIVITY LIFECYCLE
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

        if (customVideoView != null) {

            try {

                rootLayout.removeView(
                        customVideoView
                );

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

    /*
     * =========================================================
     * DP HELPER
     * =========================================================
     */

    private int dp(int value) {

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
     * PULL DOWN TO REFRESH
     * =========================================================
     *
     * Native implementation.
     *
     * No AndroidX dependency.
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

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    startY =
                            event.getY();

                    dragging = false;

                    break;

                case MotionEvent.ACTION_MOVE:

                    float distance =
                            event.getY() -
                            startY;

                    /*
                     * Only activate when
                     * website is at the top.
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
                MotionEvent event
        ) {

            if (webView == null ||
                    refreshing) {

                return true;
            }

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    startY =
                            event.getY();

                    dragging = true;

                    return true;

                case MotionEvent.ACTION_MOVE:

                    float distance =
                            event.getY() -
                            startY;

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

            refreshing = true;

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
