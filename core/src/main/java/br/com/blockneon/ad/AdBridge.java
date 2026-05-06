package br.com.blockneon.ad;

public interface AdBridge {
    /** Exibe o banner. Chame quando o GameScreen aparecer. */
    void showBanner();

    /** Esconde o banner. Chame no pause/game over. */
    void hideBanner();
}
