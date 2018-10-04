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
        int originalWidth, originalHeight;
        int zoom;
        int x, y, width, height;

        Letter makeCopy() {
            Letter l = new Letter();
            l.letter = letter;
            l.image = image;
            l.width = originalWidth;
            l.height = originalHeight;
            return l;
        }
    }
}
