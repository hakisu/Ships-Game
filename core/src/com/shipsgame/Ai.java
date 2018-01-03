package com.shipsgame;

import com.shipsgame.map.Map;
import com.shipsgame.map.Symbol;
import com.shipsgame.map.TileType;
import com.shipsgame.replays.Action;
import com.shipsgame.replays.ActionType;
import com.shipsgame.utilities.Direction;
import com.shipsgame.utilities.Pair;

import java.io.Serializable;
import java.util.*;

public class Ai implements Serializable {

    private static final int MAX_AMOUNT_OF_RANDOMIZING_DIRECTION_LOOPS = 10;

    // Reference to player map
    private Map map;

    private Random randomNumberGenerator;
    private List<Pair> availablePlayerTilesIndices;
    private List<Ship> ships;
    // Fields used by ai to "intelligently" choose next tile to attack
    private boolean justHitShip;
    private List<Pair> attackedShipIndices;
    private Direction attackedShipDirection;
    private List<Direction> checkedDirectionsForAttack;
    private boolean checkBackwardsAttackedShip;
    private boolean shipDirectionConfirmed;
    private boolean checkLongAttackedShipBackwards;

    public Ai(Map map) {
        this.map = map;
        this.ships = new ArrayList<>();
        this.randomNumberGenerator = new Random();
        this.availablePlayerTilesIndices = new ArrayList<>();
        populateListWithMapTileIndices(this.availablePlayerTilesIndices);
        this.justHitShip = false;
        attackedShipIndices = new ArrayList<>();
        attackedShipDirection = null;
        checkedDirectionsForAttack = new ArrayList<>();
        checkBackwardsAttackedShip = false;
        shipDirectionConfirmed = false;
        checkLongAttackedShipBackwards = false;

        generateShipsPosition();
    }

