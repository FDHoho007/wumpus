package de.myfdweb.woc.wumpus;

import net.dv8tion.jda.api.events.guild.GenericGuildEvent;

public abstract class Module {

    private final Wumpus botInstance;

    public Module(Wumpus botInstance) {
        this.botInstance = botInstance;
    }

    public Wumpus getBotInstance() {
        return botInstance;
    }

    public abstract String getId();

    public abstract void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config);

}
