package de.myfdweb.woc.wumpus.api;

import de.myfdweb.woc.wumpus.Wumpus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class Module {

    private final Wumpus botInstance;

    public Module(Wumpus botInstance) {
        this.botInstance = botInstance;
    }

    public Wumpus getBotInstance() {
        return botInstance;
    }

    public abstract String getId();

    public void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config) {}

    public boolean hasCommandData() { return false; }

    public CommandData[] getCommandData(GuildConfig gConfig) {
        return null;
    }

    public void onCommand(SlashCommandEvent event, GuildConfig gConfig, JsonObject config) {}

    public void onClick(ButtonClickEvent event, GuildConfig gConfig, JsonObject config) {}

}
