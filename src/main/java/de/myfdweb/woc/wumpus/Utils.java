package de.myfdweb.woc.wumpus;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

public class Utils {

    private static final HashMap<String, String> emojis = new HashMap<>();

    public static HashMap<String, String> hashMap(String... values) {
        if (values.length % 2 == 0) {
            HashMap<String, String> map = new HashMap<>();
            for (int i = 0; i < values.length; i += 2)
                map.put(values[i], values[i + 1]);
            return map;
        }
        return new HashMap<>();
    }

    public static String replace(String txt, HashMap<String, String> replacements) {
        for (String r : replacements.keySet())
            txt = txt.replace(r, replacements.get(r));
        return txt;
    }

    static {
        try {
            JSONObject obj = (JSONObject) new JSONParser().parse(new InputStreamReader(Objects.requireNonNull(Utils.class.getResourceAsStream("/discord_emojis.json"))));
            for (Object name : obj.keySet())
                emojis.put((String) name, (String) obj.get(name));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static String getEmoji(String name) {
        return emojis.getOrDefault(name, null);
    }

    public static String getEmojiName(String unicodeEmoji) {
        for (String emojiName : emojis.keySet())
            if (emojis.get(emojiName).equals(unicodeEmoji))
                return emojiName;
        return null;
    }

    public static Color hexToColor(String hex) throws NumberFormatException {
        hex = hex.replace("#", "");
        switch (hex.length()) {
            case 6:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16));
            case 8:
                return new Color(
                        Integer.valueOf(hex.substring(0, 2), 16),
                        Integer.valueOf(hex.substring(2, 4), 16),
                        Integer.valueOf(hex.substring(4, 6), 16),
                        Integer.valueOf(hex.substring(6, 8), 16));
        }
        return null;
    }

    public static String sha256(String msg) {
        try {
            return bytesToHex(MessageDigest.getInstance("SHA-256").digest(msg.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return String.valueOf(hexChars);
    }

}
