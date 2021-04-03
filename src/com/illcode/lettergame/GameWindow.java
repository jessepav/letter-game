package com.illcode.lettergame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import kuusisto.tinysound.Music;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;

import static com.illcode.lettergame.GameObjects.Cloud;
import static com.illcode.lettergame.GameObjects.Letter;

final class GameWindow implements KeyListener
{
    private boolean initialized;

    private Frame frame;
    private int screenWidth, screenHeight;
    private boolean fullscreen;

    private GraphicsDevice graphicsDevice;
    private BufferStrategy strategy;
    private FontRenderContext letterFRC;
    private int letterScale, letterZoom, letterZoomSpeed;

    private volatile boolean quitFlag;
    private BlockingQueue<Character> charQueue;

    private BufferedImage background;
    private float backgroundScaleX, backgroundScaleY;
    private BufferedImage spaceImage;

    private List<Cloud> clouds;
    private int numclouds;
    private int cloudMinSpeed, cloudMaxSpeed;

    private Map<Character,Letter> letterArchetypes;
    private List<Letter> letters;
    private Color[] letterColors;
    private Color shadowColor;
    private int letterMoveCntr;
    private boolean singleLetterMode;

    private Music music;
    private boolean playMusic;

    private Sound[] letterSounds;
    private boolean playSound;
    private double soundVolume;
    private long lastSoundTime;

    private int letterSpeed;

    private int loopCntr;

    GameWindow() {
        charQueue = new ArrayBlockingQueue<>(30);
        letterArchetypes = new HashMap<>(80);
        letters = new LinkedList<>();
    }

