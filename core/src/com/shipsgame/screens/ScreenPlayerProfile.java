package com.shipsgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.shipsgame.replays.Replay;

public class ScreenPlayerProfile extends ScreenAdapter {

    private static final int SCORE_DISPLAY_SPACING = 200;
    private static final int USER_INFO_TOP_PADDING = 30;
    private static final int REPLAYS_LABEL_TOP_PADDING = 50;
    private static final int RETURN_BUTTON_TOP_PADDING = 40;

    private final ShipsGame shipsGame;

    private TextureAtlas textureAtlas;
    private Skin skin;
    private Stage stage;
    private TextField firstNameTextField;
    private TextField lastNameTextField;
    private Label firstNameLabel;
    private Label lastNameLabel;

    public ScreenPlayerProfile(ShipsGame shipsGame) {
        this.shipsGame = shipsGame;
        this.textureAtlas = new TextureAtlas(ShipsGame.UI_SKIN_ATLAS_NAME);
        this.skin = new Skin(Gdx.files.internal(ShipsGame.UI_SKIN_JSON_NAME), textureAtlas);
        firstNameTextField = new TextField("", skin);
        firstNameTextField.setVisible(false);
        lastNameTextField = new TextField("", skin);
        lastNameTextField.setVisible(false);
    }

    public void updatePlayerData() {
        firstNameLabel.setText(shipsGame.getPlayerProfile().getFirstName());
        lastNameLabel.setText(shipsGame.getPlayerProfile().getLastName());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ShipsGame.BACKGROUND_R, ShipsGame.BACKGROUND_G, ShipsGame.BACKGROUND_B, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            shipsGame.activateMenuScreen();
        }
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));
        // Set layout of all info and buttons
        Table rootTable = new Table();
        rootTable.setFillParent(true);

        HorizontalGroup scoreHorizontalGroup = new HorizontalGroup();
        Label winsLabel = new Label("Wins : " + shipsGame.getPlayerProfile().getWins(), skin);
        winsLabel.setColor(Color.GREEN);
        Label losesLabel = new Label("Loses : " + shipsGame.getPlayerProfile().getLoses(), skin);
        losesLabel.setColor(Color.RED);
        scoreHorizontalGroup.space(SCORE_DISPLAY_SPACING);
        scoreHorizontalGroup.addActor(winsLabel);
        scoreHorizontalGroup.addActor(losesLabel);

        Table infoTable = new Table();
        firstNameLabel = new Label(shipsGame.getPlayerProfile().getFirstName(), skin);
        TextButton changeFirstNameTextButton = new TextButton("Modify", skin);
        Label loginLabel = new Label(shipsGame.getPlayerProfile().getLogin(), skin);
        lastNameLabel = new Label(shipsGame.getPlayerProfile().getLastName(), skin);
        TextButton changeLastNameTextButton = new TextButton("Modify", skin);

        infoTable.add(firstNameLabel).expandX().left();
        infoTable.add(loginLabel).expandX().center();
        infoTable.add(lastNameLabel).expandX().right();
        infoTable.row();
        infoTable.add(changeFirstNameTextButton).expandX().colspan(2).left();
        infoTable.add(changeLastNameTextButton).expandX().right();
        infoTable.row();
        infoTable.add(firstNameTextField).colspan(2).left();
        infoTable.add(lastNameTextField).right();


        Table replaysTable = new Table();
        Label replaysLabel = new Label("Replays", skin);
        replaysTable.add(replaysLabel).top().row();
        for (Replay currentReplay : shipsGame.getPlayerProfile().getReplayList()) {
            TextButton replayTextButton = new TextButton("  " + currentReplay.getCreationDateString() + "  ", skin);
            replayTextButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    shipsGame.setCurrentReplay(new Replay(currentReplay));
                    shipsGame.activateReplayerScreen();
                }
            });
            replaysTable.add(replayTextButton).row();
        }
        ScrollPane replaysScrollPane = new ScrollPane(replaysTable, skin);
        replaysScrollPane.setScrollingDisabled(true, false);
        replaysScrollPane.setFadeScrollBars(false);

        Table buttonsTable = new Table();
        TextButton returnButton = new TextButton("Return", skin);
        buttonsTable.add(returnButton);

        rootTable.add(scoreHorizontalGroup).top().row();
        rootTable.add(infoTable).top().padTop(USER_INFO_TOP_PADDING).expand().fillX().row();
        rootTable.add(replaysScrollPane).padTop(REPLAYS_LABEL_TOP_PADDING).top().expand().row();
        rootTable.add(buttonsTable).padTop(RETURN_BUTTON_TOP_PADDING).expand().bottom();
        stage.addActor(rootTable);
        stage.setScrollFocus(replaysScrollPane);


        // Add events for buttons
        returnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                shipsGame.activateMenuScreen();
            }
        });
        changeFirstNameTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!firstNameTextField.isVisible()) {
                    firstNameTextField.setVisible(true);
                    changeFirstNameTextButton.setText("Accept");
                } else {
                    shipsGame.getPlayerProfile().setFirstName(firstNameTextField.getText());
                    firstNameTextField.setVisible(false);
                    changeFirstNameTextButton.setText("Modify");
                    updatePlayerData();
                }
            }
        });
        changeLastNameTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!lastNameTextField.isVisible()) {
                    lastNameTextField.setVisible(true);
                    changeLastNameTextButton.setText("Accept");
                } else {
                    shipsGame.getPlayerProfile().setLastName(lastNameTextField.getText());
                    lastNameTextField.setVisible(false);
                    changeLastNameTextButton.setText("Modify");
                    updatePlayerData();
                }
            }
        });
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void dispose() {
        skin.dispose();
        textureAtlas.dispose();
        if (stage != null) {
            stage.dispose();
        }
        System.out.println("dispose - ScreenPlayerProfile");
    }
}
