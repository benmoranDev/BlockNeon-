package br.com.blockneon;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import br.com.blockneon.model.ScoreManager;
import br.com.blockneon.screens.MainMenuScreen;
import br.com.blockneon.ui.FontManager;

public class Main extends Game {

    public FontManager fontManager;
    public ScoreManager scoreManager;

    @Override
    public void create() {

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        fontManager  = new FontManager(w, h);
        scoreManager = new ScoreManager();

        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        fontManager.dispose();
    }
}
