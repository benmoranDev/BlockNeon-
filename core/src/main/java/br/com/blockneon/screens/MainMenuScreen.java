package br.com.blockneon.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import br.com.blockneon.Main;
import br.com.blockneon.ui.FontManager;



public class MainMenuScreen implements Screen {

    // =========================================================
    // Viewport
    // =========================================================

    private static final float WORLD_WIDTH  = 580f;
    private static final float WORLD_HEIGHT = 960f;

    // =========================================================
    // Layout
    // =========================================================

    private static final float PANEL_WIDTH       = 352f;
    private static final float PANEL_HEIGHT      = 452f;
    private static final float BUTTON_WIDTH      = 232f;
    private static final float BUTTON_HEIGHT     = 54f;
    private static final float BUTTON_SPACING    = 18f;
    private static final float OPTIONS_WIDTH     = 280f;
    private static final float OPTIONS_HEIGHT    = 210f;
    private static final float OPTION_ROW_HEIGHT = 42f;

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
    private final BitmapFont infoFont;
    private final BitmapFont hintFont;

    // =========================================================
    // State
    // =========================================================

    private float time = 0f;

    private boolean transitionStarted = false;
    private boolean fadeInComplete    = false;
    private float   fadeAlpha         = 1f;

    private boolean optionsOpen         = false;
    private boolean backgroundFxEnabled = true;
    private boolean pulseFxEnabled      = true;

    // =========================================================
    // Interaction
    // =========================================================

    private final Rectangle mainPanelBounds    = new Rectangle();
    private final Rectangle newGameButtonBounds = new Rectangle();
    private final Rectangle optionsButtonBounds = new Rectangle();

    private final Rectangle optionsPanelBounds = new Rectangle();
    private final Rectangle backgroundFxBounds = new Rectangle();
    private final Rectangle pulseFxBounds      = new Rectangle();
    private final Rectangle backButtonBounds   = new Rectangle();

    private final Vector3 touchPoint = new Vector3();

    // =========================================================
    // Background FX
    // =========================================================

    private static final int BLOCK_COUNT = 38;

    private final FallingPiece[] backgroundBlocks;

    // =========================================================
    // Constructor
    // =========================================================

    public MainMenuScreen(Game game) {
        this.game = game;

        camera   = new OrthographicCamera();
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch    = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        FontManager fonts = ((Main) game).fontManager;
        titleFont = fonts.menuTitleFont;
        neonFont  = fonts.menuNeonFont;
        infoFont  = fonts.menuButtonFont;
        hintFont  = fonts.menuHintFont;

        backgroundBlocks = new FallingPiece[BLOCK_COUNT];
        for (int i = 0; i < backgroundBlocks.length; i++) {
            backgroundBlocks[i] = new FallingPiece();
            backgroundBlocks[i].reset(true, WORLD_WIDTH, WORLD_HEIGHT);
        }
    }

    // =========================================================
    // Screen lifecycle
    // =========================================================

