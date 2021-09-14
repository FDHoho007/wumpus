package de.myfdweb.woc.wumpus.modules;

import de.myfdweb.woc.wumpus.*;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class ModWelcome extends Module {

    public ModWelcome(Wumpus botInstance) {
        super(botInstance);
    }

    @Override
    public String getId() {
        return "27203ff6-f3b6-4de6-b664-0180643031f0";
    }

    @Override
    @SubscribeEvent({GuildMemberJoinEvent.class})
    public void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config) {
        event.getGuild().getTextChannelById(config.getLong("channel")).sendMessage(config.getMessage("message", Utils.hashMap("%user%", "<@" + ((GuildMemberJoinEvent) event).getMember().getId() + ">"))).queue();
    }
}
