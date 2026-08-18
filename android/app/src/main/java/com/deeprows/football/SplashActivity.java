package com.deeprows.football;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {

    private static final int SPLASH_DURATION = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Hide STATUS BAR only.
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
         * Splash background.
         */
        LinearLayout splashLayout =
                new LinearLayout(this);

        splashLayout.setOrientation(
                LinearLayout.VERTICAL
        );

        splashLayout.setGravity(
                Gravity.CENTER
        );

        splashLayout.setBackgroundColor(
                Color.rgb(7, 9, 13)
        );

        /*
         * App name.
         */
        TextView appName =
                new TextView(this);

        appName.setText(
                "DEEPROWSS"
        );

        appName.setTextColor(
                Color.WHITE
        );

        appName.setTextSize(
                30
        );

        appName.setGravity(
                Gravity.CENTER
        );

        appName.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );

        /*
         * Add app name.
         */
        splashLayout.addView(
                appName,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        /*
         * Subtitle.
         */
        TextView subtitle =
                new TextView(this);

        subtitle.setText(
                "FOOTBALL LIVE"
        );

        subtitle.setTextColor(
                Color.rgb(150, 158, 170)
        );

        subtitle.setTextSize(
                13
        );

        subtitle.setGravity(
                Gravity.CENTER
        );

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = 8;

        splashLayout.addView(
                subtitle,
                subtitleParams
        );

        /*
         * Display splash.
         */
        setContentView(
                splashLayout
        );

        /*
         * Open MainActivity after 1.5 seconds.
         */
        new Handler(
                Looper.getMainLooper()
        ).postDelayed(() -> {

            Intent intent =
                    new Intent(
                            SplashActivity.this,
                            MainActivity.class
                    );

            startActivity(intent);

            finish();

        }, SPLASH_DURATION);
    }
}
