package br.com.blockneon.android;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import br.com.blockneon.Main;
import br.com.blockneon.ad.AdBridge;

public class AndroidLauncher extends AndroidApplication implements AdBridge {
    private AdView adView;
    private RelativeLayout rootLayout;
    private View gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        MobileAds.initialize(this, initializationStatus -> {});

        AndroidApplicationConfiguration configuration =
            new AndroidApplicationConfiguration();

        gameView = initializeForView(new Main(this), configuration);

        rootLayout = new RelativeLayout(this);

        // Game preenche tudo
        RelativeLayout.LayoutParams gameParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        );

        // Banner no TOPO centralizado
        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
        adView.loadAd(new AdRequest.Builder().build());
        adView.setVisibility(View.GONE); // começa escondido

        RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        adParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        rootLayout.addView(gameView, gameParams);
        rootLayout.addView(adView, adParams);

        setContentView(rootLayout);
        applyFullscreen();
    }

    // =========================================================
    // AdBridge
    // =========================================================

    @Override
    public void showBanner() {
        if (adView == null) return;
        runOnUiThread(() -> {
            adView.setVisibility(View.VISIBLE);
            positionAdOverHud();
        });
    }

    @Override
    public void hideBanner() {
        if (adView == null) return;
        runOnUiThread(() -> adView.setVisibility(View.GONE));
    }

    /**
     * Posiciona o AdView no topo da tela, alinhado com o shell do HUD.
     * O banner fica colado no topo da tela em pixels reais.
     */
    private void positionAdOverHud() {
        adView.post(() -> {
            int screenHeight = getWindow().getDecorView().getHeight();
            int screenWidth  = getWindow().getDecorView().getWidth();
            int adHeight     = adView.getHeight();

            // Banner fica no TOPO da tela (y=0 em Android = topo)
            // O shell superior do LibGDX fica na parte de cima da tela
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            params.topMargin = 0;

            adView.setLayoutParams(params);
        });
    }

    // =========================================================
    // Fullscreen
    // =========================================================

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applyFullscreen();
    }

    private void applyFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController insetsController =
                getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(
                    WindowInsets.Type.statusBars()
                        | WindowInsets.Type.navigationBars()
                );
                insetsController.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        super.onDestroy();
    }
}
