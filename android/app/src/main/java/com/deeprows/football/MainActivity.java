package com.deeprows.football;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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

public class MainActivity extends Activity {

    private WebView mainWebView;
    private WebView popupWebView;
    private FrameLayout rootLayout;
    private FrameLayout popupContainer;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    private final String WEBSITE_URL =
            "https://deeprows.github.io/Footbolive/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );

        rootLayout = new FrameLayout(this);
        setContentView(rootLayout);

        createMainWebView();

        mainWebView.loadUrl(WEBSITE_URL);
    }

    private void createMainWebView() {

        mainWebView = new WebView(this);

        WebSettings settings = mainWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        CookieManager cookieManager =
                CookieManager.getInstance();

        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(
                mainWebView,
                true
        );

        mainWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                String url = request.getUrl().toString();

                if (url.startsWith("https://deeprows.github.io/")) {
                    return false;
                }

                openPopup(url);

                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url) {

                if (url.startsWith("https://deeprows.github.io/")) {
                    return false;
                }

                openPopup(url);

                return true;
            }
        });

        mainWebView.setWebChromeClient(
                new WebChromeClient() {

                    @Override
                    public boolean onCreateWindow(
                            WebView view,
                            boolean isDialog,
                            boolean isUserGesture,
                            android.os.Message resultMsg) {

                        WebView popup = createPopupWebView();

                        WebView.WebViewTransport transport =
                                (WebView.WebViewTransport)
                                        resultMsg.obj;

                        transport.setWebView(popup);

                        resultMsg.sendToTarget();

                        return true;
                    }

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

                        rootLayout.addView(
                                customView,
                                new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                )
                        );

                        mainWebView.setVisibility(View.GONE);

                        setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        );

                        hideSystemBars();
                    }

                    @Override
                    public void onHideCustomView() {

                        if (customView == null) {
                            return;
                        }

                        rootLayout.removeView(customView);

                        customView = null;

                        if (customViewCallback != null) {
                            customViewCallback.onCustomViewHidden();
                            customViewCallback = null;
                        }

                        mainWebView.setVisibility(View.VISIBLE);

                        setRequestedOrientation(
                                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        );

                        showSystemBars();
                    }
                }
        );

        rootLayout.addView(
                mainWebView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );
    }

    private WebView createPopupWebView() {

        popupWebView = new WebView(this);

        WebSettings settings =
                popupWebView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        CookieManager.getInstance()
                .setAcceptThirdPartyCookies(
                        popupWebView,
                        true
                );

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

        WebView popup = createPopupWebView();

        popup.loadUrl(url);
    }

    private void showPopupContainer() {

        if (popupContainer != null) {
            return;
        }

        popupContainer = new FrameLayout(this);

        popupContainer.setBackgroundColor(
                Color.BLACK
        );

        LinearLayout topBar =
                new LinearLayout(this);

        topBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        topBar.setBackgroundColor(
                Color.rgb(15, 18, 24)
        );

        ImageButton backButton =
                new ImageButton(this);

        backButton.setImageResource(
                android.R.drawable.ic_media_previous
        );

        backButton.setBackgroundColor(
                Color.TRANSPARENT
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

        ImageButton closeButton =
                new ImageButton(this);

        closeButton.setImageResource(
                android.R.drawable.ic_menu_close_clear_cancel
        );

        closeButton.setBackgroundColor(
                Color.TRANSPARENT
        );

        closeButton.setOnClickListener(
                v -> closePopup()
        );

        topBar.addView(
                backButton,
                new LinearLayout.LayoutParams(
                        60,
                        60
                )
        );

        LinearLayout.LayoutParams spacerParams =
                new LinearLayout.LayoutParams(
                        0,
                        60,
                        1
                );

        topBar.addView(
                new View(this),
                spacerParams
        );

        topBar.addView(
                closeButton,
                new LinearLayout.LayoutParams(
                        60,
                        60
                )
        );

        popupContainer.addView(
                topBar,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        60
                )
        );

        FrameLayout.LayoutParams webParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        webParams.topMargin = 60;

        popupContainer.addView(
                popupWebView,
                webParams
        );

        rootLayout.addView(
                popupContainer,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        mainWebView.setVisibility(View.INVISIBLE);
    }

    private void closePopup() {

        if (popupContainer != null) {

            if (popupWebView != null) {
                popupWebView.stopLoading();
                popupWebView.destroy();
                popupWebView = null;
            }

            rootLayout.removeView(
                    popupContainer
            );

            popupContainer = null;
        }

        mainWebView.setVisibility(
                View.VISIBLE
        );
    }

    private void hideSystemBars() {

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void showSystemBars() {

        getWindow().getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                );
    }

    @Override
    public void onBackPressed() {

        if (customView != null) {

            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
            }

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

        if (mainWebView.canGoBack()) {

            mainWebView.goBack();

        } else {

            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {

        if (mainWebView != null) {
            mainWebView.destroy();
        }

        super.onDestroy();
    }
}
