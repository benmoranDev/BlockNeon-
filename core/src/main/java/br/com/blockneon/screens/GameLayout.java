package br.com.blockneon.screens;

import br.com.blockneon.model.Board;
import com.badlogic.gdx.math.Rectangle;


public class GameLayout {

    // =========================================================
    // World / Mundo virtual (referência base, pode ser estendido)
    // =========================================================
    public static final float WORLD_WIDTH  = 640f;
    public static final float WORLD_HEIGHT = 1080f;

    // =========================================================
    // Board / Tabuleiro
    // =========================================================
    public static final float CELL_SIZE    = 50f;
    public static final float BOARD_WIDTH  = Board.COLS * CELL_SIZE;
    public static final float BOARD_HEIGHT = Board.ROWS * CELL_SIZE;

    public static final float BOARD_FRAME_PADDING = 2f;
    public static final float BOARD_INNER_PADDING = 6f;

    // =========================================================
    // Shell sizes / Tamanhos dos HUDs
    // =========================================================
    private static final float TOP_SHELL_HEIGHT    = 150f;
    private static final float BOTTOM_SHELL_HEIGHT = 120f;
    private static final float SHELL_GAP           = 24f;

    // =========================================================
    // Área de anúncio no topo
    // =========================================================
    private static final float TOP_AD_HEIGHT        = 40f;
    private static final float TOP_AD_MARGIN_BOTTOM = 6f;

    public final Rectangle topAdBounds = new Rectangle();

    // =========================================================
    // Botão Pause
    // =========================================================
    public final Rectangle pauseButtonBounds = new Rectangle();
    private static final float PAUSE_BUTTON_SIZE         = 52f;
    private static final float PAUSE_BUTTON_MARGIN_RIGHT = 14f;
    private static final float PAUSE_BUTTON_MARGIN_TOP   = 10f;

    // =========================================================
    // Top box layout
    // =========================================================
    private static final float TOP_BOX_WIDTH         = 120f;
    private static final float TOP_BOX_HEIGHT        = 72f;
    private static final float TOP_BOX_GAP           = 12f;
    private static final float TOP_BOX_BOTTOM_MARGIN = 14f;

    // =========================================================
    // Bottom box layout
    // =========================================================
    private static final float BOTTOM_BOX_Y_OFFSET   = 18f;
    private static final float NEXT_MAIN_BOX_WIDTH   = 110f;
    private static final float NEXT_MAIN_BOX_HEIGHT  = 80f;
    private static final float NEXT_QUEUE_BOX_WIDTH  = 70f;
    private static final float NEXT_QUEUE_BOX_HEIGHT = 60f;
    private static final float BOTTOM_BOX_GAP        = 12f;

    // =========================================================
    // Posições principais (calculadas dinamicamente)
    // =========================================================
    public float boardX;
    public float boardY;

    // =========================================================
    // Dimensões reais da tela virtual
    // =========================================================
    public float viewportWidth  = WORLD_WIDTH;
    public float viewportHeight = WORLD_HEIGHT;

    // =========================================================
    // Shell bounds
    // =========================================================
    public final Rectangle topShellBounds    = new Rectangle();
    public final Rectangle bottomShellBounds = new Rectangle();
    public final Rectangle leftShellBounds   = new Rectangle();
    public final Rectangle rightShellBounds  = new Rectangle();

    // =========================================================
    // Internal boxes
    // =========================================================
    public final Rectangle holdBox       = new Rectangle();
    public final Rectangle levelBox      = new Rectangle();
    public final Rectangle goalBox       = new Rectangle();
    public final Rectangle nextMainBox   = new Rectangle();
    public final Rectangle nextQueueBox1 = new Rectangle();
    public final Rectangle nextQueueBox2 = new Rectangle();
    public final Rectangle nextQueueBox3 = new Rectangle();
    public final Rectangle nextQueueBox4 = new Rectangle();

    /**
     * Call this from GameScreen.resize() passing the current
     * ExtendViewport world dimensions.
     *
     * Chame isso do GameScreen.resize() passando as dimensões
     * reais do mundo do ExtendViewport.
     */
    public void update(float vpWidth, float vpHeight) {
        this.viewportWidth  = vpWidth;
        this.viewportHeight = vpHeight;
        updateLayout();
    }

    /** Convenience overload — uses last known viewport size. */
    public void update() {
        updateLayout();
    }

