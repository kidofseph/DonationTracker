package util;

import pircbot.User;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: Nick
 * Date: 6/3/13
 * Time: 7:46 PM
 * <p>
 * This class is used for helpful methods that perform helpful deeds
 * elsewhere in the code.
 */
public class Utils {

    /**
     * Returns a random number from 0 to the specified.
     *
     * @param param The max number to choose.
     */
    public static int nextInt(int param) {
        return random(0, param);
    }

    /**
     * Calls the #getExtension(String) method using the file name of the file.
     *
     * @param f The file to get the extension of.
     * @return The extension of the file, or null if there is none.
     */
    public static String getExtension(File f) {
        return getExtension(f.getName());
    }

    /**
     * Gets the extension of a file.
     *
     * @param fileName Name of the file to get the extension of.
     * @return The file's extension (ex: ".png" or ".wav"), or null if there is none.
     */
    public static String getExtension(String fileName) {
        String ext = null;
        int i = fileName.lastIndexOf('.');
        int len = fileName.length();
        int after = len - i;
        if (i > 0 && (i < len - 1) && after < 5) {//has to be near the end
            ext = fileName.substring(i).toLowerCase();
        }
        return ext;
    }

    /**
     * Sets the extension of a file to the specified extension.
     * <p>
     * This can also be used as an assurance that the extension of the
     * file is the specified extension.
     * <p>
     * It's expected that this method will be called before any file saving is
     * done.
     *
     * @param fileName  The name of the file to change the extension of.
     * @param extension The extension (ex: ".png" or ".wav") for the file.
     * @return The filename with the new extension.
     */
    public static String setExtension(String fileName, String extension) {
        String ext = getExtension(fileName);
        if (ext != null) {
            if (!ext.equalsIgnoreCase(extension)) {
                fileName = fileName.substring(0, fileName.indexOf(ext)) + extension;
            }
        } else {
            fileName = fileName + extension;
        }
        return fileName;
    }

    /**
     * Converts a font to string. Only really used in the Settings GUI.
     * (Font#toString() was too messy for me, and fuck making a wrapper class.
     *
     * @return The name, size, and style of the font.
     */
    public static String fontToString(Font f) {
        String toRet = "";
        if (f != null) {
            String type;
            if (f.isBold()) {
                type = f.isItalic() ? "Bold Italic" : "Bold";
            } else {
                type = f.isItalic() ? "Italic" : "Plain";
            }
            toRet = f.getName() + "," + f.getSize() + "," + type;
        }
        return toRet;
    }

    /**
     * Converts a formatted string (@see #fontToString()) into a font.
     *
     * @param fontString The string to be turned into a font.
     * @return The font.
     */
    public static Font stringToFont(String fontString) {
        String[] toFont = fontString.substring(fontString.indexOf('[') + 1, fontString.length() - 1).split(",");
        Font f = new Font("Calibri", Font.PLAIN, 18);
        if (toFont.length == 4) {
            String name = "Calibri";
            int size = 18;
            int type = Font.PLAIN;
            for (String keyValPair : toFont) {
                String[] split = keyValPair.split("=");
                String key = split[0];
                String val = split[1];
                switch (key) {
                    case "name":
                        name = val;
                        break;
                    case "style":
                        switch (val) {
                            case "plain":
                                type = Font.PLAIN;
                                break;
                            case "italic":
                                type = Font.ITALIC;
                                break;
                            case "bolditalic":
                                type = Font.BOLD + Font.ITALIC;
                                break;
                            case "bold":
                                type = Font.BOLD;
                                break;
                            default:
                                type = Font.PLAIN;
                                break;
                        }
                        break;
                    case "size":
                        try {
                            size = Integer.parseInt(val);
                        } catch (Exception e) {
                            size = 18;
                        }
                        break;
                    default:
                        break;
                }
            }
            f = new Font(name, type, size);
        }
        return f;
    }


