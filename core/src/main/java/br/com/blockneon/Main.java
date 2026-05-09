package br.com.blockneon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import br.com.blockneon.ad.AdBridge;
import br.com.blockneon.model.ScoreManager;
import br.com.blockneon.screens.MainMenuScreen;
import br.com.blockneon.screens.SplashScreen;
import br.com.blockneon.ui.FontManager;

public class Main extends Game {

    public FontManager  fontManager;
    public ScoreManager scoreManager;

    public final AdBridge adBridge;

    public Main(AdBridge adBridge) {
        this.adBridge = adBridge;
    }

    @Override
    public void create() {

        // ── Resolução real do dispositivo ────────────────────
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        // ── Sistemas globais ─────────────────────────────────
        fontManager  = new FontManager(w, h);
        scoreManager = new ScoreManager();

        // ── Target de FPS ────────────────────────────────────
        Gdx.graphics.setForegroundFPS(60);

        // ── Tela inicial ─────────────────────────────────────
        setScreen(new SplashScreen(this));
    }

    @Override
    public void dispose() {
        fontManager.dispose();
    }
}

