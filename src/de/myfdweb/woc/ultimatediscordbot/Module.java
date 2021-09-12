package de.myfdweb.woc.ultimatediscordbot.module;

import de.myfdweb.woc.ultimatediscordbot.UDB;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class Module extends ListenerAdapter {

    private final UDB botInstance;

    public Module(UDB botInstance) {
        this.botInstance = botInstance;
    }

    public UDB getBotInstance() {
        return botInstance;
    }

    public abstract String getId();

}
