package com.illcode.lettergame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.List;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.TinySound;

import static com.illcode.lettergame.GameObjects.Cloud;

final class GameWindow implements KeyListener
{
    private boolean initialized;

    private Frame frame;
    private int width, height;

    private BufferStrategy strategy;
    private volatile boolean quitFlag;
    private BlockingQueue<Character> charQueue;

    private BufferedImage background;
    private float backgroundScaleX, backgroundScaleY;

    private List<Cloud> clouds;
    private int numclouds;

    private Music music;
    private boolean playMusic;

    private int cloudSpeed, letterSpeed;

    GameWindow() {
        charQueue = new ArrayBlockingQueue<>(30);
    }

    boolean init() {
        if (initialized)
            return false;

        // GraphicsDevice gd = GuiUtils.graphicsEnvironment.getDefaultScreenDevice();
        width = Utils.intPref("window-width", 800);
        height = Utils.intPref("window-height", 600);
        if (!loadAssets())
            return false;

        frame = new Frame(null, GuiUtils.graphicsConfiguration);
        frame.setTitle("Little Bun's Letter Game");
        frame.setFocusTraversalKeysEnabled(false);
        frame.enableInputMethods(false);
        frame.setIgnoreRepaint(true);
        frame.addKeyListener(this);
        frame.addWindowListener(new GameWindowListener());
        frame.setSize(width, height);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.createBufferStrategy(2);
        strategy = frame.getBufferStrategy();

        initialized = true;
        return true;
    }

    // Note that this is called from the main (not EDT!) thread
    void gameLoop() {
        if (!initialized)
            return;

        if (playMusic)
            music.play(true);
        int loopDelay = Utils.intPref("loop-delay", 40);
        while (!quitFlag) {
            Character c = charQueue.poll();
            if (c != null)
                addLetter(c);

            do {
                do {
                    Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
                    graphics.setRenderingHints(GuiUtils.getFastRenderingHints());
                    drawBackground(graphics);
                    drawLetters(graphics);
                    graphics.dispose();
                } while (strategy.contentsRestored());
                strategy.show();
            } while (strategy.contentsLost());

            try {
                Thread.sleep(loopDelay);
            } catch (InterruptedException e) {
                System.err.println("gameLoop() sleep interrupted.");
            }
        }
    }

    void shutdown() {
        if (background != null) {
            background.flush();
            background = null;
        }
        if (music != null) {
            music.stop();
            music.unload();
            music = null;
        }
        TinySound.shutdown();
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (Character.isLetterOrDigit(c)) {
            try {
                charQueue.put(c);
            } catch (InterruptedException e1) {
                System.err.println("keyTyped() put interrupted.");
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        int keycode = e.getKeyCode();
        int modifiers = e.getModifiers();
        if (keycode == KeyEvent.VK_Q && modifiers == KeyEvent.CTRL_MASK) {
            quitFlag = true;
        }
    }

    public void keyReleased(KeyEvent e) { /* do nothing*/ }

    private boolean loadAssets() {
        BufferedImage bi = GuiUtils.loadOpaqueImage(Paths.get(Utils.pref("background-image", "assets/snowy-wash.png")));
        if (bi == null)
            return false;
        backgroundScaleX = (float) width / bi.getWidth();
        backgroundScaleY = (float) height / bi.getHeight();
        background = GuiUtils.createOpaqueImage(width, height);
        Graphics2D g = background.createGraphics();
        g.setRenderingHints(GuiUtils.getQualityRenderingHints());
        g.drawImage(bi, 0, 0, width, height, null);
        g.dispose();
        bi = null;

        String[] cloudPaths = Utils.pref("cloud-images", "").split(",\\s*");
        int numCloudImages = cloudPaths.length;
        if (numCloudImages == 0)
            return false;
        BufferedImage[] cloudImages = new BufferedImage[numCloudImages];
        for (int i = 0; i < numCloudImages; i++) {
            bi = GuiUtils.loadBitmaskImage(Paths.get(cloudPaths[i]), -1);
            if (bi == null)
                return false;
            int w = (int) (bi.getWidth() * backgroundScaleX);
            int h = (int) (bi.getHeight() * backgroundScaleY);
            cloudImages[i] = GuiUtils.createBitmaskImage(w, h);
            g = cloudImages[i].createGraphics();
            g.setRenderingHints(GuiUtils.getQualityRenderingHints());
            g.drawImage(bi, 0, 0, w, h, null);
            g.dispose();
        }
        bi = null;

        numclouds = Utils.intPref("numclouds", 3);
        clouds = new ArrayList<>(numclouds);
        for (int i = 0; i < numclouds; i++) {
            Cloud c = new Cloud();
            c.image = cloudImages[i % numCloudImages];
            c.y = Utils.randInt(20, height / 3);  // distribute the clouds randomly over the top third of the window
            c.x = Utils.randInt(-20, width - 20); // and randomly across the width
            clouds.add(c);
        }
        // TODO: debug why the cloud images are not translucent

        TinySound.init();
        playMusic = Utils.booleanPref("play-music", true);
        if (playMusic) {
            music = TinySound.loadMusic(new File(Utils.pref("music", "assets/Treehouse-Intro-Music.ogg")));
            if (music == null)
                return false;
        }
        return true;
    }

    private void drawBackground(Graphics2D g) {
        g.drawImage(background, 0, 0, null);
        for (Cloud c : clouds) {
            g.drawImage(c.image, c.x, c.y, null);
        }
    }

    private void drawLetters(Graphics2D g) {

    }

    private void addLetter(char c) {
        System.err.println("Add letter: " + c);
    }

    private class GameWindowListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent e) {
            quitFlag = true;
        }
    }
}
