package com.illcode.lettergame;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class LetterGame
{
    private static GameWindow gameWindow;
    static Properties prefs;

    public static void main(String[] args) {
        if (!loadPrefs(Paths.get("lettergame.properties"))) {
            System.err.println("Error loading preferences!");
            System.exit(1);
        }
        GuiUtils.initGraphics();
        GuiUtils.registerGameFonts();
        gameWindow = new GameWindow();
        try {
            if (!gameWindow.init()) {
                System.err.println("Error initializing!");
            } else {
                gameWindow.gameLoop();
            }
        } finally {
            gameWindow.shutdown();
        }
    }

    private static boolean loadPrefs(Path path) {
        prefs = new Properties();
        if (Files.exists(path)) {
            try (FileReader r = new FileReader(path.toFile())) {
                prefs.load(r);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
