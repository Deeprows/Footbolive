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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.splashscreen.SplashScreen;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebResponse;

import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String WEBSITE_URL = "https://deeprowss.com";
    private static final String TELEGRAM_URL = "https://t.me/deeprows";

    /*
     * Chrome-like user agent.
     * This is intentionally kept because your WebToApk test showed
     * that this UA stopped the iframe sandbox problem.
     */
    private static final String DEEPROWSS_USER_AGENT =
            "Mozilla/5.0 (Linux; Android K) App{VERSION_CODE} "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.6099.144 Mobile Safari/537.36";

    private static final int BG_COLOR = Color.rgb(7, 9, 13);
    private static final int SURFACE_COLOR = Color.rgb(16, 20, 27);
    private static final int ACCENT_COLOR = Color.rgb(255, 23, 68);

    private static GeckoRuntime geckoRuntime;

    private FrameLayout rootLayout;

    private RefreshableGeckoContainer refreshContainer;
    private GeckoView mainGeckoView;
    private GeckoSession mainSession;

    private FrameLayout popupContainer;
    private LinearLayout popupTopBar;
    private TextView popupTitle;

    private final List<PopupEntry> popupStack = new ArrayList<>();

    private View customFullscreenView;
    private GeckoSession fullscreenSession;

    private FrameLayout splashView;
    private ProgressBar splashSpinner;
    private TextView splashLoadingText;

    private boolean pageVisible = false;
    private boolean showingOfflinePage = false;
    private boolean activityDestroyed = false;

    /*
     * GeckoView 153 does not expose canGoBack().
     * NavigationDelegate.onCanGoBack() updates these values instead.
     */
    private boolean mainCanGoBack = false;

    private ExoPlayer exoPlayer;
    private PlayerView nativePlayerView;
    private boolean nativePlayerShowing = false;

    private int popupBarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        getWindow().setStatusBarColor(BG_COLOR);
        getWindow().setNavigationBarColor(BG_COLOR);
        getWindow().setBackgroundDrawableResource(android.R.color.black);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(BG_COLOR);
        setContentView(rootLayout);

        popupBarHeight = dp(58);

        showCustomSplash();

        createMainGeckoView();

        mainSession.loadUri(WEBSITE_URL);
    }

    // ============================================================
    // GECKOVIEW RUNTIME
    // ============================================================

    private GeckoRuntime getGeckoRuntime() {
        if (geckoRuntime == null) {

            GeckoRuntimeSettings runtimeSettings =
                    new GeckoRuntimeSettings.Builder()
                            .build();

            geckoRuntime = GeckoRuntime.create(
                    getApplicationContext(),
                    runtimeSettings
            );
        }

        return geckoRuntime;
    }

    private GeckoSessionSettings createSessionSettings() {

        return new GeckoSessionSettings.Builder()
                .allowJavascript(true)
                .userAgentMode(
                        GeckoSessionSettings.USER_AGENT_MODE_MOBILE
                )
                .viewportMode(
                        GeckoSessionSettings.VIEWPORT_MODE_MOBILE
                )
                .userAgentOverride(DEEPROWSS_USER_AGENT)
                .suspendMediaWhenInactive(false)
                .useTrackingProtection(false)
                .build();
    }

    // ============================================================
    // MAIN GECKOVIEW
    // ============================================================

    private void createMainGeckoView() {

        mainGeckoView = new GeckoView(this);
        mainGeckoView.setBackgroundColor(BG_COLOR);

        mainSession = new GeckoSession(createSessionSettings());

        configureSession(mainSession, false);

        mainSession.open(getGeckoRuntime());

        mainGeckoView.setSession(mainSession);

        refreshContainer =
                new RefreshableGeckoContainer(this);

        refreshContainer.setBackgroundColor(BG_COLOR);

        refreshContainer.setOnRefreshListener(() -> {

            if (mainSession != null) {

                if (showingOfflinePage) {

                    showingOfflinePage = false;

                    mainSession.loadUri(WEBSITE_URL);

                } else {

                    mainSession.reload();
                }
            }
        });

        refreshContainer.addView(
                mainGeckoView,
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

    private void configureSession(
            GeckoSession session,
            boolean popup
    ) {

        session.setNavigationDelegate(
                new GeckoSession.NavigationDelegate() {

                    @Override
                    public GeckoResult<AllowOrDeny> onLoadRequest(
                            GeckoSession session,
                            GeckoSession.NavigationDelegate.LoadRequest request
                    ) {

                        String url = request.uri;

                        if (url == null || url.isEmpty()) {

                            return GeckoResult.fromValue(
                                    AllowOrDeny.DENY
                            );
                        }

                        /*
                         * New-window request.
                         */
                        if (request.target
                                == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW) {

                            if (isTelegramUrl(url)) {

                                openExternalUrl(url);

                                return GeckoResult.fromValue(
                                        AllowOrDeny.DENY
                                );
                            }

                            if (isDirectMediaUrl(url)) {

                                playNativeMedia(url);

                                return GeckoResult.fromValue(
                                        AllowOrDeny.DENY
                                );
                            }

                            openPopup(url);

                            return GeckoResult.fromValue(
                                    AllowOrDeny.DENY
                            );
                        }

                        /*
                         * Telegram always goes outside the app.
                         */
                        if (isTelegramUrl(url)) {

                            openExternalUrl(url);

                            return GeckoResult.fromValue(
                                    AllowOrDeny.DENY
                            );
                        }

                        /*
                         * Direct media URLs use native Media3.
                         */
                        if (isDirectMediaUrl(url)) {

                            playNativeMedia(url);

                            return GeckoResult.fromValue(
                                    AllowOrDeny.DENY
                            );
                        }

                        /*
                         * Non-http schemes.
                         */
                        if (!isHttpUrl(url)) {

                            openExternalUrl(url);

                            return GeckoResult.fromValue(
                                    AllowOrDeny.DENY
                            );
                        }

                        /*
                         * Main Deeprowss page stays in the main GeckoView.
                         */
                        if (!popup) {

                            if (isMainSiteUrl(url)
                                    || url.startsWith(
                                    "https://deeprows.github.io/"
                            )) {

                                return null;
                            }

                            /*
                             * External HTTP/HTTPS links opened in popup.
                             */
                            if (!request.isRedirect
                                    && !request.isDirectNavigation) {

                                openPopup(url);

                                return GeckoResult.fromValue(
                                        AllowOrDeny.DENY
                                );
                            }
                        }

                        return null;
                    }

                    /*
                     * GeckoView 153 history API.
                     *
                     * There is no session.canGoBack().
                     * GeckoView tells us through this callback.
                     */
                    @Override
                    public void onCanGoBack(
                            GeckoSession session,
                            boolean canGoBack
                    ) {

                        if (session == mainSession) {

                            mainCanGoBack = canGoBack;

                            return;
                        }

                        for (PopupEntry entry : popupStack) {

                            if (entry.session == session) {

                                entry.canGoBack = canGoBack;

                                break;
                            }
                        }
                    }

                    @Override
                    public GeckoResult<GeckoSession> onNewSession(
                            GeckoSession session,
                            String uri
                    ) {

                        if (uri == null || uri.isEmpty()) {
                            return null;
                        }

                        GeckoSession newSession =
                                createPopupSession();

                        PopupEntry entry =
                                new PopupEntry(
                                        newSession,
                                        null
                                );

                        popupStack.add(entry);

                        showPopupContainer();

                        attachPopupSession(entry);

                        return GeckoResult.fromValue(
                                newSession
                        );
                    }

                    @Override
                    public GeckoResult<String> onLoadError(
                            GeckoSession session,
                            String uri,
                            org.mozilla.geckoview.WebRequestError error
                    ) {

                        if (session == mainSession) {

                            showOfflinePage();

                        } else {

                            showPopupError(session);
                        }

                        return null;
                    }
                }
        );

        session.setProgressDelegate(
                new GeckoSession.ProgressDelegate() {

                    @Override
                    public void onPageStart(
                            GeckoSession session,
                            String url
                    ) {

                        if (session == mainSession) {

                            showingOfflinePage = false;
                            pageVisible = true;
                        }
                    }

                    @Override
                    public void onPageStop(
                            GeckoSession session,
                            boolean success
                    ) {

                        if (session == mainSession) {

                            if (success) {

                                showingOfflinePage = false;
                                pageVisible = true;

                                hideCustomSplash();

                            } else {

                                showOfflinePage();
                            }

                        } else {

                            if (success) {

                                hidePopupError();
                            }
                        }
                    }
                }
        );

        session.setContentDelegate(
                new GeckoSession.ContentDelegate() {

                    @Override
                    public void onTitleChange(
                            GeckoSession session,
                            String title
                    ) {

                        if (session != mainSession) {

                            if (popupTitle != null
                                    && title != null
                                    && !title.trim().isEmpty()) {

                                popupTitle.setText(title);
                            }
                        }
                    }

                    @Override
                    public void onFirstContentfulPaint(
                            GeckoSession session
                    ) {

                        if (session == mainSession) {

                            pageVisible = true;

                            hideCustomSplash();
                        }
                    }

                    @Override
                    public void onFullScreen(
                            GeckoSession session,
                            boolean fullScreen
                    ) {

                        if (fullScreen) {

                            enterGeckoFullscreen(session);

                        } else {

                            exitGeckoFullscreen();
                        }
                    }

                    @Override
                    public void onCloseRequest(
                            GeckoSession session
                    ) {

                        if (session == mainSession) {

                            finish();

                        } else {

                            removePopupSession(session);
                        }
                    }

                    @Override
                    public void onExternalResponse(
                            GeckoSession session,
                            WebResponse response
                    ) {

                        handleGeckoDownload(response);
                    }

                    @Override
                    public void onCrash(
                            GeckoSession session
                    ) {

                        if (session == mainSession) {

                            showOfflinePage();

                        } else {

                            showPopupError(session);
                        }
                    }

                    @Override
                    public void onKill(
                            GeckoSession session
                    ) {

                        if (session == mainSession) {

                            showOfflinePage();
                        }
                    }
                }
        );
    }

    // ============================================================
    // POPUP SESSIONS
    // ============================================================

    private GeckoSession createPopupSession() {

        GeckoSession session =
                new GeckoSession(
                        createSessionSettings()
                );

        configureSession(
                session,
                true
        );

        session.open(
                getGeckoRuntime()
        );

        return session;
    }

    private void openPopup(String url) {

        if (!isHttpUrl(url)) {

            openExternalUrl(url);

            return;
        }

        GeckoSession session =
                createPopupSession();

        PopupEntry entry =
                new PopupEntry(
                        session,
                        null
                );

        popupStack.add(entry);

        showPopupContainer();

        attachPopupSession(entry);

        session.loadUri(url);
    }

    private void showPopupContainer() {

        if (popupContainer == null) {

            popupContainer =
                    new FrameLayout(this);

            popupContainer.setBackgroundColor(
                    BG_COLOR
            );

            rootLayout.addView(
                    popupContainer,
                    new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                    )
            );

            createPopupTopBar();

        } else {

            popupContainer.setVisibility(
                    View.VISIBLE
            );
        }

        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.INVISIBLE
            );
        }

        popupContainer.bringToFront();
    }

    private void createPopupTopBar() {

        popupTopBar =
                new LinearLayout(this);

        popupTopBar.setOrientation(
                LinearLayout.HORIZONTAL
        );

        popupTopBar.setGravity(
                Gravity.CENTER_VERTICAL
        );

        popupTopBar.setPadding(
                dp(8),
                0,
                dp(8),
                0
        );

        popupTopBar.setBackgroundColor(
                SURFACE_COLOR
        );

        TextView backButton =
                new TextView(this);

        backButton.setText("‹");
        backButton.setTextColor(Color.WHITE);
        backButton.setTextSize(34);
        backButton.setGravity(Gravity.CENTER);

        backButton.setPadding(
                dp(6),
                0,
                dp(6),
                dp(4)
        );

        backButton.setOnClickListener(v -> {

            PopupEntry active =
                    getActivePopup();

            if (active == null) {
                return;
            }

            if (active.canGoBack) {

                active.session.goBack();

            } else {

                switchToPreviousPopupWindow();
            }
        });

        popupTopBar.addView(
                backButton,
                new LinearLayout.LayoutParams(
                        dp(48),
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        popupTitle =
                new TextView(this);

        popupTitle.setText(
                "Deeprowss"
        );

        popupTitle.setTextColor(
                Color.WHITE
        );

        popupTitle.setTextSize(15);

        popupTitle.setGravity(
                Gravity.CENTER_VERTICAL
        );

        popupTitle.setSingleLine(true);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        0,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        1
                );

        popupTopBar.addView(
                popupTitle,
                titleParams
        );

        TextView closeButton =
                new TextView(this);

        closeButton.setText("×");
        closeButton.setTextColor(Color.WHITE);
        closeButton.setTextSize(30);
        closeButton.setGravity(Gravity.CENTER);

        closeButton.setOnClickListener(
                v -> closePopup()
        );

        popupTopBar.addView(
                closeButton,
                new LinearLayout.LayoutParams(
                        dp(48),
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        popupContainer.addView(
                popupTopBar,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        popupBarHeight,
                        Gravity.TOP
                )
        );
    }

    private void attachPopupSession(
            PopupEntry entry
    ) {

        if (entry == null) {
            return;
        }

        if (entry.view == null) {

            entry.view =
                    new GeckoView(this);

            entry.view.setBackgroundColor(
                    BG_COLOR
            );

            entry.view.setSession(
                    entry.session
            );
        }

        /*
         * Remove currently displayed popup browser views.
         */
        for (PopupEntry item : popupStack) {

            if (item.view != null) {

                item.view.setVisibility(
                        View.GONE
                );

                if (item.view.getParent()
                        instanceof FrameLayout) {

                    ((FrameLayout) item.view.getParent())
                            .removeView(item.view);
                }
            }
        }

        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                );

        params.topMargin =
                popupBarHeight;

        popupContainer.addView(
                entry.view,
                params
        );

        entry.view.setVisibility(
                View.VISIBLE
        );

        entry.view.bringToFront();

        if (popupTopBar != null) {

            popupTopBar.bringToFront();
        }

        entry.view.requestFocus();
    }

    private PopupEntry getActivePopup() {

        if (popupStack.isEmpty()) {
            return null;
        }

        return popupStack.get(
                popupStack.size() - 1
        );
    }

    private void switchToPreviousPopupWindow() {

        if (popupStack.size() <= 1) {

            closePopup();

            return;
        }

        PopupEntry current =
                popupStack.remove(
                        popupStack.size() - 1
                );

        destroyPopupEntry(current);

        PopupEntry previous =
                getActivePopup();

        if (previous != null) {

            attachPopupSession(
                    previous
            );
        }
    }

    private void removePopupSession(
            GeckoSession session
    ) {

        for (int i = popupStack.size() - 1;
             i >= 0;
             i--) {

            PopupEntry entry =
                    popupStack.get(i);

            if (entry.session == session) {

                popupStack.remove(i);

                destroyPopupEntry(
                        entry
                );

                break;
            }
        }

        if (popupStack.isEmpty()) {

            closePopup();

        } else {

            attachPopupSession(
                    getActivePopup()
            );
        }
    }

    private void destroyPopupEntry(
            PopupEntry entry
    ) {

        if (entry == null) {
            return;
        }

        if (entry.view != null) {

            if (entry.view.getParent()
                    instanceof FrameLayout) {

                ((FrameLayout) entry.view.getParent())
                        .removeView(
                                entry.view
                        );
            }

            entry.view.releaseSession();
            entry.view = null;
        }

        try {

            entry.session.close();

        } catch (Exception ignored) {
        }
    }

    private void closePopup() {

        exitGeckoFullscreen();

        for (PopupEntry entry : popupStack) {

            destroyPopupEntry(
                    entry
            );
        }

        popupStack.clear();

        if (popupContainer != null) {

            if (popupContainer.getParent()
                    instanceof FrameLayout) {

                ((FrameLayout) popupContainer.getParent())
                        .removeView(
                                popupContainer
                        );
            }

            popupContainer = null;
        }

        popupTopBar = null;
        popupTitle = null;

        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.VISIBLE
            );

            refreshContainer.bringToFront();
        }

        setPortrait();
    }

    // ============================================================
    // FULLSCREEN
    // ============================================================

    private void enterGeckoFullscreen(
            GeckoSession session
    ) {

        fullscreenSession =
                session;

        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.INVISIBLE
            );
        }

        if (popupContainer != null) {

            popupContainer.setVisibility(
                    View.INVISIBLE
            );
        }

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setLandscape();
    }

    private void exitGeckoFullscreen() {

        fullscreenSession = null;

        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        if (popupContainer != null
                && !popupStack.isEmpty()) {

            popupContainer.setVisibility(
                    View.VISIBLE
            );

        } else if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.VISIBLE
            );
        }

        setPortrait();
    }

    // ============================================================
    // OFFLINE SCREEN
    // ============================================================

    private void showOfflinePage() {

        if (activityDestroyed) {
            return;
        }

        showingOfflinePage = true;

        String html =
                "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<style>" +
                "html,body{" +
                "margin:0;" +
                "width:100%;" +
                "height:100%;" +
                "background:#07090d;" +
                "color:white;" +
                "font-family:Arial,sans-serif;" +
                "}" +
                "body{" +
                "display:flex;" +
                "align-items:center;" +
                "justify-content:center;" +
                "text-align:center;" +
                "}" +
                ".box{padding:30px;max-width:360px;}" +
                ".logo{" +
                "font-size:48px;" +
                "font-weight:900;" +
                "color:#ff1744;" +
                "margin-bottom:18px;" +
                "}" +
                "h2{margin:0 0 12px;font-size:22px;}" +
                "p{" +
                "color:#aeb4bf;" +
                "line-height:1.5;" +
                "font-size:14px;" +
                "}" +
                "button{" +
                "margin-top:15px;" +
                "border:0;" +
                "border-radius:8px;" +
                "padding:13px 25px;" +
                "background:#ff1744;" +
                "color:white;" +
                "font-weight:bold;" +
                "}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='box'>" +
                "<div class='logo'>D</div>" +
                "<h2>You're offline</h2>" +
                "<p>" +
                "We couldn't connect to Deeprowss right now. " +
                "Please check your internet connection and try again." +
                "</p>" +
                "<button onclick=\"location.href='" +
                WEBSITE_URL +
                "'\">TRY AGAIN</button>" +
                "</div>" +
                "</body>" +
                "</html>";

        if (mainSession != null) {

            mainSession.load(
                    new GeckoSession.Loader()
                            .data(
                                    html,
                                    "text/html"
                            )
            );
        }

        hideCustomSplash();
    }

    private void showPopupError(
            GeckoSession session
    ) {

        Toast.makeText(
                this,
                "Unable to connect at this time",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void hidePopupError() {
        // Reserved for future popup error overlay.
    }

    // ============================================================
    // DOWNLOADS
    // ============================================================

    private void handleGeckoDownload(
            WebResponse response
    ) {

        if (response == null
                || response.uri == null) {

            return;
        }

        String url =
                response.uri;

        if (!isHttpUrl(url)) {

            openExternalUrl(url);

            return;
        }

        try {

            DownloadManager manager =
                    (DownloadManager) getSystemService(
                            DOWNLOAD_SERVICE
                    );

            if (manager == null) {

                openExternalUrl(url);

                return;
            }

            DownloadManager.Request request =
                    new DownloadManager.Request(
                            Uri.parse(url)
                    );

            request.setNotificationVisibility(
                    DownloadManager.Request
                            .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            );

            String filename =
                    getDownloadFileName(
                            response,
                            url
                    );

            request.setTitle(
                    filename
            );

            request.setDescription(
                    "Downloading from Deeprowss"
            );

            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    filename
            );

            /*
             * GeckoView 153 WebResponse does not expose
             * contentType directly.
             *
             * Guess the MIME type from the URL instead.
             */
            String mimeType =
                    URLConnection.guessContentTypeFromName(
                            url
                    );

            if (mimeType != null
                    && !mimeType.isEmpty()) {

                request.setMimeType(
                        mimeType
                );
            }

            manager.enqueue(
                    request
            );

            Toast.makeText(
                    this,
                    "Download started",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            openExternalUrl(url);
        }
    }

    private String getDownloadFileName(
            WebResponse response,
            String url
    ) {

        /*
         * WebResponse in GeckoView 153 does not expose
         * filename directly, so derive it from the URL.
         */
        try {

            String path =
                    Uri.parse(url).getPath();

            if (path != null) {

                int slash =
                        path.lastIndexOf('/');

                if (slash >= 0
                        && slash < path.length() - 1) {

                    String name =
                            path.substring(
                                    slash + 1
                            );

                    if (!name.isEmpty()) {

                        name = name.trim();

                        if (!name.isEmpty()) {

                            return sanitizeFilename(
                                    name
                            );
                        }
                    }
                }
            }

        } catch (Exception ignored) {
        }

        return "deeprowss-download";
    }

    private String sanitizeFilename(
            String filename
    ) {

        return filename
                .replace("/", "_")
                .replace("\\", "_")
                .replace(":", "_")
                .replace("*", "_")
                .replace("?", "_")
                .replace("\"", "_")
                .replace("<", "_")
                .replace(">", "_")
                .replace("|", "_");
    }

    // ============================================================
    // MEDIA3 DIRECT PLAYER
    // ============================================================

    private boolean isDirectMediaUrl(
            String url
    ) {

        if (url == null) {
            return false;
        }

        String clean =
                url;

        int query =
                clean.indexOf('?');

        if (query >= 0) {

            clean =
                    clean.substring(
                            0,
                            query
                    );
        }

        clean =
                clean.toLowerCase();

        return clean.endsWith(".mp4")
                || clean.endsWith(".m4v")
                || clean.endsWith(".webm")
                || clean.endsWith(".m3u8")
                || clean.endsWith(".mpd");
    }

    private void playNativeMedia(
            String url
    ) {

        if (!isDirectMediaUrl(url)) {

            openPopup(url);

            return;
        }

        releaseNativePlayer();

        nativePlayerView =
                new PlayerView(this);

        nativePlayerView.setBackgroundColor(
                Color.BLACK
        );

        nativePlayerView.setUseController(
                true
        );

        exoPlayer =
                new ExoPlayer.Builder(this)
                        .build();

        nativePlayerView.setPlayer(
                exoPlayer
        );

        MediaItem mediaItem =
                MediaItem.fromUri(
                        Uri.parse(url)
                );

        exoPlayer.setMediaItem(
                mediaItem
        );

        exoPlayer.addListener(
                new Player.Listener() {

                    @Override
                    public void onPlaybackStateChanged(
                            int state
                    ) {

                        if (state
                                == Player.STATE_ENDED) {

                            exitNativeMedia();
                        }
                    }

                    @Override
                    public void onPlayerError(
                            androidx.media3.common.PlaybackException error
                    ) {

                        Toast.makeText(
                                MainActivity.this,
                                "Unable to play this video",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        rootLayout.addView(
                nativePlayerView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        nativePlayerView.bringToFront();

        if (popupContainer != null) {

            popupContainer.setVisibility(
                    View.INVISIBLE
            );
        }

        if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.INVISIBLE
            );
        }

        nativePlayerShowing =
                true;

        setLandscape();

        exoPlayer.prepare();
        exoPlayer.play();
    }

    private void exitNativeMedia() {

        if (!nativePlayerShowing) {
            return;
        }

        releaseNativePlayer();

        nativePlayerShowing =
                false;

        if (popupContainer != null
                && !popupStack.isEmpty()) {

            popupContainer.setVisibility(
                    View.VISIBLE
            );

            popupContainer.bringToFront();

        } else if (refreshContainer != null) {

            refreshContainer.setVisibility(
                    View.VISIBLE
            );

            refreshContainer.bringToFront();
        }

        setPortrait();
    }

    private void releaseNativePlayer() {

        if (exoPlayer != null) {

            exoPlayer.stop();
            exoPlayer.release();

            exoPlayer = null;
        }

        if (nativePlayerView != null) {

            if (nativePlayerView.getParent()
                    instanceof FrameLayout) {

                ((FrameLayout) nativePlayerView.getParent())
                        .removeView(
                                nativePlayerView
                        );
            }

            nativePlayerView.setPlayer(
                    null
            );

            nativePlayerView = null;
        }
    }

    // ============================================================
    // EXTERNAL LINKS
    // ============================================================

    private boolean isTelegramUrl(
            String url
    ) {

        try {

            Uri uri =
                    Uri.parse(url);

            String host =
                    uri.getHost();

            if (host == null) {
                return false;
            }

            host =
                    host.toLowerCase();

            return host.equals("t.me")
                    || host.equals("telegram.me")
                    || host.equals("www.telegram.me");

        } catch (Exception e) {

            return false;
        }
    }

    private boolean isHttpUrl(
            String url
    ) {

        if (url == null) {
            return false;
        }

        return url.startsWith("http://")
                || url.startsWith("https://");
    }

    private boolean isMainSiteUrl(
            String url
    ) {

        try {

            Uri uri =
                    Uri.parse(url);

            String host =
                    uri.getHost();

            if (host == null) {
                return false;
            }

            host =
                    host.toLowerCase();

            return host.equals("deeprowss.com")
                    || host.equals("www.deeprowss.com");

        } catch (Exception e) {

            return false;
        }
    }

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

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Unable to open link",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // ============================================================
    // BACK BUTTON
    // ============================================================

    @Override
    public void onBackPressed() {

        if (nativePlayerShowing) {

            exitNativeMedia();

            return;
        }

        if (fullscreenSession != null) {

            exitGeckoFullscreen();

            return;
        }

        PopupEntry activePopup =
                getActivePopup();

        if (activePopup != null) {

            if (activePopup.canGoBack) {

                activePopup.session.goBack();

            } else {

                switchToPreviousPopupWindow();
            }

            return;
        }

        if (mainSession != null
                && mainCanGoBack) {

            mainSession.goBack();

            return;
        }

        super.onBackPressed();
    }

    // ============================================================
    // LIFECYCLE
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (mainSession != null) {

            mainSession.setActive(
                    true
            );
        }

        for (PopupEntry entry : popupStack) {

            entry.session.setActive(
                    true
            );
        }
    }

    @Override
    protected void onPause() {

        if (mainSession != null) {

            mainSession.setActive(
                    false
            );
        }

        for (PopupEntry entry : popupStack) {

            entry.session.setActive(
                    false
            );
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        activityDestroyed =
                true;

        releaseNativePlayer();

        for (PopupEntry entry : popupStack) {

            destroyPopupEntry(
                    entry
            );
        }

        popupStack.clear();

        if (mainGeckoView != null) {

            try {

                mainGeckoView.releaseSession();

            } catch (Exception ignored) {
            }

            mainGeckoView = null;
        }

        if (mainSession != null) {

            try {

                mainSession.close();

            } catch (Exception ignored) {
            }

            mainSession = null;
        }

        super.onDestroy();
    }

    // ============================================================
    // SPLASH
    // ============================================================

    private void showCustomSplash() {

        if (splashView != null) {
            return;
        }

        splashView =
                new FrameLayout(this);

        splashView.setBackgroundColor(
                BG_COLOR
        );

        ImageView logo =
                new ImageView(this);

        try {

            int drawableId =
                    getResources().getIdentifier(
                            "deeprowss_splash",
                            "drawable",
                            getPackageName()
                    );

            if (drawableId != 0) {

                logo.setImageResource(
                        drawableId
                );
            }

        } catch (Exception ignored) {
        }

        logo.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        FrameLayout.LayoutParams logoParams =
                new FrameLayout.LayoutParams(
                        dp(330),
                        dp(330),
                        Gravity.CENTER
                );

        splashView.addView(
                logo,
                logoParams
        );

        splashSpinner =
                new ProgressBar(this);

        FrameLayout.LayoutParams spinnerParams =
                new FrameLayout.LayoutParams(
                        dp(32),
                        dp(32),
                        Gravity.CENTER
                );

        spinnerParams.topMargin =
                dp(185);

        splashView.addView(
                splashSpinner,
                spinnerParams
        );

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

        FrameLayout.LayoutParams textParams =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        dp(40),
                        Gravity.CENTER
                );

        textParams.topMargin =
                dp(240);

        splashView.addView(
                splashLoadingText,
                textParams
        );

        rootLayout.addView(
                splashView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                )
        );

        splashView.bringToFront();
    }

    private void hideCustomSplash() {

        if (splashView == null) {
            return;
        }

        final View splash =
                splashView;

        splash.animate()
                .alpha(0f)
                .setDuration(180)
                .withEndAction(() -> {

                    if (splash.getParent()
                            instanceof FrameLayout) {

                        ((FrameLayout) splash.getParent())
                                .removeView(
                                        splash
                                );
                    }

                    if (splashView == splash) {

                        splashView =
                                null;
                    }

                    splashSpinner =
                            null;

                    splashLoadingText =
                            null;

                })
                .start();
    }

    // ============================================================
    // ORIENTATION
    // ============================================================

    private void setLandscape() {

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        );
    }

    private void setPortrait() {

        setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        );
    }

    // ============================================================
    // UTILITIES
    // ============================================================

    private int dp(int value) {

        return Math.round(
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    // ============================================================
    // POPUP ENTRY
    // ============================================================

    private static class PopupEntry {

        final GeckoSession session;

        GeckoView view;

        boolean canGoBack = false;

        PopupEntry(
                GeckoSession session,
                GeckoView view
        ) {

            this.session =
                    session;

            this.view =
                    view;
        }
    }

    // ============================================================
    // PULL TO REFRESH
    // ============================================================

    private static class RefreshableGeckoContainer
            extends FrameLayout {

        private float downY;
        private float currentDistance;

        private boolean dragging;
        private boolean refreshing;

        private final ProgressBar spinner;

        private Runnable refreshListener;

        RefreshableGeckoContainer(
                Context context
        ) {

            super(context);

            setClipChildren(
                    false
            );

            spinner =
                    new ProgressBar(
                            context
                    );

            spinner.setVisibility(
                    View.GONE
            );

            LayoutParams spinnerParams =
                    new LayoutParams(
                            36,
                            36,
                            Gravity.TOP
                                    | Gravity.CENTER_HORIZONTAL
                    );

            spinnerParams.topMargin =
                    12;

            addView(
                    spinner,
                    spinnerParams
            );
        }

        void setOnRefreshListener(
                Runnable listener
        ) {

            refreshListener =
                    listener;
        }

        @Override
        public boolean onInterceptTouchEvent(
                MotionEvent event
        ) {

            if (refreshing) {
                return true;
            }

            if (getChildCount() == 0) {

                return super.onInterceptTouchEvent(
                        event
                );
            }

            View child =
                    getChildAt(0);

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    downY =
                            event.getY();

                    currentDistance =
                            0;

                    dragging =
                            false;

                    return super.onInterceptTouchEvent(
                            event
                    );

                case MotionEvent.ACTION_MOVE:

                    float dy =
                            event.getY()
                                    - downY;

                    if (dy > 0
                            && !child.canScrollVertically(-1)) {

                        dragging =
                                true;

                        return true;
                    }

                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:

                    dragging =
                            false;

                    break;
            }

            return super.onInterceptTouchEvent(
                    event
            );
        }

        @Override
        public boolean onTouchEvent(
                MotionEvent event
        ) {

            if (refreshing) {
                return true;
            }

            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:

                    downY =
                            event.getY();

                    currentDistance =
                            0;

                    return true;

                case MotionEvent.ACTION_MOVE:

                    float dy =
                            event.getY()
                                    - downY;

                    if (dy <= 0) {
                        return true;
                    }

                    currentDistance =
                            Math.min(
                                    dy,
                                    300
                            );

                    View child =
                            getChildCount() > 0
                                    ? getChildAt(0)
                                    : null;

                    if (child != null) {

                        child.setTranslationY(
                                currentDistance
                                        * 0.55f
                        );
                    }

                    if (currentDistance > 80) {

                        spinner.setVisibility(
                                View.VISIBLE
                        );
                    }

                    return true;

                case MotionEvent.ACTION_UP:

                    if (currentDistance >= 180) {

                        startRefreshing();

                    } else {

                        resetPosition();
                    }

                    return true;

                case MotionEvent.ACTION_CANCEL:

                    resetPosition();

                    return true;
            }

            return true;
        }

        private void startRefreshing() {

            refreshing =
                    true;

            spinner.setVisibility(
                    View.VISIBLE
            );

            View child =
                    getChildCount() > 0
                            ? getChildAt(0)
                            : null;

            if (child != null) {

                child.animate()
                        .translationY(70)
                        .setDuration(180)
                        .start();
            }

            if (refreshListener != null) {

                refreshListener.run();
            }

            postDelayed(
                    this::stopRefreshing,
                    1000
            );
        }

        private void stopRefreshing() {

            refreshing =
                    false;

            resetPosition();
        }

        private void resetPosition() {

            View child =
                    getChildCount() > 0
                            ? getChildAt(0)
                            : null;

            if (child != null) {

                child.animate()
                        .translationY(0)
                        .setDuration(180)
                        .start();
            }

            spinner.setVisibility(
                    View.GONE
            );

            currentDistance =
                    0;

            dragging =
                    false;
        }
    }
}
