package com.illcode.lettergame;

import java.util.Random;

import static com.illcode.lettergame.LetterGame.prefs;

public final class Utils
{
    private static Random random;

    /**
     * Examines a String to determine if it's a way to say "true"
     * @param s if this is "true", "on", "1", or "yes" then we consider it true
     * @return truth value of {@code s}
     */
    public static boolean parseBoolean(String s) {
        if (s != null && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on") || s.equals("1") || s.equals("yes")))
            return true;
        else
            return false;
    }

    /**
     * Parses an input string as a decimal integer
     * @param s String representation of an integer
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return int value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static int parseInt(String s, int errorVal) {
        return parseInt(s, 10, errorVal);
    }

    /**
     * Parses an input string as an integer of the given radix
     * @param s String representation of an integer
     * @param radix integer radix
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return int value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static int parseInt(String s, int radix, int errorVal) {
        int i = errorVal;
        if (s != null && s.length() > 0) {
            try {
                i = Integer.parseInt(s, radix);
            } catch (NumberFormatException ex) {
                i = errorVal;
            }
        }
        return i;
    }

    /**
     * Parses an input string as a float
     * @param s String representation of a float
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return float value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static float parseFloat(String s, float errorVal) {
        float f = errorVal;
        if (s != null && s.length() > 0) {
            try {
                f = Float.parseFloat(s);
            } catch (NumberFormatException ex) {
                f = errorVal;
            }
        }
        return f;
    }

    /**
     * Parses an input string as a double
     * @param s String representation of a double
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return double value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static double parseDouble(String s, double errorVal) {
        double f = errorVal;
        if (s != null && s.length() > 0) {
            try {
                f = Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                f = errorVal;
            }
        }
        return f;
    }

    /**
     * Equivalent to {@link #parseInt(String, int) parseInt(s, 0)}
     */
    public static int parseInt(String s) {
        return parseInt(s, 0);
    }

    /**
     * Ensures an input int {@code i} is between {@code [min, max]} (inclusive on both ends).
     */
    public static int boundedInt(int min, int max, int i) {
        if (i < min)
            return min;
        else if (i > max)
            return max;
        else return i;
    }

    /**
     * Returns a random int between {@code min} and {@code max}, inclusive.
     */
    public static int randInt(int min, int max) {
        if (random == null)
            random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    /** Equivalent to {@code String.format(s, args)} */
    public static String fmt(String s, Object... args) {
        return String.format(s, args);
    }

    /**
     * Returns the boolean value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    static boolean booleanPref(String key, boolean defaultVal) {
        if (prefs.containsKey(key)) {
            return parseBoolean(prefs.getProperty(key));
        } else {
            prefs.setProperty(key, Boolean.toString(defaultVal));
            return defaultVal;
        }
    }

    /**
     * Returns the int value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}. If the value of
     * the preference cannot be parsed as an int, we return {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    static int intPref(String key, int defaultVal) {
        if (prefs.containsKey(key)) {
            return parseInt(prefs.getProperty(key), defaultVal);
        } else {
            prefs.setProperty(key, Integer.toString(defaultVal));
            return defaultVal;
        }
    }

    /**
     * Returns the float value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}. If the value of
     * the preference cannot be parsed as a float, we return {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    static float floatPref(String key, float defaultVal) {
        if (prefs.containsKey(key)) {
            return parseFloat(prefs.getProperty(key), defaultVal);
        } else {
            prefs.setProperty(key, Float.toString(defaultVal));
            return defaultVal;
        }
    }

    /**
     * Returns the String value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    static String pref(String key, String defaultVal) {
        if (prefs.containsKey(key)) {
            return prefs.getProperty(key);
        } else {
            prefs.setProperty(key, defaultVal);
            return defaultVal;
        }
    }
}
