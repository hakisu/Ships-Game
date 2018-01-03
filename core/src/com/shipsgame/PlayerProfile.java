package com.shipsgame;

import com.shipsgame.map.Map;
import com.shipsgame.replays.Replay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayerProfile implements Serializable {

    private int wins;
    private int loses;
    private String firstName;
    private String lastName;
    private String login;
    private String password;
    private Map resumedGameMap;
    private List<Replay> replayList;

    public PlayerProfile(String login, String password) {
        this.wins = 0;
        this.loses = 0;
        this.firstName = "DefaultFirstName";
        this.lastName = "DefaultLastName";
        this.login = login;
        this.password = password;
        this.replayList = new ArrayList<>();
    }

    public int getWins() {
        return wins;
    }

    public int getLoses() {
        return loses;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLogin() {
        return login;
    }

    public Map getResumedGameMap() {
        return resumedGameMap;
    }

    public void setResumedGameMap(Map resumedGameMap) {
        this.resumedGameMap = resumedGameMap;
    }

    public List<Replay> getReplayList() {
        return replayList;
    }

    public void addReplay(Replay replayToAdd) {
        this.replayList.add(replayToAdd);
    }

    public void addWin() {
        this.wins++;
    }

    public void addLoss() {
        this.loses++;
    }

    public boolean verifyPassword(String hashedPassword) {
        return hashedPassword.equals(this.password);
    }
}
