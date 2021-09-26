package de.myfdweb.woc.wumpus;

import de.myfdweb.woc.wumpus.api.GuildConfig;
import de.myfdweb.woc.wumpus.api.JsonObject;
import de.myfdweb.woc.wumpus.api.Module;
import de.myfdweb.woc.wumpus.api.SubscribeEvent;
import de.myfdweb.woc.wumpus.modules.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Wumpus {

    // Module Ideas: Custom Echo Commands, Logging, Automatic Moderation (Clear Invites, External Links), Timed Messages, Giveaway, Levelsystem, Notifications (YouTube, Twitch, Instagram), Leaderboard (Join Button + Vanity Url), Spiele, Musik, Recording, Support, Events, Move Server, Temp Bans, Warnings, Subscriptions

    private static Wumpus instance;
    private JDA jda;
    private final HashMap<String, GuildConfig> config = new HashMap<>();

    public static void main(String[] args) {
        if (args.length == 1)
            instance = new Wumpus(args[0], new Class[]{ModWelcome.class, ModGoodbye.class, ModAutoRoles.class, ModReactionRoles.class, ModPrivateTalk.class, ModPublicTalk.class, ModEmbedEditor.class, ModAboutMe.class, ModDecorationRoles.class});
        else
            System.out.println("Syntax: java -jar Wumpus.jar <Bot Token>");
    }

    public static Wumpus getInstance() {
        return instance;
    }

    public Wumpus(String token, Class<? extends Module>[] moduleClasses) {
        try {
            for (File f : new File("config").listFiles())
                if (!f.getName().equals("default.json") && f.getName().endsWith(".json"))
                    loadGuildConfig(f.getName().substring(0, f.getName().length() - 5));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Writing guild configs...");
                for (String guildId : config.keySet()) writeGuildConfig(guildId, config.get(guildId));
            }));
            ArrayList<Module> modules = new ArrayList<>();
            this.jda = JDABuilder.createDefault(token).addEventListeners((EventListener) genericEvent -> {
                if (genericEvent instanceof GenericGuildEvent) {
                    GenericGuildEvent event = (GenericGuildEvent) genericEvent;
                    GuildConfig gConfig = Wumpus.this.getGuildConfig(event.getGuild());
                    for (Module mod : modules)
                        if (gConfig.isModuleActive(mod.getId()))
                            try {
                                Method m = mod.getClass().getMethod("event", GenericGuildEvent.class, GuildConfig.class, JsonObject.class);
                                if (m.isAnnotationPresent(SubscribeEvent.class) && Arrays.asList(m.getAnnotation(SubscribeEvent.class).value()).contains(genericEvent.getClass()))
                                    m.invoke(mod, event, gConfig, gConfig.getModConfig(mod.getId()));
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                } else if (genericEvent instanceof SlashCommandEvent) {
                    SlashCommandEvent event = (SlashCommandEvent) genericEvent;
                    GuildConfig gConfig = Wumpus.this.getGuildConfig(Objects.requireNonNull(event.getGuild()));
                    for (Module mod : modules)
                        if (mod.hasCommandData() && gConfig.isModuleActive(mod.getId()))
                            for (CommandData cmd : mod.getCommandData(gConfig))
                                if (cmd.getName().equals(event.getName())) {
                                    mod.onCommand(event, gConfig, gConfig.getModConfig(mod.getId()));
                                    break;
                                }
                } else if (genericEvent instanceof ButtonClickEvent) {
                    ButtonClickEvent event = (ButtonClickEvent) genericEvent;
                    GuildConfig gConfig = Wumpus.this.getGuildConfig(Objects.requireNonNull(event.getGuild()));
                    for (Module mod : modules)
                        if (gConfig.isModuleActive(mod.getId()))
                            mod.onClick(event, gConfig, gConfig.getModConfig(mod.getId()));
                }
            }).setChunkingFilter(ChunkingFilter.ALL).setMemberCachePolicy(MemberCachePolicy.ALL).enableIntents(GatewayIntent.GUILD_MEMBERS).setRawEventsEnabled(true).build().awaitReady();
            for (Class<? extends Module> modClass : moduleClasses)
                try {
                    modules.add(modClass.getConstructor(Wumpus.class).newInstance(this));
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            for (Guild g : this.jda.getGuilds()) {
                GuildConfig gConfig = this.getGuildConfig(g);
                ArrayList<String> mods = new ArrayList<>();
                for (Module mod : modules)
                    if (mod.getCommandData(gConfig) != null && gConfig.isModuleActive(mod.getId()))
                        mods.add(mod.getId());
                if (!Utils.sha256(String.join(";", mods)).equals(gConfig.getString("commandHash"))) {
                    CommandListUpdateAction action = g.updateCommands();
                    for (Module mod : modules)
                        if (mod.getCommandData(gConfig) != null && gConfig.isModuleActive(mod.getId()))
                            action = action.addCommands(mod.getCommandData(gConfig));
                    action.queue();
                    gConfig.set("commandHash", Utils.sha256(String.join(";", mods)));
                }
            }
        } catch (LoginException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public JDA getJDA() {
        return jda;
    }

    public GuildConfig getGuildConfig(Guild guild) {
        return getGuildConfig(guild.getId());
    }

    public GuildConfig getGuildConfig(String guildId) {
        if (!config.containsKey(guildId))
            loadGuildConfig(guildId);
        return config.get(guildId);
    }

    public void loadGuildConfig(String guildId) {
        try {
            if (config.containsKey(guildId))
                writeGuildConfig(guildId, config.get(guildId));
            File file = new File("config/" + guildId + ".json");
            if (!file.exists())
                Files.copy(new File("config/default.json").toPath(), file.toPath());
            config.put(guildId, new GuildConfig((JSONObject) new JSONParser().parse(new InputStreamReader(new FileInputStream(file)))));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void writeGuildConfig(String guildId, GuildConfig config) {
        try {
            File file = new File("config/" + guildId + ".json");
            if (!file.exists())
                assert file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(config.getData().toJSONString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
