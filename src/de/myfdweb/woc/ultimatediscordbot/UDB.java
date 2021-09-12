package de.myfdweb.woc.ultimatediscordbot;

import de.myfdweb.woc.ultimatediscordbot.modules.ModAutoRoles;
import de.myfdweb.woc.ultimatediscordbot.modules.ModGoodbye;
import de.myfdweb.woc.ultimatediscordbot.modules.ModReactionRoles;
import de.myfdweb.woc.ultimatediscordbot.modules.ModWelcome;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.hooks.EventListener;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UDB {

    private static UDB instance;
    private JDA jda;
    private final HashMap<String, GuildConfig> config = new HashMap<>();

    public static void main(String[] args) {
        if (args.length == 1)
            instance = new UDB(args[0], new Class[]{ModWelcome.class, ModGoodbye.class, ModAutoRoles.class, ModReactionRoles.class});
        else
            System.out.println("Syntax: java -jar UDB.jar <Bot Token>");
    }

    public static UDB getInstance() {
        return instance;
    }

    public UDB(String token, Class<? extends Module>[] moduleClasses) {
        try {
            for (File f : new File("config").listFiles())
                if (!f.getName().equals("default.json") && f.getName().endsWith(".json"))
                    loadGuildConfig(f.getName().substring(0, f.getName().length() - 5));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Writing guild configs...");
                for (String guildId : config.keySet()) writeGuildConfig(guildId, config.get(guildId));
            }));
            ArrayList<Module> modules = new ArrayList<>();
            for (Class<? extends Module> modClass : moduleClasses)
                try {
                    modules.add(modClass.getConstructor(UDB.class).newInstance(this));
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            this.jda = JDABuilder.createDefault(token).addEventListeners((EventListener) genericEvent -> {
                if (genericEvent instanceof GenericGuildEvent) {
                    GuildConfig gConfig = UDB.this.getGuildConfig(((GenericGuildEvent) genericEvent).getGuild());
                    for (Module mod : modules)
                        if (gConfig.isModuleActive(mod.getId()))
                            try {
                                Method m = mod.getClass().getMethod("event", GenericGuildEvent.class, GuildConfig.class, JsonObject.class);
                                if (m.isAnnotationPresent(SubscribeEvent.class) && Arrays.asList(m.getAnnotation(SubscribeEvent.class).value()).contains(genericEvent.getClass()))
                                    m.invoke(mod, genericEvent, gConfig, gConfig.getModConfig(mod.getId()));
                            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }
                }
            }).setChunkingFilter(ChunkingFilter.ALL).setMemberCachePolicy(MemberCachePolicy.ALL).enableIntents(GatewayIntent.GUILD_MEMBERS).setRawEventsEnabled(true).build();
        } catch (LoginException e) {
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
                file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(config.data.toJSONString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
