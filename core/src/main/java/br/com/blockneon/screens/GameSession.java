package br.com.blockneon.screens;


import com.badlogic.gdx.utils.Array;
import br.com.blockneon.model.ActivePiece;
import br.com.blockneon.model.Board;
import br.com.blockneon.model.Tetromino;




public class GameSession {

    // =========================================================
    // Gameplay tuning / Ajustes de gameplay
    // =========================================================

    private static final float BASE_DROP_INTERVAL = 0.50f;
    private static final float LOCK_DELAY = 0.50f;
    public static final float LINE_FLASH_DURATION = 0.18f;

    private static final int NEXT_QUEUE_SIZE = 5;
    private static final int LINES_PER_LEVEL = 10;

    // =========================================================
    // Core state / Estado principal
    // =========================================================

    private final Board board;
    private ActivePiece activePiece;

    private final Array<Tetromino> nextQueue = new Array<>();
    private final Array<Tetromino> bag = new Array<>();

    private Tetromino heldTetromino;
    private boolean holdUsedThisTurn = false;

    private float dropTimer = 0f;
    private float lockTimer = 0f;

    private int score = 0;
    private int linesClearedTotal = 0;
    private int level = 1;

    // =========================================================
    // FX state / Estado de efeitos
    // =========================================================

    private final Array<Integer> flashRows = new Array<>();
    private float lineFlashTimer = 0f;

    // =========================================================
    // Audio flags / Sinais de áudio
    // =========================================================

    private boolean pendingDropSound = false;
    private boolean pendingClearSound = false;


    // =========================================================
    // Combo state / Estado de combo
    // =========================================================

    private int   comboCount       = 0;
    private float comboWindowTimer = 0f;
    private static final float COMBO_WINDOW = 4.0f;

    private boolean pendingComboNotify = false;
    private boolean pendingComboReset  = false;

    private boolean gameOver = false;

    /**
     * Creates a new gameplay session.
     * Cria uma nova sessão de gameplay.
     */
    public GameSession(Board board) {
        this.board = board;
    }

    // =========================================================
    // Lifecycle / Ciclo de vida
    // =========================================================

    /**
     * Resets the whole run state.
     * Reinicia todo o estado da partida.
     */
    public void resetRun() {
        board.clear();
        activePiece = null;
        gameOver         = false;   // << Game Over
        nextQueue.clear();
        bag.clear();

        heldTetromino = null;
        holdUsedThisTurn = false;

        dropTimer = 0f;
        lockTimer = 0f;

        score = 0;
        linesClearedTotal = 0;
        level = 1;

        flashRows.clear();
        lineFlashTimer = 0f;

        pendingDropSound = false;
        pendingClearSound = false;

        ensureNextQueueFilled();
        spawnPiece();
    }

    /**
     * Updates gameplay and transient effects.
     * Atualiza o gameplay e os efeitos temporários.
     */
    public void update(float delta) {
        updateLineFlash(delta);
        updateComboWindow(delta); // << linha nova
        updateFall(delta);
    }

    /**
     * Updates the line clear flash timer.
     * Atualiza o timer do flash de limpeza de linha.
     */
    private void updateLineFlash(float delta) {
        if (lineFlashTimer > 0f) {
            lineFlashTimer -= delta;

            if (lineFlashTimer <= 0f) {
                lineFlashTimer = 0f;
                flashRows.clear();
            }
        }
    }

    // =========================================================
    // Bag / Queue / Hold
    // =========================================================

    /**
     * Refills the 7-bag randomizer.
     * Reabastece o randomizador 7-bag.
     */
    private void refillBag() {
        bag.clear();

        for (Tetromino tetromino : Tetromino.values()) {
            bag.add(tetromino);
        }

        bag.shuffle();
    }

    /**
     * Draws one tetromino from the bag.
     * Retira um tetromino do bag.
     */
    private Tetromino drawFromBag() {
        if (bag.size == 0) {
            refillBag();
        }

        return bag.pop();
    }

    /**
     * Ensures the visible next queue is always full.
     * Garante que a fila visível de próximas peças esteja sempre cheia.
     */
    private void ensureNextQueueFilled() {
        while (nextQueue.size < NEXT_QUEUE_SIZE) {
            nextQueue.add(drawFromBag());
        }
    }

