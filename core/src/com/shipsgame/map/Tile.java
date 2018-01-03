package com.shipsgame.map;

import java.io.Serializable;

public class Tile implements Serializable {

    private TileType tileType;
    private Symbol playerInfoSymbol;
    private Symbol aiInfoSymbol;

    public Tile() {
        this.tileType = TileType.EMPTY;
        this.playerInfoSymbol = Symbol.EMPTY;
        this.aiInfoSymbol= Symbol.EMPTY;
    }

    public Symbol getAiInfoSymbol() {
        return aiInfoSymbol;
    }

    public void setAiInfoSymbol(Symbol aiInfoSymbol) {
        this.aiInfoSymbol = aiInfoSymbol;
    }

    public TileType getTileType() {
        return tileType;
    }

    public void setTileType(TileType tileType) {
        this.tileType = tileType;
    }

    public Symbol getPlayerInfoSymbol() {
        return playerInfoSymbol;
    }

    public void setPlayerInfoSymbol(Symbol playerInfoSymbol) {
        this.playerInfoSymbol = playerInfoSymbol;
    }

    public void clear() {
        this.tileType = TileType.EMPTY;
        this.playerInfoSymbol = Symbol.EMPTY;
        this.aiInfoSymbol = Symbol.EMPTY;
    }
}