    /**
     * Adds a single string to an array of strings, first checking to see if the array contains it.
     *
     * @param toAdd The string(s) to add to the array.
     * @param array The array to add the string to.
     * @return The array of Strings.
     */
    public static String[] addStringsToArray(String[] array, String... toAdd) {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, array);
        checkAndAdd(list, toAdd);
        return list.toArray(new String[list.size()]);
    }

    /**
     * Compares two arrays of Strings and adds the non-repeating ones to the same one.
     *
     * @param list  List of strings to compare to.
     * @param toAdd String(s) to add to the list.
     */
    public static void checkAndAdd(ArrayList<String> list, String... toAdd) {
        for (String s : toAdd) {
            if (!list.contains(s)) {
                list.add(s);
            }
        }
    }

    /**
     * Checks individual files one by one like #areFilesGood(String...) and
     * returns the good and legitimate files.
     *
     * @param files The path(s) to the file(s) to check.
     * @return The array of paths to files that actually exist.
     * @see #areFilesGood(String...) for determining if files exist.
     */
    public static String[] checkFiles(String... files) {
        ArrayList<String> list = new ArrayList<>();
        for (String s : files) {
            if (areFilesGood(s)) {
                list.add(s);
            }
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * Checks to see if the file(s) is (are) actually existing and non-blank.
     *
     * @param files The path(s) to the file(s) to check.
     * @return true if (all) the file(s) exist(s)
     * @see #checkFiles(String...) For removing bad files and adding the others anyway.
     */
    public static boolean areFilesGood(String... files) {
        int i = 0;
        for (String s : files) {
            File test = new File(s);
            if (test.exists() && test.length() > 0) i++;
        }
        return i == files.length;
    }


    /**
     * Removes the last file extension from a path.
     * <p>
     * Note that if the string contains multiple extensions, this will only remove the last one.
     * <p>
     * Ex: "portal.png.exe.java" becomes "portal.png.exe" after this method returns.
     *
     * @param s The path to a file, or the file name with its extension.
     * @return The file/path name without the extension.
     */
    public static String removeExt(String s) {
        int pos = s.lastIndexOf(".");
        if (pos == -1) return s;
        return s.substring(0, pos);
    }

    /**
     * Checks to see if the input is IRC-worthy of printing.
     *
     * @param input The input in question.
     * @return The given input if it checks out, otherwise nothing.
     */
    public static String checkText(String input) {
        input = input.trim();
        return !input.isEmpty() ? input : "";
    }

    /**
     * Returns a number between a given minimum and maximum (exclusive).
     *
     * @param min The minimum number to generate on.
     * @param max The non-inclusive maximum number to generate on.
     * @return Some random number between the given numbers.
     */
    public static int random(int min, int max) {
        return min + (max == min ? 0 : new Random().nextInt(max - min));
    }

    /**
     * Generates a color from the #hashCode() of any java.lang.Object.
     * <p>
     * Author - Dr_Kegel from Gocnak's stream.
     *
     * @param seed The Hashcode of the object you want dynamic color for.
     * @return The Color of the object's hash.
     */
    public static Color getColorFromHashcode(final int seed) {
        /* We do some bit hacks here
           hashCode has 32 bit, we use every bit as a random source */
        final int HUE_BITS = 12, HUE_MASK = ((1 << HUE_BITS) - 1);
        final int SATURATION_BITS = 8, SATURATION_MASK = ((1 << SATURATION_BITS) - 1);
        final int BRIGHTNESS_BITS = 12, BRIGHTNESS_MASK = ((1 << BRIGHTNESS_BITS) - 1);
        int t = seed;
        /*
         * We want the full hue spectrum, that means all colors of the color
		 * circle
		 */
        /* [0 .. 1] */
        final float h = (t & HUE_MASK) / (float) HUE_MASK;
        t >>= HUE_BITS;
        final float s = (t & SATURATION_MASK) / (float) SATURATION_MASK;
        t >>= SATURATION_BITS;
        final float b = (t & BRIGHTNESS_MASK) / (float) BRIGHTNESS_MASK;
        /* some tweaks that nor black nor white can be reached */
        /* at the moment h,s,b are in the range of [0 .. 1) */
        /* For s and b this is restricted to [0.75 .. 1) at the moment. */
        return Color.getHSBColor(h, s * 0.25f + 0.75f, b * 0.25f + 0.75f);
    }

    /**
     * Credit: TDuva
     *
     * @param URL The URL to check
     * @return True if the URL can be formed, else false
     */
    public static boolean checkURL(String URL) {
        try {
            new URI(URL);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    /**
     * Checks if the given integer is within the range of any of the key=value
     * pairs of the Map (inclusive).
     * <p>
     * Credit: TDuva
     *
     * @param i      The integer to check.
     * @param ranges The map of the ranges to check.
     * @return true if the given int is within the range set, else false
     */
    public static boolean inRanges(int i, Map<Integer, Integer> ranges) {
        for (Map.Entry<Integer, Integer> range : ranges.entrySet()) {
            if (i >= range.getKey() && i <= range.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a given int to the correct millis form, except for 0.
     *
     * @param given Integer to convert.
     * @return The correct Integer in milliseconds.
     */
    public static int handleInt(int given) {
        if (given < 1000 && given > 0) {// not in millis
            given = given * 1000; //convert to millis
        }
        return given;
    }

    /**
     * Gets a time (in seconds) from a parsable string.
     *
     * @param toParse The string to parse.
     * @return A time (in seconds) as an integer.
     */
    public static int getTime(String toParse) {
        int toRet;
        if (toParse.contains("m")) {
            String toParseSub = toParse.substring(0, toParse.indexOf("m"));
            try {
                toRet = Integer.parseInt(toParseSub) * 60;
                if (toParse.contains("s")) {
                    toParseSub = toParse.substring(toParse.indexOf("m") + 1, toParse.indexOf("s"));
                    toRet += Integer.parseInt(toParseSub);
                }
            } catch (Exception e) {
                toRet = -1;
            }

        } else {
            if (toParse.contains("s")) {
                toParse = toParse.substring(0, toParse.indexOf('s'));
            }
            try {
                toRet = Integer.parseInt(toParse);
            } catch (Exception e) {
                toRet = -1;
            }
        }
        return toRet;
    }

    /**
     * Gets the String value of the integer permission.
     *
     * @param permission The permission to get the String representation of.
     * @return The String representation of the permission.
     */
    public static String getPermissionString(int permission) {
        return (permission > 0 ? (permission > 1 ? (permission > 2 ? (permission > 3 ?
                "Only the Broadcaster" :
                "Only Mods and the Broadcaster") :
                "Donators, Mods, and the Broadcaster") :
                "Subscribers, Donators, Mods, and the Broadcaster") :
                "Everyone");
    }


    /**
     * Generates a pseudo-random color that works for Botnak.
     *
     * @return The randomly generated color.
     */
    public static Color getRandomColor() {
        return new Color(random(100, 256), random(100, 256), random(100, 256));
    }

    /**
     * Checks a color to see if it will show up in botnak.
     *
     * @param c The color to check.
     * @return True if the color is not null, and shows up in botnak.
     */
    public static boolean checkColor(Color c) {
        return c != null && checkInts(c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Checks if the red, green, and blue show up in Botnak,
     * using the standard Luminance formula.
     *
     * @param r Red value
     * @param g Green value
     * @param b Blue value
     * @return true if the Integers meet the specification.
     */
    public static boolean checkInts(int r, int g, int b) {
        double luma = (0.3 * (double) r) + (0.6 * (double) g) + (0.1 * (double) b);
        return luma > (double) 35;
    }

    /**
     * Caps a number between two given numbers.
     *
     * @param numLesser The lower-bound (inclusive) number to compare against.
     * @param numHigher The higher-bound (inclusive) number to compare against.
     * @param toCompare The number to check and perhaps cap.
     * @param <E>       Generics, used for making one method for all number types.
     * @return If the number is within the two bounds, the number is returned.
     * Otherwise, return the supplied number bound that the number is closer to.
     */
    public static <E extends Number> E capNumber(E numLesser, E numHigher, E toCompare) {
        E toReturn = toCompare;
        if (toCompare.floatValue() > numHigher.floatValue()) toReturn = numHigher;//floats are the most precise here
        else if (toCompare.floatValue() < numLesser.floatValue()) toReturn = numLesser;
        return toReturn;
    }

    /**
     * Parses Twitch's tags for an IRC message and spits them out into a HashMap.
     *
     * @param line The line to parse.
     * @return A HashMap full of Key and Value pairs for the tags.
     */
    public static HashMap<String, String> parseTagsToMap(String line)
    {
        HashMap<String, String> toReturn = new HashMap<>();
        if (line != null)
        {
            line = line.substring(1);
            String[] parts = line.split(";");
            for (String part : parts)
            {
                String[] objectPair = part.split("=");
                //Don't add this key/pair value if there is no value.
                if (objectPair.length <= 1) continue;
                toReturn.put(objectPair[0], objectPair[1].replaceAll("\\\\s", " "));
            }
        }
        return toReturn;
    }

}