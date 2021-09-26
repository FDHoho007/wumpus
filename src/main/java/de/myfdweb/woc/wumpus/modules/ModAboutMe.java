package de.myfdweb.woc.wumpus.modules;

import de.myfdweb.woc.wumpus.Utils;
import de.myfdweb.woc.wumpus.Wumpus;
import de.myfdweb.woc.wumpus.api.GuildConfig;
import de.myfdweb.woc.wumpus.api.JsonObject;
import de.myfdweb.woc.wumpus.api.Module;
import de.myfdweb.woc.wumpus.api.SubscribeEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModAboutMe extends Module {

    public ModAboutMe(Wumpus botInstance) {
        super(botInstance);
    }

    @Override
    public String getId() {
        return "2320e5d6-8939-4658-b7f6-7972874d8eb5";
    }

    @Override
    public boolean hasCommandData() {
        return true;
    }

    @Override
    public CommandData[] getCommandData(GuildConfig gConfig) {
        return new CommandData[]{new CommandData("createaboutme", gConfig.getTranslation(this, "command.description"))};
    }

    @Override
    public void onCommand(SlashCommandEvent event, GuildConfig gConfig, JsonObject config) {
        event.deferReply(true).queue();
        InteractionHook hook = event.getHook();
        for (Role r : Objects.requireNonNull(event.getMember()).getRoles())
            if (r.getIdLong() == gConfig.getBotAdmin()) {
                event.getTextChannel().sendMessage(new EmbedBuilder().setDescription(gConfig.getTranslation(this, "initiationMessage")).setColor(Utils.hexToColor("#5865f2")).build()).setActionRow(Button.primary("aboutMe/-1/-1", gConfig.getTranslation(this, "initiationButton"))).queue();
                hook.editOriginal(gConfig.getTranslation(this, "command.success")).queue();
                return;
            }
        hook.editOriginal(gConfig.getTranslation(this, "command.error")).queue();
    }

    @Override
    public void onClick(ButtonClickEvent event, GuildConfig gConfig, JsonObject config) {
        Pattern pattern = Pattern.compile("aboutMe\\/(-1|[0-9]+)\\/(-1|[0-9]+)");
        Matcher m;
        if ((m = pattern.matcher(event.getComponentId())).matches()) {
            int index = Integer.parseInt(m.group(1));
            int option = Integer.parseInt(m.group(2));
            if (index > -1) {
                int i = 0;
                for(Object id : new JsonObject((JSONObject) config.getList("properties").get(index)).getList("roles"))
                    if(option < 0)
                        Objects.requireNonNull(event.getGuild()).removeRoleFromMember(Objects.requireNonNull(event.getMember()), Objects.requireNonNull(event.getGuild().getRoleById((Long) id))).queue();
                    else if(option == i++)
                        Objects.requireNonNull(event.getGuild()).addRoleToMember(Objects.requireNonNull(event.getMember()), Objects.requireNonNull(event.getGuild().getRoleById((Long) id))).queue();
            }
            if (++index >= config.getList("properties").size())
                event.reply(gConfig.getTranslation(this, "setupFinished")).setEphemeral(true).queue();
            else {
                JsonObject property = new JsonObject((JSONObject) config.getList("properties").get(index));
                event.reply(String.format(gConfig.getTranslation(this, "setupMessage"), property.getString("name"))).setEphemeral(true).addActionRow(buildButtons(event.getGuild(), index, property, Objects.requireNonNull(event.getMember()).getRoles())).queue();
            }
        }
    }


    private Component[] buildButtons(Guild guild, int index, JsonObject property, List<Role> roles) {
        ArrayList<Long> roleIds = new ArrayList<>();
        roles.forEach(role -> roleIds.add(role.getIdLong()));
        ArrayList<Component> buttons = new ArrayList<>();
        boolean hasValue = false;
        int i = 0;
        for (Object id : property.getList("roles")) {
            String value = Objects.requireNonNull(guild.getRoleById((Long) id)).getName();
            if (roleIds.contains((Long) id)) {
                hasValue = true;
                buttons.add(Button.primary("aboutMe/" + index + "/" + i++, value));
            } else buttons.add(Button.secondary("aboutMe/" + index + "/" + i++, value));
        }
        if (hasValue)
            buttons.add(Button.danger("aboutMe/" + index + "/-1", "Entfernen"));
        return buttons.toArray(new Component[0]);
    }

    @Override
    @SubscribeEvent({GuildMemberRoleAddEvent.class})
    public void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config) {
        if (event instanceof GuildMemberRoleAddEvent) {
            ArrayList<Object> properties = config.getList("properties");
            ArrayList<Long> roles;
            for (Role r : ((GuildMemberRoleAddEvent) event).getRoles())
                for (Object property : properties)
                    if ((roles = new JsonObject((JSONObject) property).getList("roles")).contains(r.getIdLong()))
                        for (Long id : roles)
                            if (!id.equals(r.getIdLong()))
                                r.getGuild().removeRoleFromMember(((GuildMemberRoleAddEvent) event).getMember(), Objects.requireNonNull(r.getGuild().getRoleById(id))).queue();
        }
    }
}
