package com.shipsgame.replays;

import com.shipsgame.Ship;
import com.shipsgame.map.Symbol;
import com.shipsgame.map.Tile;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

public class Replay implements Serializable {

    private static final float TIME_BETWEEN_ACTION_CHANGE = 0.5f;

    private String creationDateString;
    private List<Ship> playerShipsList;
    private Stack<Action> actionsStack;
    private float timer;

    public Replay() {
        this.actionsStack = new Stack<>();
        this.timer = 0;
    }

    @SuppressWarnings("IncompleteCopyConstructor")
    public Replay(Replay other) {
        this.creationDateString = other.creationDateString;
        this.playerShipsList = other.playerShipsList;
        this.actionsStack = new Stack<>();
        for (int i = other.actionsStack.size() - 1; i >= 0; i--) {
            this.actionsStack.add(new Action(other.actionsStack.get(i)));
        }
        this.timer = 0;
    }

    public String getCreationDateString() {
        return creationDateString;
    }

    public void setCreationDateString(String creationDateString) {

        this.creationDateString = creationDateString;
    }

    public List<Ship> getPlayerShipsList() {
        return playerShipsList;
    }

    public void setPlayerShipsList(List<Ship> playerShipsList) {
        this.playerShipsList = playerShipsList;
    }

    public void addAction(Action action) {
        this.actionsStack.push(action);
    }

    public void update(float delta, Tile[][] mapArray) {
        if (!actionsStack.empty()) {
            this.timer += delta;
            if (this.timer >= TIME_BETWEEN_ACTION_CHANGE) {
                this.timer = 0;
                Action currentAction = actionsStack.pop();
                int actionIndexX = currentAction.getActionTileIndices().getIndexX();
                int actionIndexY = currentAction.getActionTileIndices().getIndexY();

                if (currentAction.getActionType() == ActionType.PLAYER_SHOT || currentAction.getActionType() == ActionType.PLAYER_MISS_SHOT) {
                    if (currentAction.getActionType() == ActionType.PLAYER_SHOT) {
                        mapArray[actionIndexY][actionIndexX].setPlayerInfoSymbol(Symbol.SUCCESSFUL_HIT);
                    } else if (currentAction.getActionType() == ActionType.PLAYER_MISS_SHOT) {
                        mapArray[actionIndexY][actionIndexX].setPlayerInfoSymbol(Symbol.FAILED_HIT);
                    }
                } else {
                    if (currentAction.getActionType() == ActionType.AI_SHOT) {
                        mapArray[actionIndexY][actionIndexX].setAiInfoSymbol(Symbol.SUCCESSFUL_HIT);
                    } else if (currentAction.getActionType() == ActionType.AI_MISS_SHOT) {
                        mapArray[actionIndexY][actionIndexX].setAiInfoSymbol(Symbol.FAILED_HIT);
                    }
                }
            }
        }
    }
}