    /**
     * Spawns the next active piece from the queue.
     * Cria a próxima peça ativa a partir da fila.
     */
    private void spawnPiece() {
        ensureNextQueueFilled();

        Tetromino next = nextQueue.removeIndex(0);
        activePiece = new ActivePiece(next, Board.ROWS - 2, 3);

        ensureNextQueueFilled();

        dropTimer = 0f;
        lockTimer = 0f;
        holdUsedThisTurn = false;

        if (!board.canPlace(activePiece)) {
            activePiece = null;   // para o jogo
            gameOver    = true;   // sinaliza game over
        }
    }

    /**
     * Attempts to hold the current active piece.
     * Tenta guardar a peça ativa atual.
     */
    public boolean tryHoldPiece() {
        if (activePiece == null || holdUsedThisTurn) {
            return false;
        }

        Tetromino current = activePiece.getTetromino();

        if (heldTetromino == null) {
            heldTetromino = current;
            spawnPiece();
            holdUsedThisTurn = true;
            return true;
        }

        Tetromino swap = heldTetromino;
        heldTetromino = current;

        ActivePiece swappedPiece = new ActivePiece(swap, Board.ROWS - 2, 3);

        if (board.canPlace(swappedPiece)) {
            activePiece = swappedPiece;
            dropTimer = 0f;
            lockTimer = 0f;
            holdUsedThisTurn = true;
            return true;
        }

        heldTetromino = swap;
        return false;
    }


    private void updateComboWindow(float delta) {
        if (comboWindowTimer > 0f) {
            comboWindowTimer -= delta;

            if (comboWindowTimer <= 0f) {
                comboWindowTimer   = 0f;
                comboCount         = 0;
                pendingComboReset  = true;
            }
        }
    }

    // =========================================================
    // Movement / Movimento
    // =========================================================

    /**
     * Attempts to move the active piece left.
     * Tenta mover a peça ativa para a esquerda.
     */
    public boolean tryMoveLeft() {
        if (activePiece == null) {
            return false;
        }

        activePiece.moveLeft();

        if (!board.canPlace(activePiece)) {
            activePiece.moveRight();
            return false;
        }

        lockTimer = 0f;
        return true;
    }

    /**
     * Attempts to move the active piece right.
     * Tenta mover a peça ativa para a direita.
     */
    public boolean tryMoveRight() {
        if (activePiece == null) {
            return false;
        }

        activePiece.moveRight();

        if (!board.canPlace(activePiece)) {
            activePiece.moveLeft();
            return false;
        }

        lockTimer = 0f;
        return true;
    }

    /**
     * Attempts to rotate the active piece clockwise.
     * Tenta rotacionar a peça ativa no sentido horário.
     */
    public boolean tryRotate() {
        if (activePiece == null) {
            return false;
        }

        activePiece.rotateRight();

        if (!board.canPlace(activePiece)) {
            activePiece.rotateLeft();
            return false;
        }

        lockTimer = 0f;
        return true;
    }

    /**
     * Performs one soft-drop step.
     * Executa um passo de soft drop.
     */
    public void softDropOneStep() {
        if (activePiece == null) {
            return;
        }

        if (tryStepDown()) {
            score += 1;
            lockTimer = 0f;
        }
    }

    /**
     * Performs a hard drop and awards points per cell.
     * Executa um hard drop e concede pontos por célula.
     */
    public void hardDrop() {
        if (activePiece == null) {
            return;
        }

        int droppedCells = 0;

        while (tryStepDown()) {
            droppedCells++;
        }

        score += droppedCells * 2;
        pendingDropSound = true;
        lockAndSpawn();
    }

    /**
     * Tries to move the active piece one row down.
     * Tenta mover a peça ativa uma linha para baixo.
     */
    private boolean tryStepDown() {
        activePiece.moveDown();

        if (!board.canPlace(activePiece)) {
            activePiece.moveUp();
            return false;
        }

        return true;
    }

    /**
     * Checks whether the active piece is grounded.
     * Verifica se a peça ativa está apoiada.
     */
    private boolean isGrounded() {
        if (activePiece == null) {
            return false;
        }

        activePiece.moveDown();
        boolean canPlace = board.canPlace(activePiece);
        activePiece.moveUp();

        return !canPlace;
    }

