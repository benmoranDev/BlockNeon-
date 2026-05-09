package br.com.blockneon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import br.com.blockneon.Main;
import br.com.blockneon.ui.FontManager;

public class SplashScreen implements Screen {

    // =========================================================
    // Viewport
    // =========================================================

    private static final float WORLD_WIDTH  = 580f;
    private static final float WORLD_HEIGHT = 960f;

    // =========================================================
    // Timing
    // =========================================================

    private static final float FADE_IN_DURATION   = 0.8f;
    private static final float HOLD_DURATION      = 1.6f;
    private static final float FADE_OUT_DURATION  = 0.7f;
    private static final float TOTAL_DURATION     = FADE_IN_DURATION + HOLD_DURATION + FADE_OUT_DURATION;

    // =========================================================
    // Core
    // =========================================================

    private final Game game;
    private final OrthographicCamera camera;
    private final ExtendViewport viewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shapeRenderer;

    private float vw = WORLD_WIDTH;
    private float vh = WORLD_HEIGHT;

    // =========================================================
    // Fonts
    // =========================================================

    private final BitmapFont titleFont;
    private final BitmapFont neonFont;
    private final BitmapFont hintFont;

    // =========================================================
    // State
    // =========================================================

    private float time      = 0f;
    private float fadeAlpha = 1f;   // 1 = preto, 0 = visível
    private boolean done    = false;

    // =========================================================
    // Constructor
    // =========================================================

    public SplashScreen(Game game) {
        this.game = game;

        camera   = new OrthographicCamera();
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch    = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        FontManager fonts = ((Main) game).fontManager;
        titleFont = fonts.menuTitleFont;
        neonFont  = fonts.menuNeonFont;
        hintFont  = fonts.menuHintFont;
    }

    // =========================================================
    // Screen lifecycle
    // =========================================================

