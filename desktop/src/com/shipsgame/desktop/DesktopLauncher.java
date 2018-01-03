package com.shipsgame.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.shipsgame.screens.ShipsGame;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.setFromDisplayMode(LwjglApplicationConfiguration.getDesktopDisplayMode());
        config.resizable = false;
        config.backgroundFPS = 60;
        config.foregroundFPS = 60;
        config.vSyncEnabled = true;
        config.width = 640;
        config.height = 640;
        config.fullscreen = false;

        new LwjglApplication(new ShipsGame(), config);
    }
}