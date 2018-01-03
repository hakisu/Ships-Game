package com.shipsgame.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.shipsgame.PlayerProfile;
import com.shipsgame.map.Map;
import com.shipsgame.replays.Replay;

public class ShipsGame extends Game {

    public static final float BACKGROUND_R = 0.12f;
    public static final float BACKGROUND_G = 0.12f;
    public static final float BACKGROUND_B = 0.12f;
    public static final String UI_SKIN_ATLAS_NAME = "skins/uiskin.atlas";
    public static final String UI_SKIN_JSON_NAME = "skins/uiskin.json";
    public static final String PATH_TO_PLAYER_PROFILES = "profiles/";

    ShapeRenderer shapeRenderer;
    private PlayerProfile playerProfile;
    private ScreenSignIn screenSignIn;
    private ScreenMenu screenMenu;
    private ScreenGame screenGame;
    private ScreenPlayerProfile screenPlayerProfile;
    private ScreenReplayer screenReplayer;

    @Override
    public void create() {
        this.shapeRenderer = new ShapeRenderer();
        this.screenSignIn = new ScreenSignIn(this);
        this.screenMenu = new ScreenMenu(this);
        this.screenGame = new ScreenGame(this);
        this.screenReplayer = new ScreenReplayer(this);

        this.setScreen(screenSignIn);
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }

    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    public boolean isMapInitialized() {
        return screenGame.isMapInitialized();
    }

    public void initializeMap() {
        screenGame.initializeMap();
    }

    public void initializeMap(Map map) {
        screenGame.initializeMap(map);
    }

    public void setCurrentReplay(Replay currentReplay) {
        screenReplayer.setCurrentReplay(currentReplay);
    }

    public void createScreenPlayerProfile() {
        this.screenPlayerProfile = new ScreenPlayerProfile(this);
    }

    public void activateMenuScreen() {
        this.setScreen(screenMenu);
    }

    public void activateGameScreen() {
        this.setScreen(screenGame);
    }

    public void activatePlayerProfileScreen() {
        this.setScreen(screenPlayerProfile);
    }

    public void activateReplayerScreen() {
        this.setScreen(screenReplayer);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        System.out.println("Dispose launched is shipsGame");
        screenSignIn.dispose();
        screenGame.dispose();
        screenMenu.dispose();
        if (screenPlayerProfile != null) {
            screenPlayerProfile.dispose();
        }
        screenReplayer.dispose();
    }
}