    /**
     * Handle player attack at the given tileIndices and respond with own shot.
     *
     * @param tileIndices indices of a tile where the mouse was located
     * @return indices of a tile chosen by ai to attack
     */
    public Pair update(Pair tileIndices) {
        // Process player shot
        boolean successfulHit = false;
        Ship shipToDestroy = null;

        Iterator<Ship> shipsIterator = ships.iterator();
        while (shipsIterator.hasNext()) {
            Ship currentShip = shipsIterator.next();
            if (currentShip.containsTile(tileIndices)) {
                currentShip.damageShipPart(tileIndices);
                if (currentShip.isShipFullyDestroyed()) {
                    shipToDestroy = currentShip;
                    shipsIterator.remove();
                }
                successfulHit = true;
                break;
            }
        }
        if (shipToDestroy != null) {
            List<Pair> shipTileIndices = shipToDestroy.calculateShipTileIndices();
            for (Pair currentShipTileIndices : shipTileIndices) {
                map.getTile(currentShipTileIndices).setPlayerInfoSymbol(Symbol.DESTROYED);
            }
            // Used for creating game replay
            map.getReplayInConstruction().addAction(new Action(ActionType.PLAYER_SHOT, tileIndices));
        } else if (successfulHit) {
            map.getTile(tileIndices).setPlayerInfoSymbol(Symbol.SUCCESSFUL_HIT);
            // Used for creating game replay
            map.getReplayInConstruction().addAction(new Action(ActionType.PLAYER_SHOT, tileIndices));
        } else {
            map.getTile(tileIndices).setPlayerInfoSymbol(Symbol.FAILED_HIT);
            // Used for creating game replay
            map.getReplayInConstruction().addAction(new Action(ActionType.PLAYER_MISS_SHOT, tileIndices));
        }

        // Process ai attack
        Pair chosenTileIndices = null;
        if (!justHitShip) {
            chosenTileIndices = getRandomPlayerTileIndices();
        } else {
            Pair previousTileIndices;
            if (checkBackwardsAttackedShip && !checkLongAttackedShipBackwards) {
                previousTileIndices = attackedShipIndices.get(0);
            } else {
                previousTileIndices = attackedShipIndices.get(attackedShipIndices.size() - 1);
            }
            List<Direction> validDirectionsForAttack = new ArrayList<>();

            if (attackedShipDirection == null) {
                if (previousTileIndices.getIndexX() > 0) {
                    if (!checkedDirectionsForAttack.contains(Direction.WEST)
                            && availablePlayerTilesIndices.contains(new Pair(previousTileIndices.getIndexX() - 1, previousTileIndices.getIndexY()))) {
                        validDirectionsForAttack.add(Direction.WEST);
                    }
                }
                if (previousTileIndices.getIndexX() < Map.COLUMNS_AMOUNT - 1) {
                    if (!checkedDirectionsForAttack.contains(Direction.EAST)
                            && availablePlayerTilesIndices.contains(new Pair(previousTileIndices.getIndexX() + 1, previousTileIndices.getIndexY()))) {
                        validDirectionsForAttack.add(Direction.EAST);
                    }
                }
                if (previousTileIndices.getIndexY() > 0) {
                    if (!checkedDirectionsForAttack.contains(Direction.NORTH)
                            && availablePlayerTilesIndices.contains(new Pair(previousTileIndices.getIndexX(), previousTileIndices.getIndexY() - 1))) {
                        validDirectionsForAttack.add(Direction.NORTH);
                    }
                }
                if (previousTileIndices.getIndexY() < Map.ROWS_AMOUNT - 1) {
                    if (!checkedDirectionsForAttack.contains(Direction.SOUTH)
                            && availablePlayerTilesIndices.contains(new Pair(previousTileIndices.getIndexX(), previousTileIndices.getIndexY() + 1))) {
                        validDirectionsForAttack.add(Direction.SOUTH);
                    }
                }
                attackedShipDirection = validDirectionsForAttack.get(randomNumberGenerator.nextInt(validDirectionsForAttack.size()));
                checkedDirectionsForAttack.add(attackedShipDirection);
            }
            switch (attackedShipDirection) {
                case EAST:
                    chosenTileIndices = new Pair(previousTileIndices.getIndexX() + 1, previousTileIndices.getIndexY());
                    break;
                case SOUTH:
                    chosenTileIndices = new Pair(previousTileIndices.getIndexX(), previousTileIndices.getIndexY() + 1);
                    break;
                case WEST:
                    chosenTileIndices = new Pair(previousTileIndices.getIndexX() - 1, previousTileIndices.getIndexY());
                    break;
                case NORTH:
                    chosenTileIndices = new Pair(previousTileIndices.getIndexX(), previousTileIndices.getIndexY() - 1);
                    break;
            }
        }

        Symbol attackStatus = map.getStatusInfoFromTile(chosenTileIndices);
        if (attackStatus == Symbol.FAILED_HIT) {
            if (justHitShip) {
                if (shipDirectionConfirmed) {
                    checkBackwardsAttackedShip = true;
                }
                if (checkBackwardsAttackedShip) {
                    attackedShipDirection = Direction.getOppositeDirection(attackedShipDirection);
                } else {
                    attackedShipDirection = null;
                }
            } else {
                justHitShip = false;
            }
            // Used for creating game replay
            map.getReplayInConstruction().addAction(new Action(ActionType.AI_MISS_SHOT, chosenTileIndices));
        } else if (attackStatus == Symbol.DESTROYED) {
            justHitShip = false;
            checkBackwardsAttackedShip = false;
            shipDirectionConfirmed = false;
            checkedDirectionsForAttack.clear();
            attackedShipIndices.add(chosenTileIndices);
            // Remove all tile indices from availablePlayerTilesIndices that surrounded attackedShipIndices
            for (Pair currentShipTileIndices : attackedShipIndices) {
                availablePlayerTilesIndices.removeAll(createListOfNeighbourTileIndices(currentShipTileIndices));
            }
            attackedShipIndices.clear();
            attackedShipDirection = null;
            checkLongAttackedShipBackwards = false;
            // Used for creating game replay
            map.getReplayInConstruction().addAction(new Action(ActionType.AI_SHOT, chosenTileIndices));
        } else if (attackStatus == Symbol.SUCCESSFUL_HIT) {
            attackedShipIndices.add(chosenTileIndices);
            if (justHitShip) {
                shipDirectionConfirmed = true;
            }
            justHitShip = true;
            // In case of a long ship when ai starts shooting in reverse
            if (checkBackwardsAttackedShip) {
                checkLongAttackedShipBackwards = true;
            }
            // Check if ai is not attacking beyond map bounds or unavailable tile
            if (attackedShipDirection != null) {
                Pair nextTileIndicesToVerify = null;
                if (attackedShipDirection == Direction.EAST) {
                    nextTileIndicesToVerify = new Pair(chosenTileIndices.getIndexX() + 1, chosenTileIndices.getIndexY());
                } else if (attackedShipDirection == Direction.SOUTH) {
                    nextTileIndicesToVerify = new Pair(chosenTileIndices.getIndexX(), chosenTileIndices.getIndexY() + 1);
                } else if (attackedShipDirection == Direction.WEST) {
                    nextTileIndicesToVerify = new Pair(chosenTileIndices.getIndexX() - 1, chosenTileIndices.getIndexY());
                } else if (attackedShipDirection == Direction.NORTH) {
                    nextTileIndicesToVerify = new Pair(chosenTileIndices.getIndexX(), chosenTileIndices.getIndexY() - 1);
                }

                if (!availablePlayerTilesIndices.contains(nextTileIndicesToVerify)) {
                    checkBackwardsAttackedShip = true;
                    attackedShipDirection = Direction.getOppositeDirection(attackedShipDirection);
                }
            }
            // Used for creating game replay
            map.getReplayInConstruction().addAction(new Action(ActionType.AI_SHOT, chosenTileIndices));
        }

        // Return tile indices chosen by the ai to attack player
        return chosenTileIndices;
    }

