package com.shipsgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.shipsgame.Ship;
import com.shipsgame.map.Map;
import com.shipsgame.map.Symbol;
import com.shipsgame.map.Tile;
import com.shipsgame.map.TileType;
import com.shipsgame.replays.Replay;
import com.shipsgame.utilities.Pair;

public class ScreenReplayer extends ScreenAdapter {

    private final ShipsGame shipsGame;

    private OrthographicCamera camera;
    private Tile[][] mapArray;
    private int rowsAmount;
    private int columnsAmount;
    private Replay currentReplay;

    public ScreenReplayer(ShipsGame shipsGame) {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, Map.COLUMNS_AMOUNT * Map.TILE_WIDTH, Map.ROWS_AMOUNT * Map.TILE_HEIGHT);
        camera.position.x = camera.viewportWidth / 2;
        camera.position.y = camera.viewportHeight / 2;

        this.shipsGame = shipsGame;
        this.rowsAmount = Map.ROWS_AMOUNT;
        this.columnsAmount = Map.COLUMNS_AMOUNT;
        this.mapArray = new Tile[rowsAmount][columnsAmount];
        for (int i = 0; i < rowsAmount; i++) {
            for (int j = 0; j < columnsAmount; j++) {
                this.mapArray[i][j] = new Tile();
            }
        }
    }

    /**
     * Needs to be invoked before showing this screen.
     *
     * @param currentReplay replay to be watched in ScreenReplayer
     */
    public void setCurrentReplay(Replay currentReplay) {
        this.currentReplay = currentReplay;
    }

    private void update(float delta) {
        this.currentReplay.update(delta, this.mapArray);
    }

    private void updateGraphics(ShapeRenderer shapeRenderer) {
        camera.update();
        shipsGame.shapeRenderer.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(ShipsGame.BACKGROUND_R, ShipsGame.BACKGROUND_G, ShipsGame.BACKGROUND_B, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < rowsAmount; i++) {
            for (int j = 0; j < columnsAmount; j++) {
                // Draw player ships
                if (mapArray[i][j].getTileType() == TileType.SHIP) {
                    shapeRenderer.setColor(Color.DARK_GRAY);
                } else if (mapArray[i][j].getTileType() == TileType.EMPTY) {
                    // Light blue color
                    shapeRenderer.setColor(new Color(0.004f, 0.569f, 0.784f, 1));
                }
                shapeRenderer.rect(j * Map.TILE_WIDTH, i * Map.TILE_HEIGHT, Map.TILE_WIDTH, Map.TILE_HEIGHT);
                // Draw ai attacks
                if (mapArray[i][j].getAiInfoSymbol() == Symbol.SUCCESSFUL_HIT) {
                    shapeRenderer.setColor(new Color(0.929f, 0.741f, 0.243f, 1));
                    shapeRenderer.rectLine(j * Map.TILE_WIDTH, i * Map.TILE_HEIGHT, (j + 1) * Map.TILE_WIDTH, (i + 1) * Map.TILE_HEIGHT, Map.TILE_WIDTH / 10f);
                    shapeRenderer.rectLine((j + 1) * Map.TILE_WIDTH, i * Map.TILE_HEIGHT, j * Map.TILE_WIDTH, (i + 1) * Map.TILE_HEIGHT, Map.TILE_WIDTH / 10f);
                } else if (mapArray[i][j].getAiInfoSymbol() == Symbol.FAILED_HIT) {
                    shapeRenderer.setColor(Color.DARK_GRAY);
                    shapeRenderer.rectLine(j * Map.TILE_WIDTH, i * Map.TILE_HEIGHT, (j + 1) * Map.TILE_WIDTH, (i + 1) * Map.TILE_HEIGHT, Map.TILE_WIDTH / 10f);
                    shapeRenderer.rectLine((j + 1) * Map.TILE_WIDTH, i * Map.TILE_HEIGHT, j * Map.TILE_WIDTH, (i + 1) * Map.TILE_HEIGHT, Map.TILE_WIDTH / 10f);
                }
                // Draw player attacks
                if (mapArray[i][j].getPlayerInfoSymbol() == Symbol.SUCCESSFUL_HIT) {
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.circle(j * Map.TILE_WIDTH + Map.TILE_WIDTH / 2f, i * Map.TILE_HEIGHT + Map.TILE_HEIGHT / 2f, Map.TILE_WIDTH / 4f, Map.CIRCLE_EDGES);
                } else if (mapArray[i][j].getPlayerInfoSymbol() == Symbol.FAILED_HIT) {
                    shapeRenderer.setColor(Color.GREEN);
                    shapeRenderer.circle(j * Map.TILE_WIDTH + Map.TILE_WIDTH / 2f, i * Map.TILE_HEIGHT + Map.TILE_HEIGHT / 2f, Map.TILE_WIDTH / 6f, Map.CIRCLE_EDGES);
                }
            }
        }
        shapeRenderer.end();

        // Draw lines between tiles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        for (int i = 0; i < rowsAmount; i++) {
            for (int j = 0; j < columnsAmount; j++) {
                shapeRenderer.rect(j * Map.TILE_WIDTH, i * Map.TILE_HEIGHT, Map.TILE_WIDTH, Map.TILE_HEIGHT);
            }
        }
        shapeRenderer.end();
    }

    @Override
    public void show() {
        // Set all tiles to empty
        for (int i = 0; i < rowsAmount; i++) {
            for (int j = 0; j < columnsAmount; j++) {
                this.mapArray[i][j].clear();
            }
        }
        // Populate map tiles with player ship positions
        for (Ship currentShip : currentReplay.getPlayerShipsList()) {
            for (Pair currentTileIndices : currentShip.calculateShipTileIndices()) {
                this.mapArray[currentTileIndices.getIndexY()][currentTileIndices.getIndexX()].setTileType(TileType.SHIP);
            }
        }
    }

    @Override
    public void render(float delta) {
        // Logical updates
        update(delta);

        // Graphical updates
        updateGraphics(shipsGame.shapeRenderer);

        // Handle input
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            shipsGame.activateMenuScreen();
        }
    }

    @Override
    public void dispose() {
        System.out.println("dispose - ScreenReplayer");
    }
}