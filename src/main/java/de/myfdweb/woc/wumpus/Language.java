package de.myfdweb.woc.wumpus;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;

public enum Language {

    de_DE;

    private JSONObject data;

    Language() {
        try {
            this.data = (JSONObject) new JSONParser().parse(new InputStreamReader(getClass().getResourceAsStream("/lang/" + name() + ".json")));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public String getString(String key) {
        String[] keys = key.split("\\.");
        JSONObject o = data;
        for (int i = 0; i < keys.length - 1; i++)
            o = (JSONObject) o.get(keys[i]);
        return (String) o.get(keys[keys.length - 1]);
    }

}
