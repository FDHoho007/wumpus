package de.myfdweb.woc.ultimatediscordbot;

import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

public abstract class Module {

    private final UDB botInstance;

    public Module(UDB botInstance) {
        this.botInstance = botInstance;
    }

    public UDB getBotInstance() {
        return botInstance;
    }

    public abstract String getId();

    public abstract void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config);

}
