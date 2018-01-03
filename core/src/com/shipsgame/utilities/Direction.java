package com.shipsgame.utilities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Direction {
    EAST,
    SOUTH,
    WEST,
    NORTH;

    private static final List<Direction> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static Direction getRandomDirection() {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }

    public static Direction getOppositeDirection(Direction currentDirection) {
        if (currentDirection == EAST) {
            return WEST;
        } else if (currentDirection == WEST) {
            return EAST;
        } else if (currentDirection == NORTH) {
            return SOUTH;
        } else {
            return NORTH;
        }
    }
}