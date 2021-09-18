package de.myfdweb.woc.wumpus.modules;

import de.myfdweb.woc.wumpus.Wumpus;
import de.myfdweb.woc.wumpus.api.GuildConfig;
import de.myfdweb.woc.wumpus.api.JsonObject;
import de.myfdweb.woc.wumpus.api.Module;
import de.myfdweb.woc.wumpus.api.SubscribeEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class ModPrivateTalk extends Module {

    private final HashMap<Long, Long> privateTalk = new HashMap<>();

    public ModPrivateTalk(Wumpus botInstance) {
        super(botInstance);
    }

    @Override
    public String getId() {
        return "c6c95033-12fd-4d25-9fb6-93ab680b663e";
    }

    @Override
    @SubscribeEvent({GuildVoiceJoinEvent.class, GuildVoiceMoveEvent.class, GuildVoiceLeaveEvent.class})
    public void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config) {
        if (event instanceof GenericGuildVoiceUpdateEvent) {
            GenericGuildVoiceUpdateEvent e = (GenericGuildVoiceUpdateEvent) event;
            if ((event instanceof GuildVoiceJoinEvent || event instanceof GuildVoiceMoveEvent) && config.getList("channels").contains(Objects.requireNonNull(e.getChannelJoined()).getIdLong()))
                create(Objects.requireNonNull(e.getChannelJoined()), e.getMember(), config.getString("name"));
            if (event instanceof GuildVoiceLeaveEvent || event instanceof GuildVoiceMoveEvent)
                delete(Objects.requireNonNull(e.getChannelLeft()));
        }
    }

    @Override
    public CommandData[] getCommandData() {
        return new CommandData[]{
                new CommandData("talk", "Private Talks verwalten")
                        .addSubcommands(
                        new SubcommandData("add", "Füge deinem Private Talk einen weiteren Nutzer hinzu.")
                                .addOption(OptionType.USER, "user", "den hinzuzufügenden Nutzer", true),
                        new SubcommandData("remove", "Entferne einen Nutzer aus deinem Private Talk.")
                                .addOption(OptionType.USER, "user", "den zu entfernenden Nutzer", true),
                        new SubcommandData("promote", "Befördere einen Nutzer in deinem Private Talk, sodass auch er Nutzer verwalten kann.")
                                .addOption(OptionType.USER, "user", "den zu befördernden Nutzer", true),
                        new SubcommandData("demote", "Degradiere einen Nutzer mit erhöhten Rechten.")
                                .addOption(OptionType.USER, "user", "den zu degradierenden Nutzer", true),
                        new SubcommandData("purge", "Löst alle Private Talks auf (administrativ)."),
                        new SubcommandData("access", "Stellt die Anforderung für das Erstellen neuer Private Talks ein.")
                                .addOptions(
                                        new OptionData(OptionType.STRING, "access_type", "Anforderung für das Erstellen neuer Private Talks", true)
                                                .addChoices(new Command.Choice("Jeder", "open"), new Command.Choice("Nur mit Rolle", "restrict"), new Command.Choice("Geschlossen", "close")))
                )
        };
    }

    @Override
    public void onCommand(SlashCommandEvent event) {
        event.reply("Pong").queue();
//        DataObject o = event.getPayload().getObject("data").getArray("options").getObject(0);
//        String action = o.getString("name");
//        Member invoker = (Member)this.guild.retrieveMemberById(event.getPayload().getObject("member").getObject("user").getString("id")).complete(), target = (Member)this.guild.retrieveMemberById(o.getArray("options").getObject(0).getString("value")).complete();
//        if (invoker.getVoiceState() != null && invoker.getVoiceState().inVoiceChannel()) {
//            VoiceChannel channel = invoker.getVoiceState().getChannel();
//            if (channel != null && invoker.hasPermission((GuildChannel)channel, new Permission[] { Permission.VOICE_MOVE_OTHERS }))
//                if (action.equalsIgnoreCase("add") && channel.getPermissionOverride((IPermissionHolder)target) == null) {
//                    channel.getManager().putPermissionOverride((IPermissionHolder)target, Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList()).queue();
//                } else if (action.equalsIgnoreCase("remove") && channel.getPermissionOverride((IPermissionHolder)target) != null) {
//                    channel.getManager().removePermissionOverride((IPermissionHolder)target).queue();
//                    if (target.getVoiceState() != null && target.getVoiceState().getChannel() != null && target.getVoiceState().getChannel().equals(channel))
//                        this.guild.kickVoiceMember(target).queue();
//                } else if (action.equalsIgnoreCase("promote") && !target.hasPermission((GuildChannel)channel, new Permission[] { Permission.VOICE_MOVE_OTHERS })) {
//                    channel.getManager().putPermissionOverride((IPermissionHolder)target, Arrays.asList(new Permission[] { Permission.VIEW_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_DEAF_OTHERS, Permission.VOICE_MOVE_OTHERS }, ), Collections.emptyList()).queue();
//                } else if (action.equalsIgnoreCase("demote") && target.hasPermission((GuildChannel)channel, new Permission[] { Permission.VOICE_MOVE_OTHERS })) {
//                    channel.getManager().putPermissionOverride((IPermissionHolder)target, Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList()).queue();
//                }
//        }
    }

    private void create(VoiceChannel c, Member m, String name) {
        c.getGuild().createVoiceChannel(name).setParent(c.getParent())
                .addPermissionOverride(c.getGuild().getPublicRole(), Collections.emptyList(), Collections.singletonList(Permission.VIEW_CHANNEL))
                .addPermissionOverride(c.getGuild().getRolesByName("Bot", false).get(0), Arrays.asList(Permission.MANAGE_PERMISSIONS, Permission.VIEW_CHANNEL), Collections.emptyList())
                .addPermissionOverride(m, Arrays.asList(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL, Permission.VOICE_MUTE_OTHERS, Permission.VOICE_DEAF_OTHERS, Permission.VOICE_MOVE_OTHERS), Collections.emptyList()).queue(voiceChannel -> {
                    this.privateTalk.put(voiceChannel.getIdLong(), c.getGuild().getIdLong());
                    c.getGuild().moveVoiceMember(m, voiceChannel).queue();
                });
    }

    private void delete(VoiceChannel c) {
        if (this.privateTalk.containsKey(c.getIdLong()) && c.getMembers().size() == 0)
            c.delete().queue(unused -> this.privateTalk.remove(c.getIdLong()));
    }

    public void purge(Long guildId) {
        for (Long channelId : this.privateTalk.keySet())
            if (this.privateTalk.get(channelId).equals(guildId)) {
                VoiceChannel channel = getBotInstance().getJDA().getVoiceChannelById(channelId);
                if (channel != null)
                    channel.getMembers().forEach((member) -> channel.getGuild().kickVoiceMember(member).queue());
            }
    }

}
