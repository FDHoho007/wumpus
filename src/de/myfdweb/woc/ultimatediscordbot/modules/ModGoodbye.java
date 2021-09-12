package de.myfdweb.woc.ultimatediscordbot.modules;

import de.myfdweb.woc.ultimatediscordbot.GuildConfig;
import de.myfdweb.woc.ultimatediscordbot.Language;
import de.myfdweb.woc.ultimatediscordbot.Config;
import de.myfdweb.woc.ultimatediscordbot.module.Module;
import de.myfdweb.woc.ultimatediscordbot.module.ModuleManifest;
import de.myfdweb.woc.ultimatediscordbot.UDB;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.jetbrains.annotations.NotNull;

public class ModWelcome extends Module {

    public ModWelcome(UDB botInstance) {
        super(botInstance);
    }

    @Override
    public ModuleManifest getManifest() {
        return new ModuleManifest(1, Language.de_DE.getString("module.welcome.name"), Language.de_DE.getString("module.welcome.description"), "FDHoho007", "1.0.0");
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        GuildConfig gconfig = getBotInstance().getGuildConfig(event.getGuild());
        Config config = gconfig.getModConfig(getId());
        if(gconfig.isModuleActive(getId()) && config.getBoolean("welcome.active"))
            event.getGuild().getTextChannelById(config.getLong("welcome.channel")).sendMessage(config.getMessage("welcome.message")).queue();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        GuildConfig gconfig = getBotInstance().getGuildConfig(event.getGuild());
        Config config = gconfig.getModConfig(getId());
        if(gconfig.isModuleActive(getId()) && config.getBoolean("welcome.active"))
            event.getGuild().getTextChannelById(config.getLong("welcome.channel")).sendMessage(config.getMessage("welcome.message")).queue();
    }
}
