package com.shipsgame.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.shipsgame.Ai;
import com.shipsgame.PlayerProfile;
import com.shipsgame.Ship;
import com.shipsgame.replays.Replay;
import com.shipsgame.screens.ScreenGame;
import com.shipsgame.utilities.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Map implements Serializable {

    public static final int ROWS_AMOUNT = 10;
    public static final int COLUMNS_AMOUNT = 10;
    public static final int TILE_WIDTH = 1;
    public static final int TILE_HEIGHT = 1;
    // Const value used internally for drawing smooth circles
    public static final int CIRCLE_EDGES = 50;
    private static final boolean SHOW_ONLY_RECENT_AI_MOVE = true;

    private int rowsAmount;
    private int columnsAmount;
    private Tile[][] mapArray;
    private MapCreator mapCreator;
    private ArrayList<Ship> ships;
    private Ai ai;
    private Replay replayInConstruction;

    public Map() {
        this.rowsAmount = ROWS_AMOUNT;
        this.columnsAmount = COLUMNS_AMOUNT;
        this.mapArray = new Tile[rowsAmount][columnsAmount];
        this.ships = new ArrayList<>();
        this.mapCreator = new MapCreator(this);
        this.ai = new Ai(this);
        this.replayInConstruction = new Replay();

        // Initialize empty tiles for mapArray
        for (int i = 0; i < rowsAmount; i++) {
            for (int j = 0; j < columnsAmount; j++) {
                this.mapArray[i][j] = new Tile();
            }
        }
    }

    public Replay getReplayInConstruction() {
        return replayInConstruction;
    }

    public ArrayList<Ship> getShips() {
        return ships;
    }

    /**
     * Checks if player finished placing all his ships on the map.
     *
     * @return true if player finished placing his ships
     */
    public boolean isMapCreatorFinished() {
        return mapCreator.isFinished();
    }

    /**
     * Update logic and state of map after handling user mouse click
     *
     * @param tileIndexX horizontal index of tile clicked
     * @param tileIndexY vertical index of tile clicked
     */
    public void update(int tileIndexX, int tileIndexY, ScreenGame screenGame, PlayerProfile playerProfile) {
        // Update mapCreator until it fully finished placing ships on the map
        if (!mapCreator.isFinished()) {
            mapCreator.update(new Pair(tileIndexX, tileIndexY), screenGame);
        } else {
            // Process player click input if it chosen tile wasn't used in the past
            if (this.mapArray[tileIndexY][tileIndexX].getPlayerInfoSymbol() == Symbol.EMPTY) {
                // Clean map from previous, failed ai hits
                if (SHOW_ONLY_RECENT_AI_MOVE) {
                    cleanMapFromAiHits();
                }
                Pair attackedTileIndices = ai.update(new Pair(tileIndexX, tileIndexY));

                // Process tile attacked by ai
                for (Ship currentShip : this.ships) {
                    if (currentShip.damageShipPart(attackedTileIndices)) {
                        this.getTile(attackedTileIndices).setAiInfoSymbol(Symbol.SUCCESSFUL_HIT);
                        break;
                    } else {
                        this.getTile(attackedTileIndices).setAiInfoSymbol(Symbol.FAILED_HIT);
                    }
                }

                // Check if ai lost the game
                if (ai.didAiLose()) {
                    playerProfile.addWin();
                    screenGame.setMapInitialized(false);
                    screenGame.displayGameOver(true);
                }
                // Check if player lost the game
                if (areAllShipsDestroyed()) {
                    playerProfile.addLoss();
                    screenGame.setMapInitialized(false);
                    screenGame.displayGameOver(false);
                }
            }
        }
    }

    /**
     * Render map to the screen
     *
     * @param shapeRenderer used to draw shapes on the screen
     */
    public void render(ShapeRenderer shapeRenderer) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        // Draw all tiles of the map
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < rowsAmount; i++) {
            for (int j = 0; j < columnsAmount; j++) {
                if (mapArray[i][j].getTileType() == TileType.SHIP) {
                    shapeRenderer.setColor(Color.DARK_GRAY);
                } else if (mapArray[i][j].getTileType() == TileType.EMPTY) {
                    // Light blue color
                    shapeRenderer.setColor(new Color(0.004f, 0.569f, 0.784f, 1));
                } else if (mapArray[i][j].getTileType() == TileType.TEMPLATE_SHIP_BEGINNING) {
                    shapeRenderer.setColor(Color.YELLOW);
                } else if (mapArray[i][j].getTileType() == TileType.TEMPLATE_SHIP_END) {
                    shapeRenderer.setColor(Color.GREEN);
                }
                shapeRenderer.rect(j * TILE_WIDTH, i * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
            }
        }
        shapeRenderer.end();

        // Draw black lines to separate tiles from each other
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < rowsAmount; i++) {
            for (int j = 0; j < columnsAmount; j++) {
                shapeRenderer.setColor(new Color(0, 0, 0, 0.1f));
                shapeRenderer.rect(j * TILE_WIDTH, i * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
            }
        }
        shapeRenderer.end();

        // Draw symbols on the map
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < rowsAmount; i++) {
            for (int j = 0; j < columnsAmount; j++) {
                // Draw ai actions symbols
                if (mapArray[i][j].getAiInfoSymbol() == Symbol.SUCCESSFUL_HIT) {
                    shapeRenderer.setColor(new Color(0.929f, 0.741f, 0.243f, 1));
                    shapeRenderer.rectLine(j * TILE_WIDTH, i * TILE_HEIGHT, (j + 1) * TILE_WIDTH, (i + 1) * TILE_HEIGHT, TILE_WIDTH / 10f);
                    shapeRenderer.rectLine((j + 1) * TILE_WIDTH, i * TILE_HEIGHT, j * TILE_WIDTH, (i + 1) * TILE_HEIGHT, TILE_WIDTH / 10f);
                } else if (mapArray[i][j].getAiInfoSymbol() == Symbol.FAILED_HIT) {
                    shapeRenderer.setColor(Color.DARK_GRAY);
                    shapeRenderer.rectLine(j * TILE_WIDTH, i * TILE_HEIGHT, (j + 1) * TILE_WIDTH, (i + 1) * TILE_HEIGHT, TILE_WIDTH / 10f);
                    shapeRenderer.rectLine((j + 1) * TILE_WIDTH, i * TILE_HEIGHT, j * TILE_WIDTH, (i + 1) * TILE_HEIGHT, TILE_WIDTH / 10f);
                }
                // Draw player actions symbols
                if (mapArray[i][j].getPlayerInfoSymbol() == Symbol.SUCCESSFUL_HIT) {
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.circle(j * TILE_WIDTH + TILE_WIDTH / 2f, i * TILE_HEIGHT + TILE_HEIGHT / 2f, TILE_WIDTH / 4f, CIRCLE_EDGES);
                } else if (mapArray[i][j].getPlayerInfoSymbol() == Symbol.FAILED_HIT) {
                    shapeRenderer.setColor(Color.GREEN);
                    shapeRenderer.circle(j * TILE_WIDTH + TILE_WIDTH / 2f, i * TILE_HEIGHT + TILE_HEIGHT / 2f, TILE_WIDTH / 6f, CIRCLE_EDGES);
                } else if (mapArray[i][j].getPlayerInfoSymbol() == Symbol.DESTROYED) {
                    shapeRenderer.setColor(new Color(0, 0, 0, 1f));
                    shapeRenderer.circle(j * TILE_WIDTH + TILE_WIDTH / 2f, i * TILE_HEIGHT + TILE_HEIGHT / 2f, TILE_WIDTH / 6f, CIRCLE_EDGES);
                }
            }
        }
        shapeRenderer.end();
    }

    /**
     * Updates map tiles TileTypes values using the list of all ships that exist on the map.
     */
    private void updateTilesFromShips() {
        for (Ship currentShip : ships) {
            List<Pair> shipTileIndices = currentShip.calculateShipTileIndices();
            for (Pair currentPair : shipTileIndices) {
                mapArray[currentPair.getIndexY()][currentPair.getIndexX()].setTileType(TileType.SHIP);
            }
        }
    }

    /**
     * Return tile from map located at indexX and indexY.
     *
     * @param indexX horizontal index of tile
     * @param indexY vertical index of tile
     * @return Tile at given index or null if no tile exists there
     */
    public Tile getTile(int indexX, int indexY) {
        if (indexX >= this.columnsAmount || indexX < 0 ||
                indexY >= this.rowsAmount || indexY < 0) {
            return null;
        }
        return mapArray[indexY][indexX];
    }

    /**
     * Return tile from map located at tileIndices
     *
     * @param tileIndices indices of tile
     * @return Tile at given index or null if no tile exists there
     */
    public Tile getTile(Pair tileIndices) {
        return getTile(tileIndices.getIndexX(), tileIndices.getIndexY());
    }

    /**
     * Transform all map tiles in mapArray to TileType EMPTY.
     */
    private void cleanMap() {
        for (int i = 0; i < Map.ROWS_AMOUNT; i++) {
            for (int j = 0; j < Map.COLUMNS_AMOUNT; j++) {
                mapArray[i][j].setTileType(TileType.EMPTY);
            }
        }
    }

    /**
     * Add new ship to the list of all ships located on the map.
     *
     * @param ship ship added to ships
     */
    public void addShip(Ship ship) {
        this.ships.add(ship);
        cleanMap();
        updateTilesFromShips();
    }

    /**
     * Changes aiInfoSymbols in all map tiles to Symbol.EMPTY.
     */
    private void cleanMapFromAiHits() {
        for (int i = 0; i < Map.ROWS_AMOUNT; i++) {
            for (int j = 0; j < Map.COLUMNS_AMOUNT; j++) {
                if (mapArray[i][j].getAiInfoSymbol() == Symbol.FAILED_HIT) {
                    mapArray[i][j].setAiInfoSymbol(Symbol.EMPTY);
                }
            }
        }
    }

    /**
     * Check if player lost the game by losing all his ships.
     *
     * @return true if all ships are destroyed
     */
    private boolean areAllShipsDestroyed() {
        for (Ship currentShip : this.ships) {
            if (!currentShip.isShipFullyDestroyed()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check tile described by attackedTileIndices and return what happens if this tile is attacked.
     *
     * @param attackedTileIndices indices of a tile that is attacked
     * @return Symbol with value SUCCESSFUL_HIT if a ship was hit,
     * FAILED_HIT if there was no ship at given location,
     * DESTROYED if after attack a ship was completely destroyed
     */
    public Symbol getStatusInfoFromTile(Pair attackedTileIndices) {
        for (Ship currentShip : ships) {
            // Create deep copy of currentShip to test damaging it without modifying original ship
            Ship shipForStatusTest = new Ship(currentShip);
            if (shipForStatusTest.damageShipPart(attackedTileIndices)) {
                if (shipForStatusTest.isShipFullyDestroyed()) {
                    return Symbol.DESTROYED;
                } else {
                    return Symbol.SUCCESSFUL_HIT;
                }
            }
        }
        return Symbol.FAILED_HIT;
    }
}