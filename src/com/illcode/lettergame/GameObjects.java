package com.illcode.lettergame;

import java.awt.image.BufferedImage;

final class GameObjects
{
    static final class Cloud
    {
        BufferedImage image;
        int x, y, width, height;
        int speed;
        int moveCntr;
    }

    static final class Letter
    {
        char letter;
        BufferedImage image;
        int zoom;
        int x, y, width, height;

        Letter makeCopy() {
            Letter l = new Letter();
            l.letter = letter;
            l.image = image;
            l.width = width;
            l.height = height;
            return l;
        }
    }
}
