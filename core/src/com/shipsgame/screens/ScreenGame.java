package com.shipsgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.shipsgame.map.Map;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

public class ScreenGame extends ScreenAdapter {

    private static final float INFO_EXISTENCE_LENGTH = 10;

    private final ShipsGame shipsGame;

    private boolean gameRunning = true;
    private boolean mapInitialized = false;
    private Map map;
    private OrthographicCamera camera;
    // Fields for displaying text information
    private TextureAtlas textureAtlas;
    private Skin skin;
    private Stage stage;
    private Label infoLabel;
    private float infoTimer = 0;
    private boolean infoVisible = false;
    private TextButton confirmButton;

    public ScreenGame(ShipsGame shipsGame) {
        this.shipsGame = shipsGame;

        camera = new OrthographicCamera();
        camera.setToOrtho(true, Map.COLUMNS_AMOUNT * Map.TILE_WIDTH, Map.ROWS_AMOUNT * Map.TILE_HEIGHT);
        camera.position.x = camera.viewportWidth / 2;
        camera.position.y = camera.viewportHeight / 2;

        // Set up for displaying messages during game
        stage = new Stage(new ScreenViewport(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));
        textureAtlas = new TextureAtlas(ShipsGame.UI_SKIN_ATLAS_NAME);
        skin = new Skin(Gdx.files.internal(ShipsGame.UI_SKIN_JSON_NAME), textureAtlas);
        infoLabel = new Label("", skin);
        infoLabel.setVisible(false);
        infoLabel.setColor(Color.BLACK);
        confirmButton = new TextButton("Ok", skin);
        confirmButton.setVisible(false);
        Table rootTable = new Table();
        rootTable.top();
        rootTable.setFillParent(true);
        rootTable.add(infoLabel).center();
        rootTable.row();
        rootTable.add(confirmButton).padTop(20);
        stage.addActor(rootTable);

        confirmButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                shipsGame.activateMenuScreen();
            }
        });
    }

    public boolean isMapInitialized() {
        return mapInitialized;
    }

    public void setMapInitialized(boolean mapInitialized) {
        this.mapInitialized = mapInitialized;
    }

    public void initializeMap() {
        this.gameRunning = true;
        confirmButton.setVisible(false);
        this.map = new Map();
        this.mapInitialized = true;
    }

    public void initializeMap(Map map) {
        this.map = map;
        this.mapInitialized = true;
    }

    private void update(float delta) {
        if (gameRunning) {
            // Handle user mouse click input
            if (Gdx.input.justTouched()) {
                hideMessage();
                // Calculate indexX and indexY of tile where mouse cursor is pointing
                Vector3 mousePositionInGameWorld = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                camera.unproject(mousePositionInGameWorld);
                int tileIndexX = (int) mousePositionInGameWorld.x / Map.TILE_WIDTH;
                int tileIndexY = (int) mousePositionInGameWorld.y / Map.TILE_HEIGHT;

                map.update(tileIndexX, tileIndexY, this, shipsGame.getPlayerProfile());
            }

            // Timer responsible for displaying messages for specified length
            if (infoVisible) {
                infoTimer += delta;
                if (infoTimer >= INFO_EXISTENCE_LENGTH) {
                    infoVisible = false;
                    infoLabel.setVisible(false);
                    infoTimer = 0;
                }
            }
        }
    }

    private void updateGraphics() {
        camera.update();
        shipsGame.shapeRenderer.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(ShipsGame.BACKGROUND_R, ShipsGame.BACKGROUND_G, ShipsGame.BACKGROUND_B, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        map.render(shipsGame.shapeRenderer);
    }

    public void displayMessage(String messageText) {
        infoLabel.setText(messageText);
        infoLabel.setVisible(true);
        infoVisible = true;
        infoTimer = 0;
    }

    private void hideMessage() {
        infoTimer = INFO_EXISTENCE_LENGTH;
    }

    public void displayGameOver(boolean playerWon) {
        if (playerWon) {
            infoLabel.setText("You won the game.");
        } else {
            infoLabel.setText("Game over. You lost the game.");
        }
        infoLabel.setVisible(true);
        confirmButton.setVisible(true);
        gameRunning = false;
        map.getReplayInConstruction().setCreationDateString(new Date().toString());
        shipsGame.getPlayerProfile().addReplay(map.getReplayInConstruction());
    }

    // Main game loop
    @Override
    public void render(float delta) {
        // Logical updates and mouse input handling
        update(delta);

        // Graphical updates
        updateGraphics();

        // Handle keyboard input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            shipsGame.activateMenuScreen();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            displayMessage("Grey tiles - your ship\nGreen circle - you missed your shot\nRed circle - you hit enemy ship" +
                    "\nBlack circle - you destroyed enemy ship\nGray cross - previous ai missed attack\nYellow cross - ai successful hit");
        }

        // Draw and handle text messages
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void show() {
        // Set input processor to new empty InputAdapter to disable input handling from other screens
        Gdx.input.setInputProcessor(new InputAdapter());
        Gdx.input.setInputProcessor(stage);

        // Display message about player being in "placing ships" mode
        if (!map.isMapCreatorFinished()) {
            displayMessage("Place your ships on the map.");
        } else {
            displayMessage("Game started. Click mouse to attack chosen tile.\nPress h for help.");
        }
    }

    @Override
    public void dispose() {
        if (shipsGame.getPlayerProfile() != null) {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ShipsGame.PATH_TO_PLAYER_PROFILES + shipsGame.getPlayerProfile().getLogin() + ".profile"))) {
                if (this.isMapInitialized()) {
                    shipsGame.getPlayerProfile().setResumedGameMap(this.map);
                } else {
                    shipsGame.getPlayerProfile().setResumedGameMap(null);
                }
                out.writeObject(shipsGame.getPlayerProfile());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        skin.dispose();
        textureAtlas.dispose();
        stage.dispose();
        System.out.println("dispose - ScreenGame");
    }
}