    @Override
    public void show() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        camera.update();
        syncViewportSize();
        updateLayout();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.update();
        syncViewportSize();
        updateLayout();
    }

    private void syncViewportSize() {
        vw = viewport.getWorldWidth();
        vh = viewport.getWorldHeight();
    }

    // =========================================================
    // Layout
    // =========================================================

    private void updateLayout() {
        float panelX = (vw - PANEL_WIDTH)  / 2f;
        float panelY = (vh - PANEL_HEIGHT) / 2f;   // centralizado verticalmente

        mainPanelBounds.set(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

        float buttonX  = (vw - BUTTON_WIDTH) / 2f;
        float newGameY = panelY + 96f;
        float optionsY = newGameY - BUTTON_HEIGHT - BUTTON_SPACING;

        newGameButtonBounds.set(buttonX, newGameY, BUTTON_WIDTH, BUTTON_HEIGHT);
        optionsButtonBounds.set(buttonX, optionsY, BUTTON_WIDTH, BUTTON_HEIGHT);

        float optionsX      = (vw - OPTIONS_WIDTH)  / 2f;
        float optionsYPanel = (vh - OPTIONS_HEIGHT)  / 2f - 10f;

        optionsPanelBounds.set(optionsX, optionsYPanel, OPTIONS_WIDTH, OPTIONS_HEIGHT);

        float rowX     = optionsX + 22f;
        float rowWidth = OPTIONS_WIDTH - 44f;

        backgroundFxBounds.set(rowX, optionsYPanel + 122f, rowWidth, OPTION_ROW_HEIGHT);
        pulseFxBounds.set(rowX, optionsYPanel + 72f, rowWidth, OPTION_ROW_HEIGHT);
        backButtonBounds.set(optionsX + 70f, optionsYPanel + 18f, OPTIONS_WIDTH - 140f, 38f);
    }

    // =========================================================
    // Render
    // =========================================================

    @Override
    public void render(float delta) {
        time += delta;

        handleInput();
        updateTransition(delta);
        updateComponents(delta);

        Gdx.gl.glClearColor(0.03f, 0.03f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawComponents();
        drawMainPanel();
        drawMenuButtons();

        if (optionsOpen) drawOptionsOverlay();

        drawTexts();
        drawFadeOverlay();

        if (transitionStarted && fadeAlpha >= 1f) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    // =========================================================
    // Input
    // =========================================================

    private void handleInput() {
        if (!fadeInComplete || transitionStarted) return;

        handleKeyboardInput();

        if (!Gdx.input.justTouched()) return;

        viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0f));
        float x = touchPoint.x;
        float y = touchPoint.y;

        if (optionsOpen) handleOptionsTouch(x, y);
        else             handleMenuTouch(x, y);
    }

    private void handleKeyboardInput() {
        if (optionsOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
                optionsOpen = false;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.B)) backgroundFxEnabled = !backgroundFxEnabled;
            if (Gdx.input.isKeyJustPressed(Input.Keys.P)) pulseFxEnabled      = !pulseFxEnabled;
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            transitionStarted = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) optionsOpen = true;
    }

    private void handleMenuTouch(float x, float y) {
        if (newGameButtonBounds.contains(x, y)) { transitionStarted = true; return; }
        if (optionsButtonBounds.contains(x, y)) { optionsOpen = true; }
    }

    private void handleOptionsTouch(float x, float y) {
        if (backgroundFxBounds.contains(x, y)) { backgroundFxEnabled = !backgroundFxEnabled; return; }
        if (pulseFxBounds.contains(x, y))      { pulseFxEnabled      = !pulseFxEnabled;      return; }
        if (backButtonBounds.contains(x, y))   { optionsOpen = false; }
    }

    // =========================================================
    // Update
    // =========================================================

    private void updateTransition(float delta) {
        if (!fadeInComplete) {
            fadeAlpha -= delta * 1.8f;
            if (fadeAlpha <= 0f) { fadeAlpha = 0f; fadeInComplete = true; }
            return;
        }
        if (transitionStarted) {
            fadeAlpha += delta * 2.3f;
            if (fadeAlpha > 1f) fadeAlpha = 1f;
        }
    }

    private void updateComponents(float delta) {
        if (!backgroundFxEnabled) return;
        for (FallingPiece piece : backgroundBlocks) {
            piece.y -= piece.speed * delta;
            if (piece.y < -80f) piece.reset(false, vw, vh);
        }
    }

    // =========================================================
    // Drawing
    // =========================================================

    private void drawComponents() {
        if (!backgroundFxEnabled) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (FallingPiece piece : backgroundBlocks) {
            if (!piece.isOutline()) piece.draw(shapeRenderer);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (FallingPiece piece : backgroundBlocks) {
            if (piece.isOutline()) piece.draw(shapeRenderer);
        }
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawMainPanel() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0f, 1f, 1f, 0.04f);
        shapeRenderer.rect(mainPanelBounds.x - 6f, mainPanelBounds.y - 6f,
            mainPanelBounds.width + 12f, mainPanelBounds.height + 12f);

        shapeRenderer.setColor(0.09f, 0.10f, 0.15f, 0.94f);
        shapeRenderer.rect(mainPanelBounds.x, mainPanelBounds.y,
            mainPanelBounds.width, mainPanelBounds.height);

        shapeRenderer.setColor(0.13f, 0.14f, 0.20f, 0.22f);
        shapeRenderer.rect(mainPanelBounds.x + 12f, mainPanelBounds.y + 12f,
            mainPanelBounds.width - 24f, mainPanelBounds.height - 24f);

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(0f, 1f, 1f, 0.25f);
        shapeRenderer.rect(mainPanelBounds.x, mainPanelBounds.y,
            mainPanelBounds.width, mainPanelBounds.height);

        shapeRenderer.setColor(1f, 0f, 1f, 0.14f);
        shapeRenderer.rect(mainPanelBounds.x + 6f, mainPanelBounds.y + 6f,
            mainPanelBounds.width - 12f, mainPanelBounds.height - 12f);

        shapeRenderer.setColor(0f, 1f, 1f, 0.10f);
        shapeRenderer.line(
            mainPanelBounds.x + 28f, mainPanelBounds.y + 156f,
            mainPanelBounds.x + mainPanelBounds.width - 28f, mainPanelBounds.y + 156f
        );

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawMenuButtons() {
        drawButton(newGameButtonBounds, true,  !optionsOpen);
        drawButton(optionsButtonBounds, false, !optionsOpen);
    }

    private void drawButton(Rectangle bounds, boolean primary, boolean active) {
        float pulse     = pulseFxEnabled ? (MathUtils.sin(time * 5f) + 1f) * 0.5f : 0.4f;
        float glowAlpha = active ? (0.05f + pulse * 0.05f) : 0.025f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (primary) {
            shapeRenderer.setColor(0f, 1f, 1f, glowAlpha);
            shapeRenderer.rect(bounds.x - 4f, bounds.y - 4f, bounds.width + 8f, bounds.height + 8f);
            shapeRenderer.setColor(0.08f, 0.20f, 0.24f, 0.90f);
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            shapeRenderer.setColor(0.18f, 0.95f, 1f, 0.10f);
            shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f, bounds.width - 6f, bounds.height - 6f);
        } else {
            shapeRenderer.setColor(1f, 0f, 1f, glowAlpha * 0.8f);
            shapeRenderer.rect(bounds.x - 4f, bounds.y - 4f, bounds.width + 8f, bounds.height + 8f);
            shapeRenderer.setColor(0.14f, 0.10f, 0.18f, 0.90f);
            shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            shapeRenderer.setColor(1f, 0.25f, 1f, 0.08f);
            shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f, bounds.width - 6f, bounds.height - 6f);
        }

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(primary ? new Color(0f, 1f, 1f, 0.45f) : new Color(1f, 0.25f, 1f, 0.30f));
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawOptionsOverlay() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.48f);
        shapeRenderer.rect(0f, 0f, vw, vh);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0f, 1f, 1f, 0.05f);
        shapeRenderer.rect(optionsPanelBounds.x - 6f, optionsPanelBounds.y - 6f,
            optionsPanelBounds.width + 12f, optionsPanelBounds.height + 12f);

        shapeRenderer.setColor(0.08f, 0.09f, 0.14f, 0.96f);
        shapeRenderer.rect(optionsPanelBounds.x, optionsPanelBounds.y,
            optionsPanelBounds.width, optionsPanelBounds.height);

        shapeRenderer.setColor(0.13f, 0.14f, 0.20f, 0.18f);
        shapeRenderer.rect(optionsPanelBounds.x + 8f, optionsPanelBounds.y + 8f,
            optionsPanelBounds.width - 16f, optionsPanelBounds.height - 16f);

        drawOptionRow(backgroundFxBounds, backgroundFxEnabled, true);
        drawOptionRow(pulseFxBounds,      pulseFxEnabled,      false);

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(0f, 1f, 1f, 0.30f);
        shapeRenderer.rect(optionsPanelBounds.x, optionsPanelBounds.y,
            optionsPanelBounds.width, optionsPanelBounds.height);

        shapeRenderer.setColor(1f, 0f, 1f, 0.14f);
        shapeRenderer.rect(optionsPanelBounds.x + 4f, optionsPanelBounds.y + 4f,
            optionsPanelBounds.width - 8f, optionsPanelBounds.height - 8f);

        shapeRenderer.setColor(0.75f, 0.95f, 1f, 0.35f);
        shapeRenderer.rect(backButtonBounds.x, backButtonBounds.y,
            backButtonBounds.width, backButtonBounds.height);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawOptionRow(Rectangle bounds, boolean enabled, boolean cyanAccent) {
        shapeRenderer.setColor(cyanAccent
            ? new Color(0.10f, 0.18f, 0.22f, 0.88f)
            : new Color(0.16f, 0.10f, 0.20f, 0.88f));
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (enabled) {
            shapeRenderer.setColor(cyanAccent
                ? new Color(0f, 1f, 1f, 0.09f)
                : new Color(1f, 0.25f, 1f, 0.09f));
            shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f, bounds.width - 6f, bounds.height - 6f);
        }
    }

    // =========================================================
    // Text
    // =========================================================

    private void drawTexts() {
        float pulse = pulseFxEnabled ? (MathUtils.sin(time * 5f) + 1f) * 0.5f : 0.4f;

        batch.begin();
        drawMenuTitle();
        drawMenuButtonLabels();
        drawFooterHints(pulse);
        if (optionsOpen) drawOptionsTexts();
        batch.end();
    }

    private void drawMenuTitle() {
        float centerY = mainPanelBounds.y + PANEL_HEIGHT / 2f;
        float titleY  = centerY + 110f;
        float subY    = titleY  - 58f;

        float pulse    = pulseFxEnabled ? (MathUtils.sin(time * 2.8f) + 1f) * 0.5f : 0.5f;
        float glowA1   = 0.10f + pulse * 0.20f;
        float glowA2   = 0.18f + pulse * 0.28f;

        // ── BLOCK — ciano neon ────────────────────────────────
        titleFont.setColor(new Color(0f, 1f, 1f, glowA1 * 0.6f));
        titleFont.draw(batch, "BLOCK", 0f,  titleY + 8f, vw, Align.center, false);
        titleFont.draw(batch, "BLOCK", -8f, titleY,      vw, Align.center, false);
        titleFont.draw(batch, "BLOCK", 8f,  titleY,      vw, Align.center, false);

        titleFont.setColor(new Color(0.15f, 0.95f, 1f, glowA2));
        titleFont.draw(batch, "BLOCK", 0f, titleY + 3f, vw, Align.center, false);

        titleFont.setColor(new Color(0f, 0.60f, 0.80f, 0.06f + pulse * 0.08f));
        titleFont.draw(batch, "BLOCK", 3f, titleY - 3f, vw, Align.center, false);

        titleFont.setColor(new Color(0.30f, 1f, 1f, 1f));
        titleFont.draw(batch, "BLOCK", 0f, titleY, vw, Align.center, false);

        // ── NEON — magenta neon (fase oposta) ────────────────
        float pulseNeon = pulseFxEnabled
            ? (MathUtils.sin(time * 2.8f + MathUtils.PI) + 1f) * 0.5f
            : 0.5f;
        float neonA1 = 0.10f + pulseNeon * 0.20f;
        float neonA2 = 0.18f + pulseNeon * 0.28f;

        neonFont.setColor(new Color(1f, 0f, 1f, neonA1 * 0.6f));
        neonFont.draw(batch, "NEON", 0f,  subY + 7f, vw, Align.center, false);
        neonFont.draw(batch, "NEON", -6f, subY,      vw, Align.center, false);
        neonFont.draw(batch, "NEON", 6f,  subY,      vw, Align.center, false);

        neonFont.setColor(new Color(1f, 0.10f, 1f, neonA2));
        neonFont.draw(batch, "NEON", 0f, subY + 3f, vw, Align.center, false);

        neonFont.setColor(new Color(1f, 0.30f, 1f, 1f));
        neonFont.draw(batch, "NEON", 0f, subY, vw, Align.center, false);
    }

    private void drawMenuButtonLabels() {
        drawCenteredButtonLabel("NEW GAME", newGameButtonBounds, new Color(0.82f, 1f,    1f, 1f));
        drawCenteredButtonLabel("OPTIONS",  optionsButtonBounds, new Color(1f,    0.84f, 1f, 1f));
    }

    private void drawFooterHints(float pulse) {
        float hintY1 = mainPanelBounds.y - 30f;
        float hintY2 = hintY1 - 22f;
        float hintY3 = hintY2 - 22f;

        if (!optionsOpen) {
            hintFont.setColor(new Color(1f, 1f, 1f, 0.32f + pulse * 0.35f));
            hintFont.draw(batch, "Tap a button or press ENTER", 0f, hintY1, vw, Align.center, false);

            hintFont.setColor(new Color(0.70f, 0.86f, 1f, 0.48f));
            hintFont.draw(batch, "O = Options", 0f, hintY2, vw, Align.center, false);

            hintFont.setColor(new Color(0.75f, 0.95f, 1f, 0.54f));
            hintFont.draw(batch, "Swipe  \u2022  Tap  \u2022  Fling", 0f, hintY3, vw, Align.center, false);
        } else {
            hintFont.setColor(new Color(1f, 1f, 1f, 0.42f + pulse * 0.25f));
            hintFont.draw(batch, "Tap an option to toggle it", 0f, hintY1, vw, Align.center, false);

            hintFont.setColor(new Color(0.75f, 0.95f, 1f, 0.58f));
            hintFont.draw(batch, "ESC = Back", 0f, hintY2, vw, Align.center, false);
        }
    }

    private void drawOptionsTexts() {
        infoFont.setColor(new Color(0.92f, 0.98f, 1f, 1f));
        infoFont.draw(batch, "OPTIONS",
            optionsPanelBounds.x, optionsPanelBounds.y + 184f,
            optionsPanelBounds.width, Align.center, false);

        hintFont.setColor(new Color(0.75f, 0.88f, 1f, 0.62f));
        hintFont.draw(batch, "Visual effects",
            optionsPanelBounds.x, optionsPanelBounds.y + 158f,
            optionsPanelBounds.width, Align.center, false);

        infoFont.setColor(Color.WHITE);
        infoFont.draw(batch, "Background FX",
            backgroundFxBounds.x + 14f, backgroundFxBounds.y + 28f);
        infoFont.setColor(backgroundFxEnabled
            ? new Color(0.70f, 1f, 1f, 1f)
            : new Color(1f, 1f, 1f, 0.40f));
        infoFont.draw(batch, backgroundFxEnabled ? "ON" : "OFF",
            backgroundFxBounds.x, backgroundFxBounds.y + 28f,
            backgroundFxBounds.width - 14f, Align.right, false);

        infoFont.setColor(Color.WHITE);
        infoFont.draw(batch, "Pulse Glow",
            pulseFxBounds.x + 14f, pulseFxBounds.y + 28f);
        infoFont.setColor(pulseFxEnabled
            ? new Color(1f, 0.78f, 1f, 1f)
            : new Color(1f, 1f, 1f, 0.40f));
        infoFont.draw(batch, pulseFxEnabled ? "ON" : "OFF",
            pulseFxBounds.x, pulseFxBounds.y + 28f,
            pulseFxBounds.width - 14f, Align.right, false);

        hintFont.setColor(new Color(0.88f, 0.96f, 1f, 0.95f));
        hintFont.draw(batch, "BACK",
            backButtonBounds.x, backButtonBounds.y + 25f,
            backButtonBounds.width, Align.center, false);
    }

    private void drawCenteredButtonLabel(String text, Rectangle bounds, Color color) {
        infoFont.setColor(color);
        infoFont.draw(batch, text,
            bounds.x, bounds.y + bounds.height / 2f + 7f,
            bounds.width, Align.center, false);
    }

    private void drawFadeOverlay() {
        if (fadeAlpha <= 0f) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, fadeAlpha);
        shapeRenderer.rect(0f, 0f, vw, vh);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
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

    // =========================================================
    // FallingPiece
    // =========================================================

    private static class FallingPiece {

        static final int SQUARE  = 0;
        static final int DIAMOND = 1;
        static final int TETRO_L = 2;
        static final int TETRO_T = 3;
        static final int TETRO_S = 4;
        static final int TETRO_I = 5;
        static final int TETRO_O = 6;
        static final int CROSS   = 7;

        static final int NEON    = 0;
        static final int CLASSIC = 1;
        static final int OUTLINE = 2;
        static final int GHOST   = 3;

        float x, y, speed, cellSize, alpha;
        Color color;

        private int type;
        private int style;

        void reset(boolean firstTime, float worldWidth, float worldHeight) {
            x        = MathUtils.random(40f, worldWidth - 40f);
            y        = firstTime
                ? MathUtils.random(0f, worldHeight)
                : MathUtils.random(worldHeight + 20f, worldHeight + 160f);
            speed    = MathUtils.random(60f, 220f);
            type     = MathUtils.random(0, 7);
            style    = MathUtils.random(0, 3);
            cellSize = (type >= TETRO_L)
                ? MathUtils.random(7f, 14f)
                : MathUtils.random(10f, 22f);

            switch (style) {
                case NEON:    alpha = MathUtils.random(0.55f, 0.85f); break;
                case CLASSIC: alpha = MathUtils.random(0.40f, 0.65f); break;
                case OUTLINE: alpha = MathUtils.random(0.30f, 0.55f); break;
                default:      alpha = MathUtils.random(0.12f, 0.28f); break;
            }

            color = randomNeonColor();
        }

        void draw(ShapeRenderer sr) {
            switch (type) {
                case SQUARE:  drawSquare(sr);  break;
                case DIAMOND: drawDiamond(sr); break;
                case TETRO_L: drawTetroL(sr);  break;
                case TETRO_T: drawTetroT(sr);  break;
                case TETRO_S: drawTetroS(sr);  break;
                case TETRO_I: drawTetroI(sr);  break;
                case TETRO_O: drawTetroO(sr);  break;
                case CROSS:   drawCross(sr);   break;
            }
        }

        boolean isOutline() { return style == OUTLINE; }

        private void drawSquare(ShapeRenderer sr) {
            cell(sr, x, y, cellSize, cellSize);
        }

        private void drawDiamond(ShapeRenderer sr) {
            float cx = x + cellSize;
            float cy = y + cellSize;
            float r  = cellSize;

            if (style == NEON) {
                sr.setColor(color.r, color.g, color.b, alpha * 0.10f);
                sr.triangle(cx, cy + r + 4f, cx - r - 4f, cy, cx + r + 4f, cy);
                sr.triangle(cx, cy - r - 4f, cx - r - 4f, cy, cx + r + 4f, cy);
            }
            sr.setColor(color.r, color.g, color.b, alpha * 0.70f);
            sr.triangle(cx, cy + r, cx - r, cy, cx + r, cy);
            sr.triangle(cx, cy - r, cx - r, cy, cx + r, cy);
        }

        private void drawTetroL(ShapeRenderer sr) {
            float s = cellSize;
            cell(sr, x,     y,         s, s);
            cell(sr, x,     y + s,     s, s);
            cell(sr, x,     y + s * 2, s, s);
            cell(sr, x + s, y,         s, s);
        }

        private void drawTetroT(ShapeRenderer sr) {
            float s = cellSize;
            cell(sr, x,         y,     s, s);
            cell(sr, x + s,     y,     s, s);
            cell(sr, x + s * 2, y,     s, s);
            cell(sr, x + s,     y + s, s, s);
        }

        private void drawTetroS(ShapeRenderer sr) {
            float s = cellSize;
            cell(sr, x + s,     y,     s, s);
            cell(sr, x + s * 2, y,     s, s);
            cell(sr, x,         y + s, s, s);
            cell(sr, x + s,     y + s, s, s);
        }

        private void drawTetroI(ShapeRenderer sr) {
            for (int i = 0; i < 4; i++) {
                cell(sr, x + cellSize * i, y, cellSize, cellSize);
            }
        }

        private void drawTetroO(ShapeRenderer sr) {
            float s = cellSize;
            cell(sr, x,     y,     s, s);
            cell(sr, x + s, y,     s, s);
            cell(sr, x,     y + s, s, s);
            cell(sr, x + s, y + s, s, s);
        }

        private void drawCross(ShapeRenderer sr) {
            float s = cellSize;
            cell(sr, x + s,     y,         s, s);
            cell(sr, x,         y + s,     s, s);
            cell(sr, x + s,     y + s,     s, s);
            cell(sr, x + s * 2, y + s,     s, s);
            cell(sr, x + s,     y + s * 2, s, s);
        }

        private void cell(ShapeRenderer sr, float cx, float cy, float w, float h) {
            float r = color.r, g = color.g, b = color.b;

            switch (style) {
                case NEON:
                    sr.setColor(r, g, b, alpha * 0.08f);
                    sr.rect(cx - 3f, cy - 3f, w + 6f, h + 6f);
                    sr.setColor(r, g, b, alpha * 0.20f);
                    sr.rect(cx - 1f, cy - 1f, w + 2f, h + 2f);
                    sr.setColor(r, g, b, alpha * 0.55f);
                    sr.rect(cx + 2f, cy + 2f, w - 4f, h - 4f);
                    sr.setColor(r + (1f - r) * 0.6f, g + (1f - g) * 0.6f, b + (1f - b) * 0.6f, alpha);
                    sr.rect(cx + 4f, cy + 4f, w - 8f, h - 8f);
                    break;

                case CLASSIC:
                    sr.setColor(r, g, b, alpha);
                    sr.rect(cx, cy, w, h);
                    sr.setColor(1f, 1f, 1f, alpha * 0.25f);
                    sr.rect(cx, cy + h - 3f, w, 3f);
                    sr.rect(cx, cy, 3f, h);
                    break;

                case OUTLINE:
                    sr.setColor(r, g, b, alpha);
                    sr.rect(cx, cy, w, h);
                    break;

                case GHOST:
                    sr.setColor(r, g, b, alpha);
                    sr.rect(cx, cy, w, h);
                    break;
            }
        }

        private static Color randomNeonColor() {
            switch (MathUtils.random(0, 9)) {
                case 0: return new Color(0f,   1f,    1f,   1f);
                case 1: return new Color(1f,   0.2f,  1f,   1f);
                case 2: return new Color(0.2f, 1f,    0.3f, 1f);
                case 3: return new Color(1f,   0.85f, 0f,   1f);
                case 4: return new Color(1f,   0.35f, 0.1f, 1f);
                case 5: return new Color(0.4f, 0.4f,  1f,   1f);
                case 6: return new Color(1f,   0.1f,  0.4f, 1f);
                case 7: return new Color(0.9f, 1f,    0.4f, 1f);
                case 8: return new Color(0.6f, 0.2f,  1f,   1f);
                default:return new Color(1f,   1f,    1f,   1f);
            }
        }
    }
}
