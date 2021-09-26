package de.myfdweb.woc.wumpus.modules;

import de.myfdweb.woc.wumpus.Utils;
import de.myfdweb.woc.wumpus.Wumpus;
import de.myfdweb.woc.wumpus.api.GuildConfig;
import de.myfdweb.woc.wumpus.api.JsonObject;
import de.myfdweb.woc.wumpus.api.Module;
import de.myfdweb.woc.wumpus.api.SubscribeEvent;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModDecorationRoles extends Module {

    public ModDecorationRoles(Wumpus botInstance) {
        super(botInstance);
    }

    @Override
    public String getId() {
        return "bf1ce435-4b7d-4ef5-a811-22273299cabc";
    }

    @Override
    @SubscribeEvent({GuildMemberRoleAddEvent.class, GuildMemberRoleRemoveEvent.class})
    public void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config) {
        GenericGuildMemberEvent e = (GenericGuildMemberEvent) event;
        Color c = Utils.hexToColor("#292b2f");
        assert c != null;
        HashMap<Role, ArrayList<Role>> roles = new HashMap<>();
        ArrayList<Role> rolesTmp = new ArrayList<>();
        for (Role r : event.getGuild().getRoles()) {
            if (c.equals(r.getColor())) {
                roles.put(r, rolesTmp);
                rolesTmp = new ArrayList<>();
            } else
                rolesTmp.add(r);
        }
        List<Role> myRoles = e.getMember().getRoles();
        for(Role r1 : roles.keySet()) {
            boolean shouldHave = false;
            for(Role r2 : roles.get(r1))
                if(myRoles.contains(r2)) {
                    shouldHave = true;
                    break;
                }
            if(shouldHave && !myRoles.contains(r1))
                event.getGuild().addRoleToMember(e.getMember(), r1).queue();
            else if(!shouldHave && myRoles.contains(r1))
                event.getGuild().removeRoleFromMember(e.getMember(), r1).queue();
        }
    }
}
