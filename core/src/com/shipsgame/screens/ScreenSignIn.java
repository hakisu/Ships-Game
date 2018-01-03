package com.shipsgame.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.shipsgame.PlayerProfile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class ScreenSignIn extends ScreenAdapter {

    private static final String USERS_PATH_NAME = "authorization/users.passwords";
    private static final String INFO_EMPTY = "";
    private static final String INFO_WRONG_PASSWORD = "Wrong password.";
    private static final String INFO_LOGIN_ALREADY_EXISTS = "Account with this login already exists.";
    private static final String INFO_ACCOUNT_CREATED = "Account created successfully.";
    private static final String INFO_NO_SUCH_LOGIN = "No account with this login exists.";
    private static final String INFO_CHEATING = "No cheating!";

    private final ShipsGame shipsGame;

    private TextureAtlas textureAtlas;
    private Skin skin;
    private Stage stage;
    private TextField loginTextField;
    private TextField passwordTextField;
    private Label infoLabel;

    public ScreenSignIn(ShipsGame shipsGame) {
        this.shipsGame = shipsGame;
        this.textureAtlas = new TextureAtlas(ShipsGame.UI_SKIN_ATLAS_NAME);
        this.skin = new Skin(Gdx.files.internal(ShipsGame.UI_SKIN_JSON_NAME), textureAtlas);
        stage = new Stage(new ScreenViewport(new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight())));

        // Set up all actors
        TextButton loginTextButton = new TextButton("Log in", skin);
        TextButton createProfileTextButton = new TextButton("Create profile", skin);
        TextButton exitTextButton = new TextButton("Exit", skin);
        Label loginLabel = new Label("Login", skin);
        loginTextField = new TextField("", skin);
        Label passwordLabel = new Label("Password", skin);
        passwordTextField = new TextField("", skin);
        passwordTextField.setPasswordMode(true);
        passwordTextField.setPasswordCharacter('*');
        infoLabel = new Label(INFO_EMPTY, skin);
        infoLabel.setColor(Color.RED);

        // Add all actors to root table and root table to stage
        Table table = new Table();
        table.setFillParent(true);

        table.add(loginLabel).right();
        table.add(loginTextField).left();
        table.row();
        table.add(passwordLabel).right();
        table.add(passwordTextField).left();
        table.row().spaceTop(10);
        table.add(loginTextButton).colspan(2);
        table.row();
        table.add(createProfileTextButton).colspan(2);
        table.row();
        table.add(exitTextButton).colspan(2);
        table.row();
        table.add(infoLabel).colspan(2);

        stage.addActor(table);
        stage.setKeyboardFocus(loginTextField);

        // Buttons events
        loginTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                processLoginAttempt();
            }
        });

        createProfileTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (createUser()) {
                    // Creation of login, password and player profile was successful
                    infoLabel.setText(INFO_ACCOUNT_CREATED);
                } else {
                    // User with this login already exists
                    infoLabel.setText(INFO_LOGIN_ALREADY_EXISTS);
                }
            }
        });

        exitTextButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    }

    /**
     * @param login username/login of the user we are searching for
     * @return password for the given {@code login} or null if user doesn't exist
     */
    private String findUserPassword(String login) {
        Path path = Paths.get(USERS_PATH_NAME);
        try {
            String string = new String(Files.readAllBytes(path));
            Scanner scanner = new Scanner(string);
            while (scanner.hasNextLine()) {
                String currentString = scanner.nextLine();
                String[] tokens = currentString.split(" ");
                String currentUsername = tokens[0];
                String currentHashedPassword = tokens[1];
                if (currentUsername.equals(login)) {
                    return currentHashedPassword;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Go to the menu screen if login and password are valid.
     */
    private void processLoginAttempt() {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();

        String hashedPassword = findUserPassword(login);
        if (hashedPassword != null) {
            if (hashedPassword.equals(createHashedPassword(password))) {
                // What happens when login attempt is successful
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(ShipsGame.PATH_TO_PLAYER_PROFILES + loginTextField.getText() + ".profile"))) {
                    PlayerProfile loggedUserPlayerProfile = (PlayerProfile) in.readObject();
                    if (!loggedUserPlayerProfile.verifyPassword(createHashedPassword(password))) {
                        infoLabel.setText(INFO_CHEATING);
                    } else {
                        shipsGame.setPlayerProfile(loggedUserPlayerProfile);
                        shipsGame.createScreenPlayerProfile();
                        shipsGame.activateMenuScreen();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                infoLabel.setText(INFO_WRONG_PASSWORD);
            }
        } else {
            infoLabel.setText(INFO_NO_SUCH_LOGIN);
        }
    }

    /**
     * @param password string representing user password
     * @return password after hashing using MD5 or null if exception occurred
     */
    private String createHashedPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(password.getBytes());
            byte[] passwordBytes = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte passwordByte : passwordBytes) {
                stringBuilder.append(passwordByte);
            }

            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return true if user was created successfully and false otherwise
     */
    private boolean createUser() {
        // Create user if login is not already in use
        if (findUserPassword(loginTextField.getText()) == null) {
            String profileString = loginTextField.getText() + " " + createHashedPassword(passwordTextField.getText());
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(USERS_PATH_NAME), StandardOpenOption.APPEND)) {
                bufferedWriter.write(profileString);
                bufferedWriter.newLine();

                createPlayerProfile();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Create new player profile and save in the file
     */
    private void createPlayerProfile() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ShipsGame.PATH_TO_PLAYER_PROFILES + loginTextField.getText() + ".profile"))) {
            PlayerProfile playerProfile = new PlayerProfile(loginTextField.getText(), createHashedPassword(passwordTextField.getText()));
            out.writeObject(playerProfile);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        passwordTextField.setText("");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(ShipsGame.BACKGROUND_R, ShipsGame.BACKGROUND_G, ShipsGame.BACKGROUND_B, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            processLoginAttempt();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        skin.dispose();
        textureAtlas.dispose();
        stage.dispose();
        System.out.println("dispose - ScreenSignIn");
    }
}