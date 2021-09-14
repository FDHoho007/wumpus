package de.myfdweb.woc.wumpus;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class JsonObject {

    protected final JSONObject data;

    public JsonObject(JSONObject data) {
        this.data = data;
    }

    public Object getRaw(String key) {
        String[] keys = key.split("\\.");
        JSONObject o = data;
        for (int i = 0; i < keys.length - 1 && o != null; i++)
            o = (JSONObject) o.getOrDefault(keys[i], null);
        return o.getOrDefault(keys[keys.length - 1], null);
    }

    public JsonObject getObject(String key) {
        return new JsonObject((JSONObject) getRaw(key));
    }

    public ArrayList<String> keys() {
        return new ArrayList<String>(data.keySet());
    }

    public ArrayList<Object> values() {
        return new ArrayList<Object>(data.values());
    }

    public String getString(String key) {
        return (String) getRaw(key);
    }

    public Integer getInt(String key) {
        return (Integer) getRaw(key);
    }

    public Float getFloat(String key) {
        return (Float) getRaw(key);
    }

    public Double getDouble(String key) {
        return (Double) getRaw(key);
    }

    public Long getLong(String key) {
        return (Long) getRaw(key);
    }

    public Boolean getBoolean(String key) {
        return (Boolean) getRaw(key);
    }

    public <T> ArrayList<T> getList(String key) {
        ArrayList<T> list = new ArrayList<>();
        for (Object o : (JSONArray) getRaw(key))
            list.add((T) o);
        return list;
    }

    public Message getMessage(String key) {
        return getMessage(key, new HashMap<>());
    }

    public Message getMessage(String key, HashMap<String, String> replacements) {
        Object obj = getRaw(key);
        if (obj instanceof String)
            return new MessageBuilder(Utils.replace((String) obj, replacements)).build();
        else if (obj instanceof JSONObject)
            return new MessageBuilder(getEmbed((JSONObject) obj, replacements).build()).build();
        return null;
    }

    public EmbedBuilder getEmbed(JSONObject json, HashMap<String, String> replacements) {
        EmbedBuilder builder = new EmbedBuilder();
        if (json.containsKey("title")) {
            if (json.containsKey("url"))
                builder.setTitle(getString(json, "title", replacements), (String) json.get("url"));
            else
                builder.setTitle(getString(json, "title", replacements));
        }
        if (json.containsKey("description"))
            builder.setDescription(getString(json, "description", replacements));
        if (json.containsKey("thumbnail"))
            builder.setThumbnail((String) json.get("thumbnail"));
        if (json.containsKey("image"))
            builder.setImage((String) json.get("image"));
        if (json.containsKey("author")) {
            if (json.containsKey("authorUrl"))
                if (json.containsKey("authorIconUrl"))
                    builder.setAuthor(getString(json, "author", replacements), getString(json, "authorUrl", replacements), getString(json, "authorIconUrl", replacements));
                else
                    builder.setAuthor(getString(json, "author", replacements), getString(json, "authorUrl", replacements));
            else
                builder.setAuthor(getString(json, "author", replacements));
        }
        return builder;
    }

    private String getString(JSONObject json, String key, HashMap<String, String> replacements) {
        return Utils.replace((String) json.get(key), replacements);
    }

}