    // =========================================================
    // Internal layout calculation
    // =========================================================
    private void updateLayout() {

        float extraVertical   = Math.max(0f, viewportHeight - WORLD_HEIGHT);
        float extraHorizontal = Math.max(0f, viewportWidth  - WORLD_WIDTH);

        float vPad = extraVertical   / 2f;
        float hPad = extraHorizontal / 2f;

        // ---------------------------------------------------
        // Tabuleiro centralizado
        // ---------------------------------------------------
        boardX = hPad + (WORLD_WIDTH  - BOARD_WIDTH)  / 2f;
        boardY = vPad + (WORLD_HEIGHT - BOARD_HEIGHT) / 2f - 60f;
        boardY = Math.max(boardY, BOTTOM_SHELL_HEIGHT + SHELL_GAP);

        float shellWidth = viewportWidth;

        // ---------------------------------------------------
        // Shell superior — logo acima do board
        // ---------------------------------------------------
        float topShellY = boardY + BOARD_HEIGHT + SHELL_GAP;
        topShellBounds.set(0f, topShellY, shellWidth, TOP_SHELL_HEIGHT);

        // ---------------------------------------------------
        // Shell inferior — ancorado na borda inferior da tela
        // ---------------------------------------------------
        bottomShellBounds.set(0f, 0f, shellWidth, BOTTOM_SHELL_HEIGHT);

        // ---------------------------------------------------
        // Botão pause — canto superior direito do top shell
        // ---------------------------------------------------
        pauseButtonBounds.set(
            topShellBounds.x + topShellBounds.width - PAUSE_BUTTON_SIZE - PAUSE_BUTTON_MARGIN_RIGHT,
            topShellBounds.y + topShellBounds.height - PAUSE_BUTTON_SIZE - PAUSE_BUTTON_MARGIN_TOP,
            PAUSE_BUTTON_SIZE,
            PAUSE_BUTTON_SIZE
        );

        // ---------------------------------------------------
        // Faixa de anúncio
        // ---------------------------------------------------
        topAdBounds.set(
            topShellBounds.x + 12f,
            topShellBounds.y + topShellBounds.height - TOP_AD_HEIGHT - TOP_AD_MARGIN_BOTTOM,
            topShellBounds.width - 24f,
            TOP_AD_HEIGHT
        );

        // ---------------------------------------------------
        // Top boxes (HOLD / LEVEL / GOAL)
        // ---------------------------------------------------
        float topRowWidth = TOP_BOX_WIDTH * 3f + TOP_BOX_GAP * 2f;
        float topRowX     = topShellBounds.x + (topShellBounds.width - topRowWidth) / 2f;
        float topBoxY     = topShellBounds.y + TOP_BOX_BOTTOM_MARGIN + 10f;

        holdBox.set(topRowX, topBoxY, TOP_BOX_WIDTH, TOP_BOX_HEIGHT);

        levelBox.set(
            holdBox.x + holdBox.width + TOP_BOX_GAP,
            topBoxY,
            TOP_BOX_WIDTH,
            TOP_BOX_HEIGHT
        );

        goalBox.set(
            levelBox.x + levelBox.width + TOP_BOX_GAP,
            topBoxY,
            TOP_BOX_WIDTH,
            TOP_BOX_HEIGHT
        );

        // ---------------------------------------------------
        // Bottom boxes (NEXT queue)
        // ---------------------------------------------------
        float bottomRowWidth =
            NEXT_MAIN_BOX_WIDTH + BOTTOM_BOX_GAP +
                NEXT_QUEUE_BOX_WIDTH * 4f + BOTTOM_BOX_GAP * 3f;

        float bottomRowX = bottomShellBounds.x
            + (bottomShellBounds.width - bottomRowWidth) / 2f;
        float bottomBoxY = bottomShellBounds.y + BOTTOM_BOX_Y_OFFSET;

        nextMainBox.set(
            bottomRowX,
            bottomBoxY,
            NEXT_MAIN_BOX_WIDTH,
            NEXT_MAIN_BOX_HEIGHT
        );

        nextQueueBox1.set(
            nextMainBox.x + nextMainBox.width + BOTTOM_BOX_GAP,
            bottomBoxY + 8f,
            NEXT_QUEUE_BOX_WIDTH,
            NEXT_QUEUE_BOX_HEIGHT
        );

        nextQueueBox2.set(
            nextQueueBox1.x + nextQueueBox1.width + BOTTOM_BOX_GAP,
            bottomBoxY + 8f,
            NEXT_QUEUE_BOX_WIDTH,
            NEXT_QUEUE_BOX_HEIGHT
        );

        nextQueueBox3.set(
            nextQueueBox2.x + nextQueueBox2.width + BOTTOM_BOX_GAP,
            bottomBoxY + 8f,
            NEXT_QUEUE_BOX_WIDTH,
            NEXT_QUEUE_BOX_HEIGHT
        );

        nextQueueBox4.set(
            nextQueueBox3.x + nextQueueBox3.width + BOTTOM_BOX_GAP,
            bottomBoxY + 8f,
            NEXT_QUEUE_BOX_WIDTH,
            NEXT_QUEUE_BOX_HEIGHT
        );

        // ---------------------------------------------------
        // Compatibilidade legado
        // ---------------------------------------------------
        leftShellBounds.set(topShellBounds);
        rightShellBounds.set(bottomShellBounds);
    }
}
