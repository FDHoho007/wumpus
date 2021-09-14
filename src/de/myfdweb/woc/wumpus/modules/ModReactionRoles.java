package de.myfdweb.woc.wumpus.modules;

import de.myfdweb.woc.wumpus.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.react.GenericGuildMessageReactionEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;

public class ModReactionRoles extends Module {

    public ModReactionRoles(Wumpus botInstance) {
        super(botInstance);
    }

    @Override
    public String getId() {
        return "b8ba9570-7c23-41e3-985b-20f9a3c84427";
    }

    @Override
    @SubscribeEvent({GuildMessageReactionAddEvent.class, GuildMessageReactionRemoveEvent.class, GuildMessageReactionRemoveAllEvent.class, GuildMemberRemoveEvent.class})
    public void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config) {
        if (event instanceof GuildMemberRemoveEvent) {
            TextChannel channel = event.getGuild().getTextChannelById(config.getLong("channel"));
            if (channel != null) {
                Message m = channel.retrieveMessageById(config.getLong("message")).complete();
                for (MessageReaction reaction : m.getReactions())
                    reaction.removeReaction(((GuildMemberRemoveEvent) event).getUser()).queue();
            }
        } else if (event instanceof GuildMessageReactionRemoveAllEvent) {
            if (((GuildMessageReactionRemoveAllEvent) event).getMessageIdLong() == config.getLong("message")) {
                for (Object id : config.getObject("roles").values()) {
                    Role r = event.getGuild().getRoleById((Long) id);
                    event.getGuild().findMembersWithRoles(r).onSuccess(members -> members.forEach(member -> event.getGuild().removeRoleFromMember(member, r).queue()));
                }
            }
        } else {
            GenericGuildMessageReactionEvent e = (GenericGuildMessageReactionEvent) event;
            if (e.getMember() != null && !e.getJDA().getSelfUser().equals(e.getUser()) && e.getMessageIdLong() == config.getLong("message")) {
                Long id = config.getObject("roles").getLong(e.getReactionEmote().isEmoji() ? e.getReactionEmote().getEmoji() : e.getReactionEmote().getName());
                if (id != null) {
                    Role r = e.getGuild().getRoleById(id);
                    if (r != null) {
                        if (e instanceof GuildMessageReactionAddEvent)
                            e.getGuild().addRoleToMember(e.getMember(), r).queue();
                        else if (e instanceof GuildMessageReactionRemoveEvent)
                            e.getGuild().removeRoleFromMember(e.getMember(), r).queue();
                    }
                }
            }
        }
    }
}
