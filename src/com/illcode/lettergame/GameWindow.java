package com.illcode.lettergame;

import java.awt.*;
import java.awt.image.BufferStrategy;

final class GameWindow
{
    private Frame frame;
    private BufferStrategy strategy;

    GameWindow() {
        frame = new Frame(null, GuiUtils.graphicsConfiguration);
        frame.setFocusTraversalKeysEnabled(false);
        frame.enableInputMethods(false);
        frame.setIgnoreRepaint(true);
    }

    void begin() {
        GraphicsDevice gd = GuiUtils.graphicsEnvironment.getDefaultScreenDevice();
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.createBufferStrategy(2);
        strategy = frame.getBufferStrategy();
        BufferCapabilities caps = GuiUtils.graphicsConfiguration.getBufferCapabilities();
        System.err.println("isPageFlipping: " + caps.isPageFlipping());
        frame.dispose();
    }
}
