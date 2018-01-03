package com.shipsgame.map;

import com.shipsgame.Ship;
import com.shipsgame.screens.ScreenGame;
import com.shipsgame.utilities.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapCreator implements Serializable {

    private Map map;
    private List<Integer> availableShipsSizes;
    private boolean finished;
    private boolean shipBeginningPlaced;
    private Pair currentShipBeginningTileIndices;

    public MapCreator(Map map) {
        this.map = map;
        this.finished = false;
        this.shipBeginningPlaced = false;
        // Ship sizes and their amount to be used in a game
        this.availableShipsSizes = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 2, 2, 2, 3, 3, 4));
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Update this map after considering new mouse click
     *
     * @param tileIndices indices of a tile where the mouse was located
     * @param screenGame screen witch holds your map and is the main game screen
     */
    public void update(Pair tileIndices, ScreenGame screenGame) {
        if (shipBeginningPlaced) {
            // Create real ship and place it on the map
            if (map.getTile(tileIndices).getTileType() == TileType.TEMPLATE_SHIP_END) {
                int startIndexX = Math.min(currentShipBeginningTileIndices.getIndexX(), tileIndices.getIndexX());
                int startIndexY = Math.min(currentShipBeginningTileIndices.getIndexY(), tileIndices.getIndexY());
                int endIndexX = Math.max(currentShipBeginningTileIndices.getIndexX(), tileIndices.getIndexX());
                int endIndexY = Math.max(currentShipBeginningTileIndices.getIndexY(), tileIndices.getIndexY());
                map.addShip(new Ship(startIndexX, startIndexY, endIndexX, endIndexY));
                cleanTemplatesOnMap();

                finalizeCreatingShip();
            }
        } else {
            if (map.getTile(tileIndices).getTileType() == TileType.EMPTY && checkIfSurroundingTilesAreEmpty(tileIndices)) {
                placeShipBeginning(tileIndices);
            }
        }

        if (this.finished) {
            map.getReplayInConstruction().setPlayerShipsList(map.getShips());
            screenGame.displayMessage("Game started. Click mouse to attack chosen tile.\nPress h for help.");
        }
    }

    /**
     * Changes tile to TileType.TEMPLATE_SHIP_BEGINNING and if necessary calls placePossibleShipsEndings
     *
     * @param tileIndices represents indexX and indexY of tile where the ship beginning is placed
     */
    private void placeShipBeginning(Pair tileIndices) {
        // If current ship to build has size equal to 1, build it immediately
        if (getCurrentShipSize() == 1) {
            map.addShip(new Ship(tileIndices.getIndexX(), tileIndices.getIndexY()));
            finalizeCreatingShip();
        } else {
            currentShipBeginningTileIndices = tileIndices;
            if (placePossibleShipsEndings(tileIndices) > 0) {
                Tile selectedTile = map.getTile(tileIndices.getIndexX(), tileIndices.getIndexY());
                selectedTile.setTileType(TileType.TEMPLATE_SHIP_BEGINNING);
                shipBeginningPlaced = true;
            }
        }
    }

    /**
     * Changes all tiles that can be used to create valid ship to TileType.TEMPLATE_SHIP_ENDING
     *
     * @param tileIndices indices of a tile that was used as ship beginning
     * @return amount of possible endings
     */
    private int placePossibleShipsEndings(Pair tileIndices) {
        int startIndexX = tileIndices.getIndexX();
        int startIndexY = tileIndices.getIndexY();
        int amountOfEndingsPossible = 0;

        List<Pair> shipsEndingsIndices = new ArrayList<>();
        int endIndexX, endIndexY;
        if ((endIndexX = startIndexX - getCurrentShipSize() + 1) >= 0) {
            if (map.getTile(endIndexX, startIndexY).getTileType() == TileType.EMPTY
                    && checkIfSurroundingTilesAreEmpty(new Pair(endIndexX, startIndexY))
                    && checkIfLineBetweenTilesIsEmpty(new Pair(endIndexX, startIndexY), currentShipBeginningTileIndices)) {
                shipsEndingsIndices.add(new Pair(endIndexX, startIndexY));
                amountOfEndingsPossible++;
            }
        }
        if ((endIndexX = startIndexX + getCurrentShipSize() - 1) < Map.COLUMNS_AMOUNT) {
            if (map.getTile(endIndexX, startIndexY).getTileType() == TileType.EMPTY
                    && checkIfSurroundingTilesAreEmpty(new Pair(endIndexX, startIndexY))
                    && checkIfLineBetweenTilesIsEmpty(new Pair(endIndexX, startIndexY), currentShipBeginningTileIndices)) {
                shipsEndingsIndices.add(new Pair(endIndexX, startIndexY));
                amountOfEndingsPossible++;
            }
        }
        if ((endIndexY = startIndexY - getCurrentShipSize() + 1) >= 0) {
            if (map.getTile(startIndexX, endIndexY).getTileType() == TileType.EMPTY
                    && checkIfSurroundingTilesAreEmpty(new Pair(startIndexX, endIndexY))
                    && checkIfLineBetweenTilesIsEmpty(new Pair(startIndexX, endIndexY), currentShipBeginningTileIndices)) {
                shipsEndingsIndices.add(new Pair(startIndexX, endIndexY));
                amountOfEndingsPossible++;
            }
        }
        if ((endIndexY = startIndexY + getCurrentShipSize() - 1) < Map.ROWS_AMOUNT) {
            if (map.getTile(startIndexX, endIndexY).getTileType() == TileType.EMPTY
                    && checkIfSurroundingTilesAreEmpty(new Pair(startIndexX, endIndexY))
                    && checkIfLineBetweenTilesIsEmpty(new Pair(startIndexX, endIndexY), currentShipBeginningTileIndices)) {
                shipsEndingsIndices.add(new Pair(startIndexX, endIndexY));
                amountOfEndingsPossible++;
            }
        }

        for (Pair currentPair : shipsEndingsIndices) {
            map.getTile(currentPair.getIndexX(), currentPair.getIndexY()).setTileType(TileType.TEMPLATE_SHIP_END);
        }
        return amountOfEndingsPossible;
    }

    /**
     * @return ship size of the current building ship from availableShipsSizes
     */
    private int getCurrentShipSize() {
        return availableShipsSizes.get(availableShipsSizes.size() - 1);
    }

    /**
     * Transforms all template tiles (TileType.TEMPLATE_SHIP_BEGINNING or TileType.TEMPLATE_SHIP_END) left after building ship to empty tiles.
     */
    private void cleanTemplatesOnMap() {
        for (int i = 0; i < Map.ROWS_AMOUNT; i++) {
            for (int j = 0; j < Map.COLUMNS_AMOUNT; j++) {
                if (map.getTile(j, i).getTileType() == TileType.TEMPLATE_SHIP_END
                        || map.getTile(j, i).getTileType() == TileType.TEMPLATE_SHIP_BEGINNING) {
                    map.getTile(j, i).setTileType(TileType.EMPTY);
                }
            }
        }
    }

    /**
     * Updates all necessary variables to correctly finish a process of creating new ship on the map.
     * Has to be always used after creating a ship.
     */
    private void finalizeCreatingShip() {
        this.shipBeginningPlaced = false;
        removeCurrentShipSize();
        if (availableShipsSizes.size() == 0) {
            this.finished = true;
        }
    }

    /**
     * Removes size of the constructed ship from the pool of available sizes.
     */
    private void removeCurrentShipSize() {
        availableShipsSizes.remove(availableShipsSizes.size() - 1);
    }

    /**
     * @param tileIndices indices x and y of the tile being checked
     * @return true if the tile is not touching any ships or false otherwise
     */
    private boolean checkIfSurroundingTilesAreEmpty(Pair tileIndices) {
        int indexX = tileIndices.getIndexX();
        int indexY = tileIndices.getIndexY();
        //noinspection RedundantIfStatement
        if ((map.getTile(indexX + 1, indexY) != null && map.getTile(indexX + 1, indexY).getTileType() == TileType.SHIP)
                || (map.getTile(indexX + 1, indexY + 1) != null && map.getTile(indexX + 1, indexY + 1).getTileType() == TileType.SHIP)
                || (map.getTile(indexX, indexY + 1) != null && map.getTile(indexX, indexY + 1).getTileType() == TileType.SHIP)
                || (map.getTile(indexX - 1, indexY + 1) != null && map.getTile(indexX - 1, indexY + 1).getTileType() == TileType.SHIP)
                || (map.getTile(indexX - 1, indexY) != null && map.getTile(indexX - 1, indexY).getTileType() == TileType.SHIP)
                || (map.getTile(indexX - 1, indexY - 1) != null && map.getTile(indexX - 1, indexY - 1).getTileType() == TileType.SHIP)
                || (map.getTile(indexX, indexY - 1) != null && map.getTile(indexX, indexY - 1).getTileType() == TileType.SHIP)
                || (map.getTile(indexX + 1, indexY - 1) != null && map.getTile(indexX + 1, indexY - 1).getTileType() == TileType.SHIP)) {
            return false;
        }
        return true;
    }

    /**
     * @param startTileIndices indices x and y of the beginning tile
     * @param endTileIndices   indices x andy y of the ending tile
     * @return true if space between tiles is empty or false otherwise
     */
    private boolean checkIfLineBetweenTilesIsEmpty(Pair startTileIndices, Pair endTileIndices) {
        int startIndexX = Math.min(startTileIndices.getIndexX(), endTileIndices.getIndexX());
        int startIndexY = Math.min(startTileIndices.getIndexY(), endTileIndices.getIndexY());
        int endIndexX = Math.max(endTileIndices.getIndexX(), startTileIndices.getIndexX());
        int endIndexY = Math.max(endTileIndices.getIndexY(), startTileIndices.getIndexY());

        if (startIndexX == endIndexX) {
            for (int i = startIndexY + 1; i < endIndexY; i++) {
                if (!checkIfSurroundingTilesAreEmpty(new Pair(startIndexX, i))) {
                    return false;
                }
            }
        } else if (startIndexY == endIndexY) {
            for (int i = startIndexX + 1; i < endIndexX; i++) {
                if (!checkIfSurroundingTilesAreEmpty(new Pair(i, startIndexY))) {
                    return false;
                }
            }
        }
        return true;
    }
}