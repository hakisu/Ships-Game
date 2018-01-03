package com.shipsgame.replays;

import com.shipsgame.utilities.Pair;

import java.io.Serializable;

public class Action implements Serializable {

    private ActionType actionType;
    private Pair actionTileIndices;

    public Action(ActionType actionType, Pair actionTileIndices) {

        this.actionType = actionType;
        this.actionTileIndices = actionTileIndices;
    }

    @SuppressWarnings("IncompleteCopyConstructor")
    public Action(Action other) {
        this.actionType = other.actionType;
        this.actionTileIndices = new Pair(other.actionTileIndices);
    }

    public ActionType getActionType() {
        return actionType;
    }

    public Pair getActionTileIndices() {
        return actionTileIndices;
    }
}
