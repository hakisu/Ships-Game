package com.shipsgame;

import com.shipsgame.map.Orientation;
import com.shipsgame.utilities.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Ship implements Serializable {

    private int shipSize;
    private int startX;
    private int startY;
    private List<Integer> goodShipParts;
    private Orientation orientation;

    public Ship(int startX, int startY, int endX, int endY) {
        this.startX = startX;
        this.startY = startY;
        shipSize = Math.abs(startX - endX) + Math.abs(startY - endY) + 1;
        if (startY == endY) {
            orientation = Orientation.HORIZONTAL;
        } else {
            orientation = Orientation.VERTICAL;
        }
        goodShipParts = new ArrayList<>(shipSize);
        for (int i = 0; i < shipSize; i++) {
            goodShipParts.add(i);
        }
    }

    @SuppressWarnings("IncompleteCopyConstructor")
    public Ship(Ship other) {
        this.shipSize = other.shipSize;
        this.startX = other.startX;
        this.startY = other.startY;
        this.goodShipParts = new ArrayList<>();
        this.goodShipParts.addAll(other.goodShipParts);
        this.orientation = other.orientation;
    }

    /**
     * Constructor for ship with size equal to 1
     *
     * @param indexX index of ship on X axis
     * @param indexY index of ship on Y axis
     */
    public Ship(int indexX, int indexY) {
        this(indexX, indexY, indexX, indexY);
    }

    /**
     * Calculate all tile indices from which the ship is built.
     *
     * @return list of tile indices which represent location of this ship
     */
    public List<Pair> calculateShipTileIndices() {
        List<Pair> shipTileIndices = new ArrayList<>();
        for (int i = 0; i < shipSize; i++) {
            if (orientation == Orientation.HORIZONTAL) {
                shipTileIndices.add(new Pair(startX + i, startY));
            } else {
                shipTileIndices.add(new Pair(startX, startY + i));
            }
        }
        return shipTileIndices;
    }

    /**
     * Check if tile with given indices belongs to this ship.
     *
     * @param tileIndices indices of tile to check
     * @return true if ships contains given tile described by tileIndices
     */
    public boolean containsTile(Pair tileIndices) {
        if (this.orientation == Orientation.HORIZONTAL) {
            return tileIndices.getIndexY() == this.startY
                    && tileIndices.getIndexX() >= this.startX
                    && tileIndices.getIndexX() < this.startX + this.shipSize;
        } else {
            return tileIndices.getIndexX() == this.startX
                    && tileIndices.getIndexY() >= this.startY
                    && tileIndices.getIndexY() < this.startY + this.shipSize;
        }
    }

    /**
     * Damages ship by destroying its goodShipPart described by shipPartIndices.
     *
     * @param shipPartIndices indices of tile to damage
     * @return true if ship was damaged or false otherwise
     */
    public boolean damageShipPart(Pair shipPartIndices) {
        if (this.containsTile(shipPartIndices)) {
            int shipPartIndexInShip;
            if (this.orientation == Orientation.HORIZONTAL) {
                shipPartIndexInShip = shipPartIndices.getIndexX() - this.startX;
                goodShipParts.remove(new Integer(shipPartIndexInShip));
            } else {
                shipPartIndexInShip = shipPartIndices.getIndexY() - this.startY;
            }
            goodShipParts.remove(new Integer(shipPartIndexInShip));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if all goodShipParts of this ship were destroyed.
     *
     * @return true if ship has no more goodShipParts
     */
    public boolean isShipFullyDestroyed() {
        return this.goodShipParts.size() <= 0;
    }
}
