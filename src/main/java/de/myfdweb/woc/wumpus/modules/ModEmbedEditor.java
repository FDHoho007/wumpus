package de.myfdweb.woc.wumpus.modules;

import de.myfdweb.woc.wumpus.Utils;
import de.myfdweb.woc.wumpus.Wumpus;
import de.myfdweb.woc.wumpus.api.GuildConfig;
import de.myfdweb.woc.wumpus.api.JsonObject;
import de.myfdweb.woc.wumpus.api.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Objects;

public class ModEmbedEditor extends Module {

    public ModEmbedEditor(Wumpus botInstance) {
        super(botInstance);
    }

    @Override
    public String getId() {
        return "984b7f8e-5ed5-416f-aee0-6cbb81dd0248";
    }

    @Override
    public boolean hasCommandData() {
        return true;
    }

    @Override
    public CommandData[] getCommandData(GuildConfig gConfig) {
        return new CommandData[]{
                new CommandData("embed", "Eingebettete Nachricht erstellen")
                        .addOption(OptionType.STRING, "id", "ID einer bestehenden eingebetteten Nachricht, die bearbeitet werden soll", false)
                        .addOption(OptionType.CHANNEL, "channel", "Channel, in dem die eingebettete Nachricht gesendet werden soll", false)
                        .addOption(OptionType.STRING, "title", "Titel der Nachricht", false)
                        .addOption(OptionType.STRING, "description", "Beschreibung der Nachricht", false)
                        .addOption(OptionType.STRING, "author", "Autor der Nachricht", false)
                        .addOption(OptionType.STRING, "author_url", "URL des Autors der Nachricht", false)
                        .addOption(OptionType.STRING, "author_icon", "Bild des Autors der Nachricht", false)
                        .addOption(OptionType.STRING, "color", "Farbe der Nachricht", false)
                        .addOption(OptionType.STRING, "footer", "Footer Text", false)
                        .addOption(OptionType.STRING, "footer_icon", "Footer Bild", false)
                        .addOption(OptionType.STRING, "fields", "Felder. Syntax: name=value (inline), name==value (block)", false)
        };
    }

    @Override
    public void onCommand(SlashCommandEvent event, GuildConfig gConfig, JsonObject config) {
        event.deferReply(true).queue();
        InteractionHook hook = event.getHook();
        Member invoker = event.getMember();
        if (invoker != null && invoker.hasPermission(Permission.MESSAGE_MANAGE)) {
            boolean oneparamset = false;
            EmbedBuilder builder = new EmbedBuilder();
            if (event.getOption("title") != null) {
                oneparamset = true;
                builder.setTitle(Objects.requireNonNull(event.getOption("title")).getAsString());
            }
            if (event.getOption("description") != null) {
                oneparamset = true;
                builder.setDescription(Objects.requireNonNull(event.getOption("description")).getAsString());
            }
            if (event.getOption("author") != null) {
                oneparamset = true;
                OptionMapping url = event.getOption("author_url");
                OptionMapping icon = event.getOption("author_icon");
                builder.setAuthor(Objects.requireNonNull(event.getOption("author")).getAsString(), url == null ? null : url.getAsString(), icon == null ? null : icon.getAsString());
            }
            if (event.getOption("color") != null) {
                oneparamset = true;
                try {
                    builder.setColor(Utils.hexToColor(Objects.requireNonNull(event.getOption("color")).getAsString()));
                } catch (NumberFormatException e) {
                    hook.editOriginal("Bitte gib eine gültige Farbe an.").queue();
                    return;
                }
            }
            if (event.getOption("footer") != null) {
                oneparamset = true;
                OptionMapping footer_icon = event.getOption("footer_icon");
                builder.setFooter(Objects.requireNonNull(event.getOption("footer")).getAsString(), footer_icon == null ? null : footer_icon.getAsString());
            }
            if (event.getOption("fields") != null) {
                oneparamset = true;
                String[] split = Objects.requireNonNull(event.getOption("fields")).getAsString().split("=");
                for (int i = 0; i < split.length; i++) {
                    String name = split[i];
                    boolean inline = split[++i].equals("");
                    String value = split[inline ? ++i : i];
                    builder.addField(name, value, inline);
                }
            }
            if (oneparamset) {
                if (event.getOption("id") != null) {
                    ((TextChannel) Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel()).editMessageById(Long.parseLong(Objects.requireNonNull(event.getOption("id")).getAsString()), builder.build()).queue();
                    hook.editOriginal("Here you go!").queue();
                } else if (event.getOption("channel") != null)
                    if (Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel() instanceof TextChannel) {
                        ((TextChannel) Objects.requireNonNull(event.getOption("channel")).getAsGuildChannel()).sendMessage(builder.build()).queue();
                        hook.editOriginal("Here you go!").queue();
                    } else
                        hook.editOriginal("Bitte gib einen Text Kanal an.").queue();
                else
                    hook.editOriginal("Bitte gib entweder einen Channel oder eine Nachrichten-ID an.").queue();
            } else
                hook.editOriginal("Es ist mindestens ein Parameter für eine Nachricht erforderlich.").queue();
        }
    }
}
