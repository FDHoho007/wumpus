package de.myfdweb.woc.ultimatediscordbot.modules;

import de.myfdweb.woc.ultimatediscordbot.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class ModAutoRoles extends Module {

    public ModAutoRoles(UDB botInstance) {
        super(botInstance);
    }

    @Override
    public String getId() {
        return "ef184f35-42bb-4771-9201-bf6cf8f97713";
    }

    @Override
    @SubscribeEvent({GuildMemberJoinEvent.class})
    public void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config) {
        Member m = ((GuildMemberJoinEvent) event).getMember();
        for (Object o : config.getList("roles")) {
            Role role = event.getGuild().getRoleById((Long) o);
            if (role != null)
                event.getGuild().addRoleToMember(m, role).queue();
        }
    }
}
