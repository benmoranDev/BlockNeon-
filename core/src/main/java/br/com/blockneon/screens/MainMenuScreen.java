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

    private final Rectangle mainPanelBounds     = new Rectangle();
    private final Rectangle newGameButtonBounds  = new Rectangle();
    private final Rectangle optionsButtonBounds  = new Rectangle();

    private final Rectangle optionsPanelBounds  = new Rectangle();
    private final Rectangle backgroundFxBounds  = new Rectangle();
    private final Rectangle pulseFxBounds       = new Rectangle();
    private final Rectangle backButtonBounds    = new Rectangle();

    private final Vector3 touchPoint = new Vector3();

    // =========================================================
    // Background FX
    // =========================================================

    private static final int BLOCK_COUNT = 38;
    private final FallingPiece[] backgroundBlocks;



    // Constantes de escala — ajuste conforme a sua resolução
    private static final float TITLE_SCALE  = 2.2f;  // aumente para deixar a fonte maior
    private static final float NEON_SCALE   = 1.8f;

    // =========================================================
    // Constructor
    // =========================================================

    public MainMenuScreen(Game game) {
        this.game = game;

        camera        = new OrthographicCamera();
        viewport      = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch         = new SpriteBatch();
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
        float panelY = (vh - PANEL_HEIGHT) / 2f;

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
        delta = Math.min(delta, 1f / 30f);
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

    // =========================================================
    // Painel principal com cantos redondos + brilho intenso
    // =========================================================

    private void drawMainPanel() {
        float pulse      = pulseFxEnabled ? (MathUtils.sin(time * 2.4f) + 1f) * 0.5f : 0.5f;
        float pulseSlow  = pulseFxEnabled ? (MathUtils.sin(time * 0.9f) + 1f) * 0.5f : 0.5f;
        float pulseFast  = pulseFxEnabled ? (MathUtils.sin(time * 4.2f) + 1f) * 0.5f : 0.5f;

        float RADIUS = 14f;
        int   SEGS   = 7;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // ── Camada 1: halo ambiente largo (10 layers) ────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        int haloLayers = 10;
        for (int i = haloLayers; i >= 1; i--) {
            float t      = (float) i / haloLayers;
            float expand = i * 14f;
            float alpha  = (0.050f - t * 0.043f) * (0.75f + pulseSlow * 0.25f);

            // ciano -> azul pulsante
            float hue = 0.50f + MathUtils.sin(time * 0.4f + t) * 0.08f;
            float rC  = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC  = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC  = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);
            shapeRenderer.setColor(rC, gC, bC, alpha);
            shapeRenderer.rect(
                mainPanelBounds.x - expand,
                mainPanelBounds.y - expand,
                mainPanelBounds.width  + expand * 2f,
                mainPanelBounds.height + expand * 2f);
        }

        // ── Camada 2: corona brilhante na borda ───────────────
        float[] coronaExpand = { 8f, 5f, 3f, 1.5f };
        float[] coronaAlpha  = { 0.05f, 0.09f, 0.14f, 0.20f };
        for (int i = 0; i < 4; i++) {
            float ex = coronaExpand[i];
            float ca = coronaAlpha[i] * (0.8f + pulse * 0.2f);
            shapeRenderer.setColor(0f, 1f, 1f, ca);
            shapeRenderer.rect(
                mainPanelBounds.x - ex, mainPanelBounds.y - ex,
                mainPanelBounds.width  + ex * 2f,
                mainPanelBounds.height + ex * 2f);
        }

        // ── Corpo do painel ───────────────────────────────────
        shapeRenderer.setColor(0.09f, 0.10f, 0.15f, 0.94f);
        shapeRenderer.rect(mainPanelBounds.x, mainPanelBounds.y,
            mainPanelBounds.width, mainPanelBounds.height);

        shapeRenderer.setColor(0.13f, 0.14f, 0.20f, 0.22f);
        shapeRenderer.rect(mainPanelBounds.x + 12f, mainPanelBounds.y + 12f,
            mainPanelBounds.width - 24f, mainPanelBounds.height - 24f);

        // ── Reflexo de luz no topo (flare horizontal) ─────────
        float flareW = mainPanelBounds.width * (0.55f + pulseFast * 0.15f);
        float flareX = mainPanelBounds.x + (mainPanelBounds.width - flareW) * 0.5f;
        float flareY = mainPanelBounds.y + mainPanelBounds.height - 6f;

        shapeRenderer.setColor(1f, 1f, 1f, 0.035f + pulseFast * 0.025f);
        shapeRenderer.rect(flareX - 20f, flareY - 1f, flareW + 40f, 8f);
        shapeRenderer.setColor(0.75f, 0.95f, 1f, 0.07f + pulseFast * 0.04f);
        shapeRenderer.rect(flareX, flareY, flareW, 4f);
        shapeRenderer.setColor(1f, 1f, 1f, 0.18f + pulseFast * 0.10f);
        shapeRenderer.rect(flareX + 16f, flareY + 2f, flareW - 32f, 1.5f);

        // ── Reflexo diagonal (vidro) ───────────────────────────
        float refW = mainPanelBounds.width  * 0.38f;
        float refH = mainPanelBounds.height * 0.52f;
        shapeRenderer.setColor(1f, 1f, 1f, 0.022f + pulseSlow * 0.010f);
        shapeRenderer.triangle(
            mainPanelBounds.x + 8f,        mainPanelBounds.y + mainPanelBounds.height - 8f,
            mainPanelBounds.x + 8f + refW, mainPanelBounds.y + mainPanelBounds.height - 8f,
            mainPanelBounds.x + 8f,        mainPanelBounds.y + mainPanelBounds.height - 8f - refH);

        shapeRenderer.end();

        // ── Contornos com cantos redondos ─────────────────────
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Glow externo — 4 passes
        for (int pass = 0; pass < 4; pass++) {
            float expand = (pass + 1) * 4.5f;
            float phase  = time * 0.55f + pass * MathUtils.PI * 0.5f;
            float raw    = ((phase / MathUtils.PI2) % 1f + 1f) % 1f;
            float hue    = 0.38f + raw * 0.30f;
            float rC = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);
            float a  = Math.max((0.10f - pass * 0.02f) * (0.75f + pulse * 0.25f), 0f);
            shapeRenderer.setColor(rC, gC, bC, a);
            roundedRect(
                mainPanelBounds.x - expand,
                mainPanelBounds.y - expand,
                mainPanelBounds.width  + expand * 2f,
                mainPanelBounds.height + expand * 2f,
                RADIUS + expand, SEGS);
        }

        // Borda principal arco-íris
        drawRainbowBorderEdges(mainPanelBounds, RADIUS, SEGS, 0.55f + pulse * 0.22f);

        // Linha interna branca
        shapeRenderer.setColor(1f, 1f, 1f, 0.14f + pulse * 0.08f);
        roundedRect(mainPanelBounds.x + 1.5f, mainPanelBounds.y + 1.5f,
            mainPanelBounds.width - 3f, mainPanelBounds.height - 3f,
            RADIUS - 1.5f, SEGS);

        // Linha divisória interna (separador título/botões)
        shapeRenderer.setColor(0f, 1f, 1f, 0.12f);
        shapeRenderer.line(
            mainPanelBounds.x + 28f,                              mainPanelBounds.y + 156f,
            mainPanelBounds.x + mainPanelBounds.width - 28f,     mainPanelBounds.y + 156f);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // =========================================================
    // Botões com cantos redondos + brilho
    // =========================================================

    private void drawMenuButtons() {
        drawButton(newGameButtonBounds, true,  !optionsOpen);
        drawButton(optionsButtonBounds, false, !optionsOpen);
    }

    private void drawButton(Rectangle bounds, boolean primary, boolean active) {
        float pulse     = pulseFxEnabled ? (MathUtils.sin(time * 5f) + 1f) * 0.5f : 0.4f;
        float pulseFast = pulseFxEnabled ? (MathUtils.sin(time * 4.2f) + 1f) * 0.5f : 0.4f;
        float glowAlpha = active ? (0.05f + pulse * 0.05f) : 0.025f;

        float RADIUS = 8f;
        int   SEGS   = 5;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Halo externo em camadas
        float baseR = primary ? 0f    : 1f;
        float baseG = primary ? 1f    : 0.25f;
        float baseB = primary ? 1f    : 1f;

        for (int i = 4; i >= 1; i--) {
            float ex  = i * 5f;
            float a   = (0.06f - i * 0.012f) * (0.7f + pulse * 0.3f);
            if (!active) a *= 0.4f;
            shapeRenderer.setColor(baseR, baseG, baseB, Math.max(a, 0f));
            shapeRenderer.rect(bounds.x - ex, bounds.y - ex,
                bounds.width + ex * 2f, bounds.height + ex * 2f);
        }

        // Glow imediato (corona)
        shapeRenderer.setColor(baseR, baseG, baseB, glowAlpha * 1.2f);
        shapeRenderer.rect(bounds.x - 3f, bounds.y - 3f, bounds.width + 6f, bounds.height + 6f);

        // Corpo
        if (primary) {
            shapeRenderer.setColor(0.08f, 0.20f, 0.24f, 0.90f);
        } else {
            shapeRenderer.setColor(0.14f, 0.10f, 0.18f, 0.90f);
        }
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        // Tint interior
        shapeRenderer.setColor(baseR, baseG, baseB, active ? 0.08f : 0.03f);
        shapeRenderer.rect(bounds.x + 3f, bounds.y + 3f, bounds.width - 6f, bounds.height - 6f);

        // Reflexo de luz no topo do botão
        float fw = bounds.width * 0.50f;
        shapeRenderer.setColor(1f, 1f, 1f, 0.06f + pulseFast * 0.04f);
        shapeRenderer.rect(bounds.x + bounds.width * 0.25f, bounds.y + bounds.height - 5f, fw, 4f);
        shapeRenderer.setColor(1f, 1f, 1f, 0.13f + pulseFast * 0.06f);
        shapeRenderer.rect(bounds.x + bounds.width * 0.25f + 6f, bounds.y + bounds.height - 4f, fw - 12f, 1.5f);

        shapeRenderer.end();

        // Contorno arco-íris com cantos redondos + glow externo
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Glow externo 2 passes
        for (int pass = 0; pass < 2; pass++) {
            float ex  = (pass + 1) * 3.5f;
            float a   = (0.08f - pass * 0.03f) * (active ? (0.7f + pulse * 0.3f) : 0.3f);
            shapeRenderer.setColor(baseR, baseG, baseB, a);
            roundedRect(bounds.x - ex, bounds.y - ex,
                bounds.width + ex * 2f, bounds.height + ex * 2f,
                RADIUS + ex, SEGS);
        }

        // Borda principal
        drawRainbowBorderEdges(bounds, RADIUS, SEGS,
            active ? (0.52f + pulse * 0.26f) : 0.20f);

        // Linha interna branca
        shapeRenderer.setColor(1f, 1f, 1f, active ? (0.12f + pulse * 0.08f) : 0.05f);
        roundedRect(bounds.x + 1.5f, bounds.y + 1.5f,
            bounds.width - 3f, bounds.height - 3f,
            RADIUS - 1.5f, SEGS);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // =========================================================
    // Options overlay com cantos redondos + brilho
    // =========================================================

    private void drawOptionsOverlay() {
        float pulse     = pulseFxEnabled ? (MathUtils.sin(time * 2.4f) + 1f) * 0.5f : 0.5f;
        float pulseSlow = pulseFxEnabled ? (MathUtils.sin(time * 0.9f) + 1f) * 0.5f : 0.5f;
        float pulseFast = pulseFxEnabled ? (MathUtils.sin(time * 4.2f) + 1f) * 0.5f : 0.5f;

        float RADIUS = 12f;
        int   SEGS   = 6;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Fundo escuro semi-transparente
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.48f);
        shapeRenderer.rect(0f, 0f, vw, vh);

        // Halo ambiente
        for (int i = 8; i >= 1; i--) {
            float t      = (float) i / 8;
            float expand = i * 12f;
            float alpha  = (0.045f - t * 0.038f) * (0.75f + pulseSlow * 0.25f);
            float hue    = 0.50f + MathUtils.sin(time * 0.4f + t) * 0.08f;
            float rC = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);
            shapeRenderer.setColor(rC, gC, bC, alpha);
            shapeRenderer.rect(
                optionsPanelBounds.x - expand,
                optionsPanelBounds.y - expand,
                optionsPanelBounds.width  + expand * 2f,
                optionsPanelBounds.height + expand * 2f);
        }

        // Corona
        float[] coronaExpand = { 6f, 4f, 2f, 1f };
        float[] coronaAlpha  = { 0.04f, 0.08f, 0.13f, 0.18f };
        for (int i = 0; i < 4; i++) {
            float ex = coronaExpand[i];
            shapeRenderer.setColor(0f, 1f, 1f, coronaAlpha[i] * (0.8f + pulse * 0.2f));
            shapeRenderer.rect(
                optionsPanelBounds.x - ex, optionsPanelBounds.y - ex,
                optionsPanelBounds.width  + ex * 2f,
                optionsPanelBounds.height + ex * 2f);
        }

        // Corpo
        shapeRenderer.setColor(0.08f, 0.09f, 0.14f, 0.96f);
        shapeRenderer.rect(optionsPanelBounds.x, optionsPanelBounds.y,
            optionsPanelBounds.width, optionsPanelBounds.height);

        shapeRenderer.setColor(0.13f, 0.14f, 0.20f, 0.18f);
        shapeRenderer.rect(optionsPanelBounds.x + 8f, optionsPanelBounds.y + 8f,
            optionsPanelBounds.width - 16f, optionsPanelBounds.height - 16f);

        // Reflexo topo
        float flareW = optionsPanelBounds.width * (0.50f + pulseFast * 0.15f);
        float flareX = optionsPanelBounds.x + (optionsPanelBounds.width - flareW) * 0.5f;
        float flareY = optionsPanelBounds.y + optionsPanelBounds.height - 5f;
        shapeRenderer.setColor(1f, 1f, 1f, 0.03f + pulseFast * 0.02f);
        shapeRenderer.rect(flareX - 14f, flareY - 1f, flareW + 28f, 6f);
        shapeRenderer.setColor(1f, 1f, 1f, 0.16f + pulseFast * 0.08f);
        shapeRenderer.rect(flareX + 12f, flareY + 1f, flareW - 24f, 1.5f);

        drawOptionRow(backgroundFxBounds, backgroundFxEnabled, true);
        drawOptionRow(pulseFxBounds,      pulseFxEnabled,      false);

        shapeRenderer.end();

        // Contornos
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (int pass = 0; pass < 3; pass++) {
            float expand = (pass + 1) * 4f;
            float phase  = time * 0.55f + pass * MathUtils.PI * 0.5f;
            float raw    = ((phase / MathUtils.PI2) % 1f + 1f) % 1f;
            float hue    = 0.38f + raw * 0.30f;
            float rC = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);
            float a  = Math.max((0.09f - pass * 0.025f) * (0.75f + pulse * 0.25f), 0f);
            shapeRenderer.setColor(rC, gC, bC, a);
            roundedRect(
                optionsPanelBounds.x - expand,
                optionsPanelBounds.y - expand,
                optionsPanelBounds.width  + expand * 2f,
                optionsPanelBounds.height + expand * 2f,
                RADIUS + expand, SEGS);
        }

        drawRainbowBorderEdges(optionsPanelBounds, RADIUS, SEGS, 0.50f + pulse * 0.20f);

        shapeRenderer.setColor(1f, 1f, 1f, 0.12f + pulse * 0.07f);
        roundedRect(optionsPanelBounds.x + 1.5f, optionsPanelBounds.y + 1.5f,
            optionsPanelBounds.width - 3f, optionsPanelBounds.height - 3f,
            RADIUS - 1.5f, SEGS);

        // Botão Back com cantos redondos
        shapeRenderer.setColor(0.75f, 0.95f, 1f, 0.35f + pulse * 0.15f);
        roundedRect(backButtonBounds.x, backButtonBounds.y,
            backButtonBounds.width, backButtonBounds.height, 6f, 4);

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
    // Helpers — cantos redondos + borda arco-íris
    // =========================================================

    private void roundedRect(float x, float y, float w, float h, float r, int segs) {
        float step = 90f / segs;
        // canto sup-dir
        for (int i = 0; i < segs; i++) {
            float a1 = i * step, a2 = a1 + step;
            shapeRenderer.rectLine(
                x + w - r + MathUtils.cosDeg(a1) * r, y + h - r + MathUtils.sinDeg(a1) * r,
                x + w - r + MathUtils.cosDeg(a2) * r, y + h - r + MathUtils.sinDeg(a2) * r, 1.2f);
        }
        // canto sup-esq
        for (int i = 0; i < segs; i++) {
            float a1 = 90f + i * step, a2 = a1 + step;
            shapeRenderer.rectLine(
                x + r + MathUtils.cosDeg(a1) * r, y + h - r + MathUtils.sinDeg(a1) * r,
                x + r + MathUtils.cosDeg(a2) * r, y + h - r + MathUtils.sinDeg(a2) * r, 1.2f);
        }
        // canto inf-esq
        for (int i = 0; i < segs; i++) {
            float a1 = 180f + i * step, a2 = a1 + step;
            shapeRenderer.rectLine(
                x + r + MathUtils.cosDeg(a1) * r, y + r + MathUtils.sinDeg(a1) * r,
                x + r + MathUtils.cosDeg(a2) * r, y + r + MathUtils.sinDeg(a2) * r, 1.2f);
        }
        // canto inf-dir
        for (int i = 0; i < segs; i++) {
            float a1 = 270f + i * step, a2 = a1 + step;
            shapeRenderer.rectLine(
                x + w - r + MathUtils.cosDeg(a1) * r, y + r + MathUtils.sinDeg(a1) * r,
                x + w - r + MathUtils.cosDeg(a2) * r, y + r + MathUtils.sinDeg(a2) * r, 1.2f);
        }
        // lados retos
        shapeRenderer.rectLine(x + r, y + h, x + w - r, y + h, 1.2f);
        shapeRenderer.rectLine(x + r, y,     x + w - r, y,     1.2f);
        shapeRenderer.rectLine(x,     y + r, x,         y + h - r, 1.2f);
        shapeRenderer.rectLine(x + w, y + r, x + w,     y + h - r, 1.2f);
    }

    private void drawRainbowBorderEdges(Rectangle rect, float r, int segs, float alpha) {
        float x = rect.x, y = rect.y, w = rect.width, h = rect.height;
        float basePhase = time * 0.55f;
        float lineW     = 1.2f;

        for (int edge = 0; edge < 4; edge++) {
            float phase = basePhase + edge * (MathUtils.PI2 / 4f);
            float raw   = ((phase / MathUtils.PI2) % 1f + 1f) % 1f;
            float hue   = 0.38f + raw * 0.30f;
            float rC = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);
            shapeRenderer.setColor(rC, gC, bC, alpha);
            switch (edge) {
                case 0: shapeRenderer.rectLine(x + r, y + h, x + w - r, y + h, lineW); break;
                case 1: shapeRenderer.rectLine(x + w, y + r, x + w, y + h - r, lineW); break;
                case 2: shapeRenderer.rectLine(x + r, y, x + w - r, y, lineW);         break;
                case 3: shapeRenderer.rectLine(x, y + r, x, y + h - r, lineW);         break;
            }
        }

        float[] cornerAngles = { 0f, 90f, 180f, 270f };
        float[] cornerX      = { x + w - r, x + r,     x + r,     x + w - r };
        float[] cornerY      = { y + h - r, y + h - r, y + r,     y + r     };
        float step = 90f / segs;

        for (int c = 0; c < 4; c++) {
            float phase = basePhase + c * (MathUtils.PI2 / 4f);
            float raw   = ((phase / MathUtils.PI2) % 1f + 1f) % 1f;
            float hue   = 0.38f + raw * 0.30f;
            float rC = MathUtils.clamp(Math.abs(hue * 6f - 3f) - 1f, 0f, 1f);
            float gC = MathUtils.clamp(2f - Math.abs(hue * 6f - 2f), 0f, 1f);
            float bC = MathUtils.clamp(2f - Math.abs(hue * 6f - 4f), 0f, 1f);
            shapeRenderer.setColor(rC, gC, bC, alpha);
            for (int i = 0; i < segs; i++) {
                float a1 = cornerAngles[c] + i * step;
                float a2 = a1 + step;
                shapeRenderer.rectLine(
                    cornerX[c] + MathUtils.cosDeg(a1) * r,
                    cornerY[c] + MathUtils.sinDeg(a1) * r,
                    cornerX[c] + MathUtils.cosDeg(a2) * r,
                    cornerY[c] + MathUtils.sinDeg(a2) * r, lineW);
            }
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
        float titleY  = centerY + 130f;   // sobe um pouco por conta da fonte maior
        float subY    = titleY  - 72f;

        // ── Pulsação ──────────────────────────────────────────────────
        float pulse     = pulseFxEnabled ? (MathUtils.sin(time * 2.8f) + 1f) * 0.5f : 0.5f;
        float glowA1    = 0.10f + pulse * 0.20f;
        float glowA2    = 0.18f + pulse * 0.28f;

        float pulseNeon = pulseFxEnabled
            ? (MathUtils.sin(time * 2.8f + MathUtils.PI) + 1f) * 0.5f
            : 0.5f;
        float neonA1    = 0.10f + pulseNeon * 0.20f;
        float neonA2    = 0.18f + pulseNeon * 0.28f;



        // ── BLOCK — cyan (mantido) ────────────────────────────────────
        titleFont.getData().setScale(TITLE_SCALE);

        titleFont.setColor(0f, 1f, 1f, glowA1 * 0.6f);
        titleFont.draw(batch, "BLOCK", 0f,  titleY + 10f, vw, Align.center, false);
        titleFont.draw(batch, "BLOCK", -10f, titleY,      vw, Align.center, false);
        titleFont.draw(batch, "BLOCK", 10f,  titleY,      vw, Align.center, false);

        titleFont.setColor(0.15f, 0.95f, 1f, glowA2);
        titleFont.draw(batch, "BLOCK", 0f, titleY + 4f, vw, Align.center, false);

        titleFont.setColor(0f, 0.60f, 0.80f, 0.06f + pulse * 0.08f);
        titleFont.draw(batch, "BLOCK", 3f, titleY - 4f, vw, Align.center, false);

        titleFont.setColor(0.35f, 1f, 1f, 1f);
        titleFont.draw(batch, "BLOCK", 0f, titleY, vw, Align.center, false);

        // ── NEON — LIME neon ──────────────────────────────────────────
        neonFont.getData().setScale(NEON_SCALE);

        // Glow externo amplo (larguíssimo e suave)
        neonFont.setColor(0.55f, 1f, 0.10f, neonA1 * 0.55f);
        neonFont.draw(batch, "NEON", 0f,  subY + 8f, vw, Align.center, false);
        neonFont.draw(batch, "NEON", -7f, subY,      vw, Align.center, false);
        neonFont.draw(batch, "NEON",  7f, subY,      vw, Align.center, false);

        // Glow intermediário
        neonFont.setColor(0.72f, 1f, 0.22f, neonA2);
        neonFont.draw(batch, "NEON", 0f, subY + 3f, vw, Align.center, false);

        // Núcleo brilhante
        neonFont.setColor(0.90f, 1f, 0.55f, 1f);
        neonFont.draw(batch, "NEON", 0f, subY, vw, Align.center, false);

        // Restaura escala padrão para não afetar outros textos
        titleFont.getData().setScale(1f);
        neonFont.getData().setScale(1f);
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
    // FallingPiece (inalterada)
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

        private void drawSquare(ShapeRenderer sr)  { cell(sr, x, y, cellSize, cellSize); }

        private void drawDiamond(ShapeRenderer sr) {
            float cx = x + cellSize, cy = y + cellSize, r = cellSize;
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
            cell(sr, x, y, s, s); cell(sr, x, y + s, s, s);
            cell(sr, x, y + s * 2, s, s); cell(sr, x + s, y, s, s);
        }

        private void drawTetroT(ShapeRenderer sr) {
            float s = cellSize;
            cell(sr, x, y, s, s); cell(sr, x + s, y, s, s);
            cell(sr, x + s * 2, y, s, s); cell(sr, x + s, y + s, s, s);
        }

        private void drawTetroS(ShapeRenderer sr) {
            float s = cellSize;
            cell(sr, x + s, y, s, s); cell(sr, x + s * 2, y, s, s);
            cell(sr, x, y + s, s, s); cell(sr, x + s, y + s, s, s);
        }

        private void drawTetroI(ShapeRenderer sr) {
            for (int i = 0; i < 4; i++) cell(sr, x + cellSize * i, y, cellSize, cellSize);
        }

        private void drawTetroO(ShapeRenderer sr) {
            float s = cellSize;
            cell(sr, x, y, s, s); cell(sr, x + s, y, s, s);
            cell(sr, x, y + s, s, s); cell(sr, x + s, y + s, s, s);
        }

        private void drawCross(ShapeRenderer sr) {
            float s = cellSize;
            cell(sr, x + s, y, s, s); cell(sr, x, y + s, s, s);
            cell(sr, x + s, y + s, s, s); cell(sr, x + s * 2, y + s, s, s);
            cell(sr, x + s, y + s * 2, s, s);
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
