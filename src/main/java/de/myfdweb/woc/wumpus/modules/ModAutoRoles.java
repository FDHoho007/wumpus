package de.myfdweb.woc.wumpus.modules;

import de.myfdweb.woc.wumpus.*;
import de.myfdweb.woc.wumpus.api.GuildConfig;
import de.myfdweb.woc.wumpus.api.JsonObject;
import de.myfdweb.woc.wumpus.api.Module;
import de.myfdweb.woc.wumpus.api.SubscribeEvent;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

public class ModAutoRoles extends Module {

    public ModAutoRoles(Wumpus botInstance) {
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
