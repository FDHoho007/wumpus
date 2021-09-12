package de.myfdweb.woc.ultimatediscordbot;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Utils {

    private static HashMap<String, String> emojis = new HashMap<>();

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
            JSONObject obj = (JSONObject) new JSONParser().parse(new InputStreamReader(Utils.class.getResourceAsStream("/discord_emojis.json")));
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

}