    /**
     * Get a random tile indices from the list of available ones and remove them afterwards.
     *
     * @return a random pair of tile indices from a pool of available ones
     */
    private Pair getRandomPlayerTileIndices() {
        int randomIndex = randomNumberGenerator.nextInt(availablePlayerTilesIndices.size());
        Pair randomTileIndices = availablePlayerTilesIndices.get(randomIndex);
        availablePlayerTilesIndices.remove(randomIndex);
        return randomTileIndices;
    }

    /**
     * Choose random, valid positions for ai ships and create them
     */
    private void generateShipsPosition() {
        List<Integer> availableShipsSizes = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 2, 2, 2, 3, 3, 4));
        List<Pair> availableAiTileIndices = new ArrayList<>(Map.ROWS_AMOUNT * Map.COLUMNS_AMOUNT);
        TileType[][] tempAiMap = new TileType[Map.ROWS_AMOUNT][Map.COLUMNS_AMOUNT];
        for (int i = 0; i < tempAiMap.length; i++) {
            for (int j = 0; j < tempAiMap[0].length; j++) {
                tempAiMap[i][j] = TileType.EMPTY;
            }
        }
        // Available tile indices for choosing random tile for ship beginning
        populateListWithMapTileIndices(availableAiTileIndices);

        while (availableShipsSizes.size() > 0) {
            if (availableAiTileIndices.size() <= 0) {
                throw new AiPlacingShipsException();
            }
            int randomIndex = randomNumberGenerator.nextInt(availableAiTileIndices.size());
            Pair randomTileIndices = availableAiTileIndices.get(randomIndex);
            int currentTileIndexX = randomTileIndices.getIndexX();
            int currentTileIndexY = randomTileIndices.getIndexY();
            int currentShipSize = availableShipsSizes.get(availableShipsSizes.size() - 1);

            int directionsLoopCounter = 0;
            boolean canCreateShip = true;
            randomizingDirectionsLoop:
            while (directionsLoopCounter < MAX_AMOUNT_OF_RANDOMIZING_DIRECTION_LOOPS) {
                directionsLoopCounter++;
                Direction currentDirection = Direction.getRandomDirection();
                switch (currentDirection) {
                    case EAST:
                        if (currentTileIndexX + currentShipSize - 1 < Map.COLUMNS_AMOUNT) {
                            for (int i = currentTileIndexX; i < currentTileIndexX + currentShipSize; i++) {
                                if (tempAiMap[currentTileIndexY][i] == TileType.SHIP
                                        || (i + 1 < Map.COLUMNS_AMOUNT && tempAiMap[currentTileIndexY][i + 1] == TileType.SHIP)
                                        || !availableAiTileIndices.contains(new Pair(i, currentTileIndexY))) {
                                    canCreateShip = false;
                                    break;
                                }
                            }
                            if (canCreateShip) {
                                Ship newShip = new Ship(currentTileIndexX, currentTileIndexY, currentTileIndexX + currentShipSize - 1, currentTileIndexY);
                                ships.add(newShip);
                                break randomizingDirectionsLoop;
                            }
                        }
                    case SOUTH:
                        if (currentTileIndexY + currentShipSize - 1 < Map.ROWS_AMOUNT) {
                            for (int i = currentTileIndexY; i < currentTileIndexY + currentShipSize; i++) {
                                if (tempAiMap[i][currentTileIndexX] == TileType.SHIP
                                        || (i + 1 < Map.ROWS_AMOUNT && tempAiMap[i + 1][currentTileIndexX] == TileType.SHIP)
                                        || !availableAiTileIndices.contains(new Pair(currentTileIndexX, i))) {
                                    canCreateShip = false;
                                    break;
                                }
                            }
                            if (canCreateShip) {
                                Ship newShip = new Ship(currentTileIndexX, currentTileIndexY, currentTileIndexX, currentTileIndexY + currentShipSize - 1);
                                ships.add(newShip);
                                break randomizingDirectionsLoop;
                            }
                        }
                    case WEST:
                        if (currentTileIndexX - currentShipSize + 1 >= 0) {
                            for (int i = currentTileIndexX - currentShipSize + 1; i <= currentTileIndexX; i++) {
                                if (tempAiMap[currentTileIndexY][i] == TileType.SHIP
                                        || (i - 1 >= 0 && tempAiMap[currentTileIndexY][i - 1] == TileType.SHIP)
                                        || !availableAiTileIndices.contains(new Pair(i, currentTileIndexY))) {
                                    canCreateShip = false;
                                    break;
                                }
                            }
                            if (canCreateShip) {
                                Ship newShip = new Ship(currentTileIndexX - currentShipSize + 1, currentTileIndexY, currentTileIndexX, currentTileIndexY);
                                ships.add(newShip);
                                break randomizingDirectionsLoop;
                            }
                        }
                    case NORTH:
                        if (currentTileIndexY - currentShipSize + 1 >= 0) {
                            for (int i = currentTileIndexY - (currentShipSize - 1); i <= currentTileIndexY; i++) {
                                if (tempAiMap[i][currentTileIndexX] == TileType.SHIP
                                        || (i - 1 >= 0 && tempAiMap[i - 1][currentTileIndexX] == TileType.SHIP)
                                        || !availableAiTileIndices.contains(new Pair(currentTileIndexX, i))) {
                                    canCreateShip = false;
                                    break;
                                }
                            }
                            if (canCreateShip) {
                                Ship newShip = new Ship(currentTileIndexX, currentTileIndexY - currentShipSize + 1, currentTileIndexX, currentTileIndexY);
                                ships.add(newShip);
                                break randomizingDirectionsLoop;
                            }
                        }
                }
            }
            // If new ship was constructed, remove its size from the availableShipSizes,
            // build new map and remove tile indices no longer available for placing ships on
            if (canCreateShip) {
                availableShipsSizes.remove(availableShipsSizes.size() - 1);
                copyShipsToArray(tempAiMap);
                removeShipSurroundingIndicesFromList(ships.get(ships.size() - 1), availableAiTileIndices);
            }
        }

        // Temp to see if ships were placed correctly by the ai
        for (int i = 0; i < tempAiMap.length; i++) {
            for (int j = 0; j < tempAiMap[0].length; j++) {
                if (tempAiMap[i][j] == TileType.EMPTY) {
                    System.out.print(0 + " ");
                } else {
                    System.out.print(1 + " ");
                }
            }
            System.out.println("");
        }
        System.out.println("\n\n");
    }

    /**
     * Add to tilesIndicesList all tiles indices from the map
     *
     * @param tilesIndicesList empty list of tiles indices
     */
    private void populateListWithMapTileIndices(List<Pair> tilesIndicesList) {
        for (int i = 0; i < Map.ROWS_AMOUNT; i++) {
            for (int j = 0; j < Map.COLUMNS_AMOUNT; j++) {
                tilesIndicesList.add(new Pair(j, i));
            }
        }
    }

    /**
     * Go through all ships and copy their positions to shipsMap
     *
     * @param shipsMap 2d array of TileType representing map for ai
     */
    private void copyShipsToArray(TileType[][] shipsMap) {
        for (Ship currentShip : ships) {
            for (Pair currentTileIndices : currentShip.calculateShipTileIndices()) {
                shipsMap[currentTileIndices.getIndexY()][currentTileIndices.getIndexX()] = TileType.SHIP;
            }
        }
    }

    /**
     * Removes indices surrounding ship from availableAiTileIndices, to keep ships separated from each other.
     *
     * @param ship                   ship object, that we want to remove surrounding tile indices from
     * @param availableAiTileIndices list of available tile indices on the map
     */
    private void removeShipSurroundingIndicesFromList(Ship ship, List<Pair> availableAiTileIndices) {
        List<Pair> shipTilesIndices = ship.calculateShipTileIndices();
        for (Pair currentTileIndices : shipTilesIndices) {
            availableAiTileIndices.remove(currentTileIndices);
            availableAiTileIndices.remove(new Pair(currentTileIndices.getIndexX() + 1, currentTileIndices.getIndexY()));
            availableAiTileIndices.remove(new Pair(currentTileIndices.getIndexX() + 1, currentTileIndices.getIndexY() + 1));
            availableAiTileIndices.remove(new Pair(currentTileIndices.getIndexX(), currentTileIndices.getIndexY() + 1));
            availableAiTileIndices.remove(new Pair(currentTileIndices.getIndexX() - 1, currentTileIndices.getIndexY() + 1));
            availableAiTileIndices.remove(new Pair(currentTileIndices.getIndexX() - 1, currentTileIndices.getIndexY()));
            availableAiTileIndices.remove(new Pair(currentTileIndices.getIndexX() - 1, currentTileIndices.getIndexY() - 1));
            availableAiTileIndices.remove(new Pair(currentTileIndices.getIndexX(), currentTileIndices.getIndexY() - 1));
            availableAiTileIndices.remove(new Pair(currentTileIndices.getIndexX() + 1, currentTileIndices.getIndexY() - 1));
        }
    }

    /**
     * Used to check if game is over for the ai.
     *
     * @return true if ai lost all ships
     */
    public boolean didAiLose() {
        return ships.size() <= 0;
    }

    /**
     * Creates an arrayList of tile indices that surround tile described by tileIndices. Always returns list with size 8.
     *
     * @param tileIndices indices of the tile for which we want to get neighbours
     * @return list of tile indices that are neighbours to our tile
     */
    private List<Pair> createListOfNeighbourTileIndices(Pair tileIndices) {
        int indexX = tileIndices.getIndexX();
        int indexY = tileIndices.getIndexY();

        List<Pair> neighbourTilesIndices = new ArrayList<>();
        neighbourTilesIndices.add(new Pair(indexX + 1, indexY));
        neighbourTilesIndices.add(new Pair(indexX + 1, indexY + 1));
        neighbourTilesIndices.add(new Pair(indexX, indexY + 1));
        neighbourTilesIndices.add(new Pair(indexX - 1, indexY + 1));
        neighbourTilesIndices.add(new Pair(indexX - 1, indexY));
        neighbourTilesIndices.add(new Pair(indexX - 1, indexY - 1));
        neighbourTilesIndices.add(new Pair(indexX, indexY - 1));
        neighbourTilesIndices.add(new Pair(indexX + 1, indexY - 1));

        return neighbourTilesIndices;
    }
}