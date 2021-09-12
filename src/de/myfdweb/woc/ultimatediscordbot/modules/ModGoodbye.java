package de.myfdweb.woc.ultimatediscordbot.modules;

import de.myfdweb.woc.ultimatediscordbot.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;

public class ModGoodbye extends Module {

    public ModGoodbye(UDB botInstance) {
        super(botInstance);
    }

    @Override
    public String getId() {
        return "dc2a6a29-ce08-44d1-92db-90f165ca7693";
    }

    @Override
    @SubscribeEvent({GuildMemberRemoveEvent.class})
    public void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config) {
        event.getGuild().getTextChannelById(config.getLong("channel")).sendMessage(config.getMessage("message", Utils.hashMap("%user%", "<@" + ((GuildMemberRemoveEvent) event).getMember().getId() + ">"))).queue();
    }
}