    boolean init() {
        if (initialized)
            return false;

        graphicsDevice = GuiUtils.graphicsEnvironment.getDefaultScreenDevice();
        fullscreen = Utils.booleanPref("fullscreen", false) && graphicsDevice.isFullScreenSupported();

        if (fullscreen) {
            DisplayMode mode = graphicsDevice.getDisplayMode();
            screenWidth = mode.getWidth();
            screenHeight = mode.getHeight();
        } else {
            screenWidth = Utils.intPref("window-width", 900);
            screenHeight = Utils.intPref("window-height", 506);
        }
        if (!loadAssets())
            return false;
        if (!loadColors())
            return false;
        GuiUtils.letterFont = GuiUtils.letterFont.deriveFont
            (GuiUtils.letterFont.getSize2D() * (backgroundScaleX + backgroundScaleY) / 2);

        frame = new Frame(null, GuiUtils.graphicsConfiguration);
        frame.setFocusTraversalKeysEnabled(false);
        frame.enableInputMethods(false);
        frame.setIgnoreRepaint(true);
        frame.addKeyListener(this);
        frame.addWindowListener(new GameWindowListener());
        frame.setUndecorated(true);
        frame.setCursor(GuiUtils.getBlankCursor());
        if (fullscreen) {
            graphicsDevice.setFullScreenWindow(frame);
        } else {
            frame.setSize(screenWidth, screenHeight);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
        frame.createBufferStrategy(2);
        strategy = frame.getBufferStrategy();
        System.out.println("Hardware accelerated back-buffer: " +
            strategy.getCapabilities().getBackBufferCapabilities().isAccelerated());

        letterSpeed = Utils.intPref("letter-speed", 5);
        letterScale = Utils.intPref("letter-scale", 2);
        letterZoom = Utils.intPref("letter-zoom", 8);
        letterZoomSpeed = Utils.intPref("letter-zoom-speed", 4) * letterScale;
        singleLetterMode = Utils.booleanPref("single-letter-mode", false);

        initialized = true;
        return true;
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
        if (letterSounds != null) {
            for (Sound s : letterSounds) {
                s.stop();
                s.unload();
            }
            letterSounds = null;
        }
        TinySound.shutdown();
        if (clouds != null) {
            for (Cloud c : clouds)
                c.image.flush();
            clouds = null;
        }
        for (Letter l : letters)
            l.image.flush();
        letters.clear();
        if (frame != null) {
            if (fullscreen)
                graphicsDevice.setFullScreenWindow(null);
            strategy.dispose();
            frame.setVisible(false);
            frame.dispose();
        }
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
                    if (letterFRC == null)
                        letterFRC = graphics.getFontRenderContext();
                    updateObjects();
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
            loopCntr++;
        }
    }

    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (c == ' ' || Character.isLetterOrDigit(c)) {
            try {
                charQueue.put(Character.toUpperCase(c));
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
        backgroundScaleX = (float) screenWidth / bi.getWidth();
        backgroundScaleY = (float) screenHeight / bi.getHeight();
        background = GuiUtils.createOpaqueImage(screenWidth, screenHeight);
        Graphics2D g = background.createGraphics();
        g.setRenderingHints(GuiUtils.getQualityRenderingHints());
        g.drawImage(bi, 0, 0, screenWidth, screenHeight, null);
        g.dispose();
        bi = null;

        spaceImage = GuiUtils.loadBitmaskImage(Paths.get(Utils.pref("space-image", "assets/space-image.png")), -1);
        if (spaceImage == null)
            return false;

        numclouds = Utils.intPref("numclouds", 3);
        if (numclouds != 0) {
            String[] cloudPaths = Utils.pref("cloud-images", "").split(",\\s*");
            int numCloudImages = cloudPaths.length;
            if (numCloudImages == 0)
                return false;
            BufferedImage[] cloudImages = new BufferedImage[numCloudImages];
            for (int i = 0; i < numCloudImages; i++) {
                bi = GuiUtils.loadTranslucentImage(Paths.get(cloudPaths[i]));
                if (bi == null)
                    return false;
                int w = (int) (bi.getWidth() * backgroundScaleX);
                int h = (int) (bi.getHeight() * backgroundScaleY);
                cloudImages[i] = GuiUtils.createTranslucentImage(w, h);
                g = cloudImages[i].createGraphics();
                g.setRenderingHints(GuiUtils.getQualityRenderingHints());
                g.drawImage(bi, 0, 0, w, h, null);
                g.dispose();
            }
            bi = null;

            clouds = new ArrayList<>(numclouds);
            for (int i = 0; i < numclouds; i++) {
                Cloud c = new Cloud();
                c.image = cloudImages[i % numCloudImages];
                c.width = c.image.getWidth();
                c.height = c.image.getHeight();
                clouds.add(c);
            }

            String[] sa = Utils.pref("cloud-speed-range", "32,96").split(",\\s*");
            if (sa.length != 2)
                return false;
            cloudMinSpeed = Utils.parseInt(sa[0], 32);
            cloudMaxSpeed = Utils.parseInt(sa[1], 96);

            // Distribute the clouds around the sky, with varying speed
            int skyZoneSize = screenWidth / numclouds;
            for (int i = 0; i < numclouds; i++) {
                Cloud c = clouds.get(i);
                c.x = skyZoneSize*i + Utils.randInt(10, skyZoneSize-10);
                c.y = Utils.randInt(30, screenHeight / 3);
                c.speed = Utils.randInt(cloudMinSpeed, cloudMaxSpeed);
            }
        }

        TinySound.init();
        playMusic = Utils.booleanPref("play-music", true);
        if (playMusic) {
            music = TinySound.loadMusic(new File(Utils.pref("music", "assets/Treehouse-Intro-Music.ogg")));
            if (music == null)
                return false;
            music.setVolume(Utils.floatPref("music-volume", 1.0f));
        }
        playSound = Utils.booleanPref("play-sounds", true);
        if (playSound) {
            soundVolume = Utils.floatPref("sound-volume", 1.0f);
            String[] sa = Utils.pref("letter-sounds", "").split(",\\s*");
            int n = sa.length;
            if (n == 0)
                return false;
            letterSounds = new Sound[n];
            for (int i = 0; i < n; i++) {
                letterSounds[i] = TinySound.loadSound(new File(sa[i]));
                if (letterSounds[i] == null)
                    return false;
            }
        }
        return true;
    }

    private boolean loadColors() {
        String[] sa = Utils.pref("letter-colors", "").split(",\\s*");
        int n = sa.length;
        if (n == 0)
            return false;
        letterColors = new Color[n];
        try {
            for (int i = 0; i < n; i++)
                letterColors[i] = new Color(Integer.parseInt(sa[i], 16));
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            return false;
        }
        int rgb = Utils.parseInt(Utils.pref("shadow-color", "676767"), 16, -1);
        if (rgb == -1)
            return false;
        shadowColor = new Color(rgb);
        return true;
    }

    private void updateObjects() {
        if (numclouds != 0) {
            for (Cloud c : clouds) {
                c.moveCntr += c.speed;
                int px = c.moveCntr >> 6;
                c.moveCntr &= 0x3F;  // zero out everything but the bottom 6 bits
                c.x += px;
                if (c.x > (screenWidth + c.width)) {
                    c.x = -c.width / 2;
                    c.y = Utils.randInt(30, screenHeight / 3);
                    c.speed = Utils.randInt(cloudMinSpeed, cloudMaxSpeed);
                }
            }
        }

        if (!singleLetterMode) {
            ListIterator<Letter> iter = letters.listIterator(0);
            letterMoveCntr += letterSpeed;
            int px = letterMoveCntr >> 6;
            letterMoveCntr &= 0x3F;
            while (iter.hasNext()) {
                Letter l = iter.next();
                if (l.zoom > 0) {
                    l.zoom -= letterZoomSpeed;
                    l.width -= letterZoomSpeed;
                    l.height -= letterZoomSpeed;
                    l.x += letterZoomSpeed / 2;
                    //l.y += letterZoomSpeed / 2;
                } else {
                    l.y += px;
                    if (l.y > screenHeight)
                        iter.remove();
                }
            }
        }
    }

    private void drawBackground(Graphics2D g) {
        g.drawImage(background, 0, 0, null);
        if (numclouds != 0) {
            for (Cloud c : clouds) {
                g.drawImage(c.image, c.x - c.width/2, c.y - c.height/2, null);
            }
        }
    }

    private void drawLetters(Graphics2D g) {
        for (Letter l : letters) {
            g.drawImage(l.image, l.x, l.y, l.width, l.height, null);
        }
    }

    private void addLetter(char c) {
        Letter archetype = letterArchetypes.get(c);
        Letter newLetter;
        if (archetype == null) {
            archetype = createLetter(c, getColorForLetter(c));
            letterArchetypes.put(c, archetype);
            newLetter = archetype;
        } else {
            newLetter = archetype.makeCopy();
        }
        if (singleLetterMode) {
            letters.clear();
            newLetter.x = screenWidth / 2 - newLetter.width / 2;
            newLetter.y = screenHeight / 2 - newLetter.height / 2;
        } else {
            newLetter.zoom = letterZoom * letterScale;
            newLetter.width += newLetter.zoom;
            newLetter.height += newLetter.zoom;
            newLetter.x = Utils.randInt(5, screenWidth - 5 - newLetter.width);
            newLetter.y = 0;
        }
        letters.add(newLetter);
        if (playSound)
            playLetterSound(c);
    }

    private Color getColorForLetter(char c) {
        int idx;
        if (c >= '0' && c <= '9')
            idx = 26 + (c - '0');
        else if (c >= 'A' && c <= 'Z')
            idx = c - 'A';
        else
            idx = 0;
        return letterColors[idx % letterColors.length];
    }

    private void playLetterSound(char c) {
        long t = System.currentTimeMillis();
        if (t - lastSoundTime > 200) {  // don't play sounds too close together
            letterSounds[c % letterSounds.length].play(soundVolume);
            lastSoundTime = t;
        }
    }

    private Letter createLetter(char c, Color color) {
        Letter l = new Letter();
        if (c == ' ') {
            l.image = spaceImage;
            l.width = l.originalWidth = spaceImage.getWidth();
            l.height = l.originalHeight = spaceImage.getHeight();
        } else {
            TextLayout layout = new TextLayout(Character.toString(c), GuiUtils.letterFont, letterFRC);
            Rectangle2D r = layout.getPixelBounds(null, 0, 0);
            int xoff = (int)-r.getX();
            int yoff = (int)-r.getY();
            int w = (int)r.getWidth();
            int h = (int)r.getHeight();
            int m = 5;  // margin
            l.width = l.originalWidth = w + 2*m;
            l.height = l.originalHeight = h + 2*m;
            l.image = GuiUtils.createBitmaskImage(l.width, l.height);
            Graphics2D g = l.image.createGraphics();
            g.setColor(shadowColor);
            layout.draw(g, m + xoff + 3, m + yoff + 3);
            g.setColor(color);
            layout.draw(g, m + xoff, m + yoff);
            g.dispose();
        }
        if (singleLetterMode)
            letterScale = screenHeight / l.height;

        if (letterScale != 1) {
            l.width *= letterScale;
            l.height *= letterScale;
            l.originalWidth = l.width;
            l.originalHeight = l.height;
        }
        return l;
    }

    private class GameWindowListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent e) {
            quitFlag = true;
        }
    }
}
