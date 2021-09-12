package de.myfdweb.woc.ultimatediscordbot;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class GuildConfig extends JsonObject {

    private final Language language;
    private final ArrayList<String> modulesActive;

    public GuildConfig(JSONObject data) {
        super(data);
        language = Language.valueOf(getString("language"));
        modulesActive = new ArrayList<>();
        for(Object o : getList("modulesActive"))
            modulesActive.add((String) o);
    }

    public Language getLanguage() {
        return language;
    }

    public ArrayList<String> getModulesActive() {
        return modulesActive;
    }

    public boolean isModuleActive(String moduleId) {
        return modulesActive.contains(moduleId);
    }

    public JsonObject getModConfig(String moduleId) {
        JSONObject json = (JSONObject) getRaw("moduleConfig." + moduleId);
        return json == null ? null : new JsonObject(json);
    }
}