    @Override
    public void show() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.update();
        vw = viewport.getWorldWidth();
        vh = viewport.getWorldHeight();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.update();
        vw = viewport.getWorldWidth();
        vh = viewport.getWorldHeight();
    }

    // =========================================================
    // Render
    // =========================================================

    @Override
    public void render(float delta) {
        // Limite de FPS
        delta = Math.min(delta, 1f / 30f);
        time += delta;

        // Pula para o menu com toque/tecla
        if (Gdx.input.justTouched()
            || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            goToMenu();
            return;
        }

        updateFade();

        Gdx.gl.glClearColor(0f, 0f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawBackground();
        drawProgressBar();
        drawLogo();
        drawStudio();
        drawFadeOverlay();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        if (done) goToMenu();
    }

    // =========================================================
    // Fade
    // =========================================================

    private void updateFade() {
        if (time < FADE_IN_DURATION) {
            // Fade in — preto → visível
            fadeAlpha = 1f - (time / FADE_IN_DURATION);
        } else if (time < FADE_IN_DURATION + HOLD_DURATION) {
            // Hold — totalmente visível
            fadeAlpha = 0f;
        } else {
            // Fade out — visível → preto → vai para o menu
            float t = (time - FADE_IN_DURATION - HOLD_DURATION) / FADE_OUT_DURATION;
            fadeAlpha = Math.min(t, 1f);
            if (fadeAlpha >= 1f) done = true;
        }
    }

    // =========================================================
    // Drawing
    // =========================================================

    private void drawBackground() {
        // Gradiente simples de faixas verticais
        int bands = 16;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < bands; i++) {
            float t     = (float) i / (bands - 1);
            float curve = (float) Math.pow(1f - t, 2f);
            float bandH = vh / bands + 1f;
            float bandY = i * (vh / bands);
            shapeRenderer.setColor(0.01f, 0.03f + curve * 0.10f, 0.05f + curve * 0.22f, 1f);
            shapeRenderer.rect(0, bandY, vw, bandH);
        }

        // Halo central difuso
        float pulse = (MathUtils.sin(time * 1.8f) + 1f) * 0.5f;
        float hR = vw * 0.70f;
        shapeRenderer.setColor(0.04f, 0.18f, 0.55f, 0.06f + pulse * 0.03f);
        shapeRenderer.ellipse(vw / 2f - hR / 2f, vh / 2f - hR / 2f, hR, hR);

        shapeRenderer.end();
    }

    private void drawLogo() {
        float cx    = vw / 2f;
        float baseY = vh / 2f + 60f;

        // Visibilidade = inverso do fadeAlpha
        float visible = 1f - fadeAlpha;

        // Pulso de glow só durante o HOLD
        float pulse = (time > FADE_IN_DURATION && time < FADE_IN_DURATION + HOLD_DURATION)
            ? (MathUtils.sin(time * 2.8f) + 1f) * 0.5f
            : 0.5f;

        float titleY = baseY;
        float subY   = titleY - 58f;

        // ── BLOCK ────────────────────────────────────────────
        float glowA = (0.08f + pulse * 0.18f) * visible;

        batch.begin();

        titleFont.setColor(0f, 1f, 1f, glowA * 0.55f);
        titleFont.draw(batch, "BLOCK", -8f, titleY + 6f, vw, Align.center, false);
        titleFont.draw(batch, "BLOCK",  8f, titleY + 6f, vw, Align.center, false);
        titleFont.draw(batch, "BLOCK",  0f, titleY + 8f, vw, Align.center, false);

        titleFont.setColor(0.15f, 0.95f, 1f, (0.18f + pulse * 0.28f) * visible);
        titleFont.draw(batch, "BLOCK", 0f, titleY + 3f, vw, Align.center, false);

        titleFont.setColor(0.30f, 1f, 1f, visible);
        titleFont.draw(batch, "BLOCK", 0f, titleY, vw, Align.center, false);

        // ── NEON ─────────────────────────────────────────────
        float pulseN = (MathUtils.sin(time * 2.8f + MathUtils.PI) + 1f) * 0.5f;
        float neonA  = (0.08f + pulseN * 0.18f) * visible;

        neonFont.setColor(1f, 0f, 1f, neonA * 0.55f);
        neonFont.draw(batch, "NEON", -6f, subY + 6f, vw, Align.center, false);
        neonFont.draw(batch, "NEON",  6f, subY + 6f, vw, Align.center, false);

        neonFont.setColor(1f, 0.10f, 1f, (0.18f + pulseN * 0.28f) * visible);
        neonFont.draw(batch, "NEON", 0f, subY + 3f, vw, Align.center, false);

        neonFont.setColor(1f, 0.30f, 1f, visible);
        neonFont.draw(batch, "NEON", 0f, subY, vw, Align.center, false);

        batch.end();

        // ── Separador ciano abaixo do logo ───────────────────
        float lineY = subY - 32f;
        float lineW = 180f * visible;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 1f, 1f, 0.35f * visible);
        shapeRenderer.rect(cx - lineW / 2f, lineY, lineW, 1.5f);
        shapeRenderer.end();
    }

    private void drawProgressBar() {
        float visible  = 1f - fadeAlpha;
        float progress = Math.min(time / TOTAL_DURATION, 1f);

        float barW  = 220f;
        float barH  = 4f;
        float barX  = vw / 2f - barW / 2f;
        float barY  = vh / 2f - 130f;

        float pulse = (MathUtils.sin(time * 3.5f) + 1f) * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Trilho de fundo
        shapeRenderer.setColor(0.10f, 0.18f, 0.28f, 0.55f * visible);
        shapeRenderer.rect(barX, barY, barW, barH);

        // Preenchimento com glow
        float fillW = barW * progress;
        shapeRenderer.setColor(0.04f, 0.55f, 0.90f, (0.55f + pulse * 0.20f) * visible);
        shapeRenderer.rect(barX, barY, fillW, barH);

        // Brilho no topo da barra
        shapeRenderer.setColor(0.50f, 1f, 1f, (0.35f + pulse * 0.20f) * visible);
        shapeRenderer.rect(barX, barY + barH - 1.5f, fillW, 1.5f);

        shapeRenderer.end();

        // Contorno da trilha
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0f, 0.70f, 1f, 0.30f * visible);
        shapeRenderer.rect(barX, barY, barW, barH);
        shapeRenderer.end();

        // Label abaixo da barra
        batch.begin();
        hintFont.setColor(0.55f, 0.85f, 1f, 0.65f * visible);
        hintFont.draw(batch, "LOADING...", 0f, barY - 14f, vw, Align.center, false);
        batch.end();
    }

    private void drawStudio() {
        float visible = 1f - fadeAlpha;
        batch.begin();
        hintFont.setColor(1f, 1f, 1f, 0.28f * visible);
        hintFont.draw(batch, "Tap to skip", 0f, vh / 2f - 175f, vw, Align.center, false);
        batch.end();
    }

    private void drawFadeOverlay() {
        if (fadeAlpha <= 0f) return;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, fadeAlpha);
        shapeRenderer.rect(0f, 0f, vw, vh);
        shapeRenderer.end();
    }

    // =========================================================
    // Transition
    // =========================================================

    private void goToMenu() {
        game.setScreen(new MainMenuScreen(game));
        dispose();
    }

    // =========================================================
    // Lifecycle
    // =========================================================

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }
}
