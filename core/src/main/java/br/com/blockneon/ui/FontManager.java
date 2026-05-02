package br.com.blockneon.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;



public class FontManager {

    private final int   screenWidth;
    private final int   screenHeight;

    // ── HUD (GameScreen) ──────────────────────────────────────
    public BitmapFont hudLabelFont;
    public BitmapFont hudValueFont;

    // ── Menu principal ────────────────────────────────────────
    public BitmapFont menuTitleFont;
    public BitmapFont menuNeonFont;
    public BitmapFont menuButtonFont;
    public BitmapFont menuHintFont;

    // ── Game Over ─────────────────────────────────────────────
    public BitmapFont gameOverTitleFont;
    public BitmapFont gameOverScoreFont;
    public BitmapFont gameOverLabelFont;
    public BitmapFont gameOverHintFont;

    // =========================================================
    // Constructor
    // =========================================================

    public FontManager(int screenWidth, int screenHeight) {
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;
        createFonts();
    }

    // =========================================================
    // Font creation
    // =========================================================

    private void createFonts() {

        // ── HUD ───────────────────────────────────────────────
        hudLabelFont = plain(1f, Color.WHITE);
        hudValueFont = plain(2f, Color.CYAN);

        // ── Menu principal ────────────────────────────────────
        menuTitleFont  = plain(3f, new Color(0.25f, 1f, 1f, 1f));
        menuNeonFont   = plain(2f, new Color(1f, 0.30f, 1f, 1f));
        menuButtonFont = plain(1.5f, Color.WHITE);
        menuHintFont   = plain(1f,   new Color(0.75f, 0.90f, 1f, 0.70f));

        // ── Game Over ─────────────────────────────────────────
        gameOverTitleFont = plain(3f, new Color(1f, 0.88f, 0.88f, 1f));
        gameOverScoreFont = plain(3f, new Color(0.82f, 1f, 1f, 1f));
        gameOverLabelFont = plain(1.5f, Color.WHITE);
        gameOverHintFont  = plain(1f,   new Color(0.75f, 0.90f, 1f, 0.70f));
    }

    // =========================================================
    // Font helpers
    // =========================================================

    /**
     * Creates a scaled BitmapFont using LibGDX's built-in font.
     * Cria uma BitmapFont escalada usando a fonte built-in do LibGDX.
     */
    private BitmapFont plain(float scale, Color color) {
        BitmapFont font = new BitmapFont();
        font.getData().setScale(scale);
        font.setColor(color);
        font.getRegion().getTexture().setFilter(
            Texture.TextureFilter.Linear,
            Texture.TextureFilter.Linear
        );
        return font;
    }

    // =========================================================
    // Dispose
    // =========================================================

    public void dispose() {
        if (hudLabelFont      != null) hudLabelFont.dispose();
        if (hudValueFont      != null) hudValueFont.dispose();
        if (menuTitleFont     != null) menuTitleFont.dispose();
        if (menuNeonFont      != null) menuNeonFont.dispose();
        if (menuButtonFont    != null) menuButtonFont.dispose();
        if (menuHintFont      != null) menuHintFont.dispose();
        if (gameOverTitleFont != null) gameOverTitleFont.dispose();
        if (gameOverScoreFont != null) gameOverScoreFont.dispose();
        if (gameOverLabelFont != null) gameOverLabelFont.dispose();
        if (gameOverHintFont  != null) gameOverHintFont.dispose();
    }
}
