package com.illcode.lettergame;

public class LetterGame
{
    private static GameWindow gameWindow;

    public static void main(String[] args) {
        GuiUtils.initGraphics();
        gameWindow = new GameWindow();
        gameWindow.begin();
    }
}
