package br.com.blockneon.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class ScoreManager {

    private static final String PREFS_NAME  = "blockneon_data";
    private static final String KEY_BEST    = "best_score";

    private final Preferences prefs;
    private int bestScore;

    // =========================================================
    // Constructor
    // =========================================================

    public ScoreManager() {
        prefs     = Gdx.app.getPreferences(PREFS_NAME);
        bestScore = prefs.getInteger(KEY_BEST, 0);
    }

    // =========================================================
    // Public API
    // =========================================================

    /**
     * Submits a score. Saves if it beats the current best.
     * Envia um score. Salva se for maior que o recorde atual.
     */
    public boolean submit(int score) {
        if (score > bestScore) {
            bestScore = score;
            prefs.putInteger(KEY_BEST, bestScore);
            prefs.flush(); // grava no arquivo imediatamente
            return true;   // retorna true = novo recorde
        }
        return false;
    }

    /**
     * Returns the saved best score.
     * Retorna o melhor score salvo.
     */
    public int getBestScore() {
        return bestScore;
    }

    /**
     * Resets the best score to zero.
     * Zera o melhor score salvo.
     */
    public void reset() {
        bestScore = 0;
        prefs.putInteger(KEY_BEST, 0);
        prefs.flush();
    }
}
