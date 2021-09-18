package de.myfdweb.woc.wumpus.modules;

import de.myfdweb.woc.wumpus.Wumpus;
import de.myfdweb.woc.wumpus.api.GuildConfig;
import de.myfdweb.woc.wumpus.api.JsonObject;
import de.myfdweb.woc.wumpus.api.Module;
import de.myfdweb.woc.wumpus.api.SubscribeEvent;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ModPublicTalk extends Module {

    public ModPublicTalk(Wumpus botInstance) {
        super(botInstance);
    }

    @Override
    public String getId() {
        return "09afafbf-8b42-497c-b5fb-ad22f4ca61b9";
    }

    @Override
    @SubscribeEvent({GuildVoiceJoinEvent.class, GuildVoiceMoveEvent.class, GuildVoiceLeaveEvent.class})
    public void event(GenericGuildEvent event, GuildConfig gConfig, JsonObject config) {
        if (event instanceof GenericGuildVoiceUpdateEvent) {
            GenericGuildVoiceUpdateEvent e = (GenericGuildVoiceUpdateEvent) event;
            if ((e instanceof GuildVoiceMoveEvent || e instanceof GuildVoiceLeaveEvent) && isPublicTalk(config, Objects.requireNonNull(e.getChannelLeft())))
                update(Objects.requireNonNull(e.getChannelLeft().getParent()));
            // update muss nur einmal pro Kategorie ausgeführt werden
            else if ((e instanceof GuildVoiceMoveEvent || e instanceof GuildVoiceJoinEvent) && isPublicTalk(config, Objects.requireNonNull(e.getChannelJoined())))
                update(Objects.requireNonNull(e.getChannelJoined().getParent()));
        }
    }

    private boolean isPublicTalk(JsonObject config, VoiceChannel channel) {
        return (channel.getParent() != null && config.getList("categories").contains(channel.getParent().getIdLong()));
    }

    private void update(Category category) {
        ArrayList<ArrayList<VoiceChannel>> deleteQueue = new ArrayList<>();
        category.getVoiceChannels().forEach(voiceChannel -> {
            // Ist noch ein leerer Channel der selben Art vorhanden?
            if (voiceChannel.getMembers().size() != 0 && findChannels(voiceChannel).stream().noneMatch(ch -> ch.getMembers().size() == 0)) {
                // Alle Channel danach um 1 nach hinten verschieben
                category.getVoiceChannels().forEach(ch -> {
                    if (ch.getPosition() > voiceChannel.getPosition())
                        ch.getManager().setPosition(ch.getPosition() + 1).queue();
                });
                // Neuen Channel erstellen
                voiceChannel.createCopy().setPosition(voiceChannel.getPosition() + 1).queue();
            }
            // Finde ALLE leeren Channel der selben Art und packe sie in eine ArrayList
            Stream<VoiceChannel> stream = findChannels(voiceChannel).stream().filter(ch -> ch.getMembers().size() == 0);
            ArrayList<VoiceChannel> channels = new ArrayList<>();
            stream.forEach(channels::add);
            // Sicherstellen, dass deleteQueue nur eine ArrayList pro Channelart enthält, welche alle leeren Channel enthält
            if (channels.size() > 1 && !arrayListInArrayList(channels, deleteQueue))
                deleteQueue.add(channels);
        });
        deleteQueue.forEach(voiceChannelStream -> voiceChannelStream.forEach(new Consumer<VoiceChannel>() {
            boolean delete = false;

            // Bisschen umständlich aber überspringt den ersten leeren Channel, damit immer einer vorhanden ist
            public void accept(VoiceChannel channel) {
                if (this.delete)
                    channel.delete().queue();
                this.delete = true;
            }
        }));
    }

    private <T> boolean arrayListInArrayList(ArrayList<T> needle, ArrayList<ArrayList<T>> haystack) {
        for (ArrayList<T> list : haystack) {
            if (list.size() == needle.size()) {
                boolean found = true;
                for (T e : needle) {
                    if (!list.contains(e)) {
                        found = false;
                        break;
                    }
                }
                if (found)
                    return true;
            }
        }
        return false;
    }

    // Findet alle Channel mit dem selben Namen und selber Anzahl maximaler Clients innerhalb der eigenen Kategorie
    private ArrayList<VoiceChannel> findChannels(VoiceChannel channel) {
        ArrayList<VoiceChannel> channels = new ArrayList<>();
        Objects.requireNonNull(channel.getParent()).getVoiceChannels().forEach(channel1 -> {
            if (channel1.getName().equals(channel.getName()) && channel1.getUserLimit() == channel.getUserLimit())
                channels.add(channel1);
        });
        return channels;
    }

}