    // =========================================================
    // Falling / Lock / Clear
    // =========================================================

    /**
     * Updates automatic falling and lock delay.
     * Atualiza a queda automática e o lock delay.
     */
    private void updateFall(float delta) {
        if (activePiece == null) {
            return;
        }

        dropTimer += delta;
        float interval = getCurrentDropInterval();

        while (dropTimer >= interval) {
            dropTimer -= interval;

            if (!tryStepDown()) {
                break;
            } else {
                lockTimer = 0f;
            }
        }

        if (isGrounded()) {
            lockTimer += delta;

            if (lockTimer >= LOCK_DELAY) {
                pendingDropSound = true;
                lockAndSpawn();
            }
        } else {
            lockTimer = 0f;
        }
    }

    /**
     * Locks the current piece, clears lines and spawns a new one.
     * Trava a peça atual, limpa linhas e cria a próxima.
     */
    private void lockAndSpawn() {
        board.lockPiece(activePiece);

        Array<Integer> clearedRows = board.clearLinesWithInfo();
        int cleared = clearedRows.size;

        if (cleared > 0) {
            flashRows.clear();
            flashRows.addAll(clearedRows);
            lineFlashTimer = LINE_FLASH_DURATION;

            linesClearedTotal += cleared;
            refreshLevel();

            score += cleared * 100 * cleared * level;

            comboCount++;
            comboWindowTimer   = COMBO_WINDOW;
            pendingComboNotify = true;

            pendingClearSound = true;
        } else {
            refreshLevel();
            // Não reseta o combo aqui — a janela de 4s expira naturalmente
        }

        spawnPiece();
    }

    /**
     * Updates the derived level from total cleared lines.
     * Atualiza o nível derivado do total de linhas limpas.
     */
    private void refreshLevel() {
        level = 1 + (linesClearedTotal / LINES_PER_LEVEL);
    }

    /**
     * Returns the current gravity interval based on level.
     * Retorna o intervalo atual de queda com base no nível.
     */
    private float getCurrentDropInterval() {
        return Math.max(0.08f, BASE_DROP_INTERVAL - (level - 1) * 0.03f);
    }

    // =========================================================
    // Audio consumption / Consumo de áudio
    // =========================================================

    /**
     * Consumes the pending drop sound flag.
     * Consome o sinal pendente de som de queda.
     */
    public boolean consumeDropSound() {
        boolean value = pendingDropSound;
        pendingDropSound = false;
        return value;
    }

    /**
     * Consumes the pending clear sound flag.
     * Consome o sinal pendente de som de limpeza.
     */
    public boolean consumeClearSound() {
        boolean value = pendingClearSound;
        pendingClearSound = false;
        return value;
    }

    // =========================================================
    // Getters / Acessores
    // =========================================================

    public Board getBoard() {
        return board;
    }

    public ActivePiece getActivePiece() {
        return activePiece;
    }

    public Array<Tetromino> getNextQueue() {
        return nextQueue;
    }

    public Tetromino getHeldTetromino() {
        return heldTetromino;
    }

    public Array<Integer> getFlashRows() {
        return flashRows;
    }

    public float getLineFlashTimer() {
        return lineFlashTimer;
    }

    public int getScore() {
        return score;
    }

    public int getLinesClearedTotal() {
        return linesClearedTotal;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Returns remaining lines until the next level.
     * Retorna as linhas restantes até o próximo nível.
     */
    public int getGoalLines() {
        int remaining = LINES_PER_LEVEL - (linesClearedTotal % LINES_PER_LEVEL);
        return remaining == 0 ? LINES_PER_LEVEL : remaining;
    }

    public boolean isHoldUsedThisTurn() {
        return holdUsedThisTurn;
    }

    public boolean consumeComboNotify() {
        boolean v = pendingComboNotify;
        pendingComboNotify = false;
        return v;
    }

    public boolean consumeComboReset() {
        boolean v = pendingComboReset;
        pendingComboReset = false;
        return v;
    }

    public int getComboCount() {
        return comboCount;
    }

    public float getComboWindowTimer() {
        return comboWindowTimer;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
