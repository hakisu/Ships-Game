package com.shipsgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ScreenMenu extends ScreenAdapter {

    private static final float TEXT_SCALE_FACTOR = 1f;

    private final ShipsGame shipsGame;

    private TextureAtlas textureAtlas;
    private Skin skin;
    private Stage stage;

    public ScreenMenu(ShipsGame shipsGame) {
        this.shipsGame = shipsGame;
        stage = new Stage(new ScreenViewport(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));
        this.textureAtlas = new TextureAtlas(ShipsGame.UI_SKIN_ATLAS_NAME);
        this.skin = new Skin(Gdx.files.internal(ShipsGame.UI_SKIN_JSON_NAME), textureAtlas);

        TextButton resumeTextButton = new TextButton("Resume", skin);
        resumeTextButton.getLabel().setFontScale(TEXT_SCALE_FACTOR);
        TextButton newGameTextButton = new TextButton("New Game", skin);
        newGameTextButton.getLabel().setFontScale(TEXT_SCALE_FACTOR);
        TextButton playerProfileTextButton = new TextButton("Player Profile", skin);
        playerProfileTextButton.getLabel().setFontScale(TEXT_SCALE_FACTOR);
        TextButton exitTextButton = new TextButton("Exit", skin);
        exitTextButton.getLabel().setFontScale(TEXT_SCALE_FACTOR);

        Table table = new Table();
        table.setFillParent(true);
        table.add(resumeTextButton).width(Gdx.graphics.getWidth() / 6);
        table.row();
        table.add(newGameTextButton).width(Gdx.graphics.getWidth() / 5);
        table.row();
        table.add(playerProfileTextButton).width(Gdx.graphics.getWidth() / 3);
        table.row();
        table.add(exitTextButton).width(Gdx.graphics.getWidth() / 2);
        stage.addActor(table);

        // Add events for buttons
        resumeTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (shipsGame.isMapInitialized()) {
                    shipsGame.getPlayerProfile().setResumedGameMap(null);
                    shipsGame.activateGameScreen();
                    return;
                }
                // What happens when there is a game to resume in player profile
                if (shipsGame.getPlayerProfile().getResumedGameMap() != null) {
                    shipsGame.initializeMap(shipsGame.getPlayerProfile().getResumedGameMap());
                    shipsGame.getPlayerProfile().setResumedGameMap(null);
                    shipsGame.activateGameScreen();
                }
            }
        });
        newGameTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (shipsGame.isMapInitialized() || shipsGame.getPlayerProfile().getResumedGameMap() != null) {
                    shipsGame.getPlayerProfile().addLoss();
                }
                shipsGame.initializeMap();
                shipsGame.getPlayerProfile().setResumedGameMap(null);
                shipsGame.activateGameScreen();
            }
        });
        playerProfileTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                shipsGame.activatePlayerProfileScreen();
            }
        });
        exitTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ShipsGame.BACKGROUND_R, ShipsGame.BACKGROUND_G, ShipsGame.BACKGROUND_B, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            shipsGame.activateGameScreen();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        skin.dispose();
        textureAtlas.dispose();
        stage.dispose();
        System.out.println("dispose - ScreenMenu");
    }
}