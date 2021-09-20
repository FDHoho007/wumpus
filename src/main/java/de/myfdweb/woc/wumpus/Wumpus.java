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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class Wumpus {

    private static Wumpus instance;
    private JDA jda;
    private final HashMap<String, GuildConfig> config = new HashMap<>();

    public static void main(String[] args) {
        if (args.length == 1)
            instance = new Wumpus(args[0], new Class[]{ModWelcome.class, ModGoodbye.class, ModAutoRoles.class, ModReactionRoles.class, ModPrivateTalk.class, ModPublicTalk.class});
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
                }
            }).setChunkingFilter(ChunkingFilter.ALL).setMemberCachePolicy(MemberCachePolicy.ALL).enableIntents(GatewayIntent.GUILD_MEMBERS).setRawEventsEnabled(true).build().awaitReady();
            for (Class<? extends Module> modClass : moduleClasses)
                try {
                    Module mod = modClass.getConstructor(Wumpus.class).newInstance(this);
                    if (mod.hasCommandData())
                        for (Guild g : this.jda.getGuilds()) {
                            GuildConfig gConfig = this.getGuildConfig(g);
                            JsonObject config = gConfig.getModConfig(mod.getId());
                            if (gConfig.isModuleActive(mod.getId()) && config.getList("commands") == null)
                                g.updateCommands().addCommands(mod.getCommandData(gConfig)).queue(commands -> {
                                    ArrayList<Long> ids = new ArrayList<>();
                                    for (Command cmd : commands)
                                        ids.add(cmd.getIdLong());
                                    config.set("commands", ids);
                                });
                            else if (!gConfig.isModuleActive(mod.getId()) && config.getList("commands") != null) {
                                config.getList("commands").forEach(o -> g.deleteCommandById((long) o).queue());
                                config.delete("commands");
                            }
                        }
                    modules.add(mod);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    e.printStackTrace();
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
