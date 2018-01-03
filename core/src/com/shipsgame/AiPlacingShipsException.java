package com.shipsgame;

public class AiPlacingShipsException extends RuntimeException {
    public AiPlacingShipsException() {
        super("Ai couldn't place all ships of given sizes on the map.");
    }
}
