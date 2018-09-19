package com.illcode.lettergame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

final class GameWindow implements KeyListener
{
    private Frame frame;
    private BufferStrategy strategy;

    GameWindow() {
        frame = new Frame(null, GuiUtils.graphicsConfiguration);
        frame.setFocusTraversalKeysEnabled(false);
        frame.enableInputMethods(false);
        frame.setIgnoreRepaint(true);
        frame.addKeyListener(this);
        frame.addWindowListener(new GameWindowListener());
    }

    void init() {
        GraphicsDevice gd = GuiUtils.graphicsEnvironment.getDefaultScreenDevice();
        frame.setSize(Utils.intPref("window-width", 800), Utils.intPref("window-height", 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.createBufferStrategy(2);
        strategy = frame.getBufferStrategy();
    }

    void close() {
        frame.setVisible(false);
        frame.dispose();
    }

    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (c == KeyEvent.CHAR_UNDEFINED)
            return;
    }

    public void keyPressed(KeyEvent e) {
        int keycode = e.getKeyCode();
        int modifiers = e.getModifiers();
        if (keycode == KeyEvent.VK_Q && modifiers == KeyEvent.CTRL_MASK) {
            close();
        }
    }

    public void keyReleased(KeyEvent e) { /* do nothing*/ }

    private class GameWindowListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent e) {
            close();
        }
    }
}
