package com.shipsgame.utilities;

import java.io.Serializable;

public class Pair implements Serializable {

    private int indexX;
    private int indexY;

    public Pair(int indexX, int indexY) {
        this.indexX = indexX;
        this.indexY = indexY;
    }

    public Pair(Pair other) {
        this.indexX = other.indexX;
        this.indexY = other.indexY;
    }

    public int getIndexX() {
        return indexX;
    }

    public void setIndexX(int indexX) {
        this.indexX = indexX;
    }

    public int getIndexY() {
        return indexY;
    }

    public void setIndexY(int indexY) {
        this.indexY = indexY;
    }

    @Override
    public String toString() {
        return "Index x : " + indexX + " Index y : " + indexY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return indexX == pair.indexX && indexY == pair.indexY;
    }
}
