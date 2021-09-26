package de.myfdweb.woc.wumpus.modules;

import de.myfdweb.woc.wumpus.Wumpus;
import de.myfdweb.woc.wumpus.api.GuildConfig;
import de.myfdweb.woc.wumpus.api.JsonObject;
import de.myfdweb.woc.wumpus.api.Module;
import de.myfdweb.woc.wumpus.api.SubscribeEvent;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ModPrivateTalk extends Module {

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
            if ((event instanceof GuildVoiceJoinEvent || event instanceof GuildVoiceMoveEvent) && config.getList("channels").contains(Objects.requireNonNull(e.getChannelJoined()).getIdLong())) {
                VoiceChannel c = e.getChannelJoined();
                c.getGuild().createVoiceChannel(config.getString("name")).setParent(c.getParent())
                        .addPermissionOverride(c.getGuild().getPublicRole(), Collections.emptyList(), Collections.singletonList(Permission.VIEW_CHANNEL))
                        .addPermissionOverride(c.getGuild().getRolesByName("Bot", false).get(0), Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList())
                        .addPermissionOverride(e.getMember(), Arrays.asList(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), Collections.emptyList()).queue(voiceChannel -> c.getGuild().moveVoiceMember(e.getMember(), voiceChannel).queue());
            }
            if (event instanceof GuildVoiceLeaveEvent || event instanceof GuildVoiceMoveEvent) {
                if (isPrivateTalk(Objects.requireNonNull(e.getChannelLeft()), config.getList("channels")) && e.getChannelLeft().getMembers().size() == 0)
                    e.getChannelLeft().delete().queue();
            }
        }
    }

    @Override
    public boolean hasCommandData() {
        return true;
    }

    @Override
    public CommandData[] getCommandData(GuildConfig gConfig) {
        return new CommandData[]{
                new CommandData("talk", "Private Talk verwalten")
                        .addSubcommands(
                        new SubcommandData("add", gConfig.getTranslation(this, "command.add.description"))
                                .addOption(OptionType.USER, "user", gConfig.getTranslation(this, "command.add.parameter"), true),
                        new SubcommandData("remove", gConfig.getTranslation(this, "command.remove.description"))
                                .addOption(OptionType.USER, "user", gConfig.getTranslation(this, "command.remove.parameter"), true),
                        new SubcommandData("promote", gConfig.getTranslation(this, "command.promote.description"))
                                .addOption(OptionType.USER, "user", gConfig.getTranslation(this, "command.promote.parameter"), true),
                        new SubcommandData("demote", gConfig.getTranslation(this, "command.demote.description"))
                                .addOption(OptionType.USER, "user", gConfig.getTranslation(this, "command.demote.parameter"), true),
                        new SubcommandData("purge", gConfig.getTranslation(this, "command.purge.description")),
                        new SubcommandData("access", gConfig.getTranslation(this, "command.access.description"))
                                .addOptions(new OptionData(OptionType.STRING, "access_type", gConfig.getTranslation(this, "command.access.parameter"), true)
                                        .addChoices(new Command.Choice(gConfig.getTranslation(this, "command.access.open"), "open"), new Command.Choice(gConfig.getTranslation(this, "command.access.restrict"), "restrict"), new Command.Choice(gConfig.getTranslation(this, "command.access.close"), "close")))
                )
        };
    }

    @Override
    public void onCommand(SlashCommandEvent event, GuildConfig gConfig, JsonObject config) {
        event.deferReply(true).queue();
        InteractionHook hook = event.getHook();
        Member invoker = event.getMember();
        if (event.getName().equals("talk") && event.getSubcommandName() != null && invoker != null) {
            if (event.getSubcommandName().equals("purge"))
                if (invoker.hasPermission(Permission.MANAGE_CHANNEL)) {
                    List<Long> channels = config.getList("channels");
                    for (Long channelId : channels)
                        for (VoiceChannel ch : Objects.requireNonNull(Objects.requireNonNull(invoker.getGuild().getVoiceChannelById(channelId)).getParent()).getVoiceChannels())
                            if (!channels.contains(ch.getIdLong()))
                                ch.getMembers().forEach((member) -> invoker.getGuild().kickVoiceMember(member).queue());
                    hook.editOriginal(gConfig.getTranslation(this, "command.purge.success")).queue();
                } else
                    hook.editOriginal(gConfig.getTranslation(this, "command.error.no_permission")).queue();
            else if (event.getSubcommandName().equals("access"))
                if (invoker.hasPermission(Permission.MANAGE_CHANNEL)) {
                    List<Long> channels = config.getList("channels");
                    Role role = config.getLong("role") == null ? null : invoker.getGuild().getRoleById(config.getLong("role"));
                    if (event.getOption("access_type").getAsString().equals("open")) {
                        for (Long channelId : channels) {
                            Objects.requireNonNull(invoker.getGuild().getVoiceChannelById(channelId)).getManager().putPermissionOverride(invoker.getGuild().getPublicRole(), Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList()).queue();
                            if (role != null)
                                Objects.requireNonNull(invoker.getGuild().getVoiceChannelById(channelId)).getManager().removePermissionOverride(role).queue();
                            hook.editOriginal(gConfig.getTranslation(this, "command.access.success_open")).queue();
                        }
                    } else if (event.getOption("access_type").getAsString().equals("restrict")) {
                        if (role != null)
                            for (Long channelId : channels) {
                                Objects.requireNonNull(invoker.getGuild().getVoiceChannelById(channelId)).getManager().putPermissionOverride(invoker.getGuild().getPublicRole(), Collections.emptyList(), Collections.singletonList(Permission.VIEW_CHANNEL)).queue();
                                Objects.requireNonNull(invoker.getGuild().getVoiceChannelById(channelId)).getManager().putPermissionOverride(role, Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList()).queue();
                                hook.editOriginal(gConfig.getTranslation(this, "command.access.success_restrict")).queue();
                            }
                        else
                            hook.editOriginal(gConfig.getTranslation(this, "command.error.no_role_set")).queue();
                    } else if (event.getOption("access_type").getAsString().equals("close")) {
                        for (Long channelId : channels) {
                            Objects.requireNonNull(invoker.getGuild().getVoiceChannelById(channelId)).getManager().putPermissionOverride(invoker.getGuild().getPublicRole(), Collections.emptyList(), Collections.singletonList(Permission.VIEW_CHANNEL)).queue();
                            if (role != null)
                                Objects.requireNonNull(invoker.getGuild().getVoiceChannelById(channelId)).getManager().removePermissionOverride(role).queue();
                            hook.editOriginal(gConfig.getTranslation(this, "command.access.success_close")).queue();
                        }
                    }
                } else
                    hook.editOriginal(gConfig.getTranslation(this, "command.error.no_permission")).queue();
            else if (invoker.getVoiceState() != null && Objects.requireNonNull(invoker.getVoiceState()).inVoiceChannel()) {
                VoiceChannel channel = invoker.getVoiceState().getChannel();
                Member target = Objects.requireNonNull(event.getOption("user")).getAsMember();
                if (channel != null && target != null)
                    if (isPrivateTalk(channel, config.getList("channels")))
                        if (invoker.hasPermission(channel, Permission.MANAGE_CHANNEL))
                            switch (event.getSubcommandName()) {
                                case "add":
                                    if (!target.hasPermission(channel, Permission.VIEW_CHANNEL)) {
                                        channel.getManager().putPermissionOverride(target, Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList()).queue();
                                        hook.editOriginal(String.format(gConfig.getTranslation(this, "command.add.success"), target.getEffectiveName())).queue();
                                    } else
                                        hook.editOriginal(String.format(gConfig.getTranslation(this, "command.add.fail"), target.getEffectiveName())).queue();
                                    break;
                                case "remove":
                                    if (target.hasPermission(channel, Permission.VIEW_CHANNEL)) {
                                        channel.getManager().removePermissionOverride(target).queue();
                                        boolean kick = target.getVoiceState() != null && target.getVoiceState().getChannel() != null && target.getVoiceState().getChannel().equals(channel);
                                        if (kick)
                                            channel.getGuild().kickVoiceMember(target).queue();
                                        hook.editOriginal(String.format(gConfig.getTranslation(this, kick ? "command.remove.success_1" : "command.remove.success_2"), target.getEffectiveName())).queue();
                                    } else
                                        hook.editOriginal(String.format(gConfig.getTranslation(this, "command.remove.fail"), target.getEffectiveName())).queue();
                                    break;
                                case "promote":
                                    if (!target.hasPermission(channel, Permission.MANAGE_CHANNEL)) {
                                        boolean newUser = !target.hasPermission(channel, Permission.VIEW_CHANNEL);
                                        channel.getManager().putPermissionOverride(target, Arrays.asList(Permission.VIEW_CHANNEL, Permission.MANAGE_CHANNEL), Collections.emptyList()).queue();
                                        hook.editOriginal(String.format(gConfig.getTranslation(this, newUser ? "command.promote.success_1" : "command.promote.success_2"), target.getEffectiveName())).queue();
                                    } else
                                        hook.editOriginal(String.format(gConfig.getTranslation(this, "command.promote.fail"), target.getEffectiveName())).queue();
                                    break;
                                case "demote":
                                    if (target.hasPermission(channel, Permission.MANAGE_CHANNEL)) {
                                        channel.getManager().putPermissionOverride(target, Collections.singletonList(Permission.VIEW_CHANNEL), Collections.emptyList()).queue();
                                        hook.editOriginal(String.format(gConfig.getTranslation(this, "command.demote.success"), target.getEffectiveName())).queue();
                                    } else
                                        hook.editOriginal(String.format(gConfig.getTranslation(this, "command.demote.fail"), target.getEffectiveName())).queue();
                                    break;
                            }
                        else
                            hook.editOriginal(gConfig.getTranslation(this, "command.error.not_your_private_talk")).queue();
                    else
                        hook.editOriginal(gConfig.getTranslation(this, "command.error.no_private_talk")).queue();
            } else
                hook.editOriginal(gConfig.getTranslation(this, "command.error.not_in_channel")).queue();
        }
    }

    public boolean isPrivateTalk(VoiceChannel channel, List<Long> channels) {
        if (!channels.contains(channel.getIdLong()) && channel.getParent() != null)
            for (Long channelId : channels)
                if (channel.getParent().equals(Objects.requireNonNull(channel.getGuild().getVoiceChannelById(channelId)).getParent()))
                    return true;
        return false;
    }

}
