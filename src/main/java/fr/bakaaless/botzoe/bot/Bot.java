package fr.bakaaless.botzoe.bot;

import fr.bakaaless.botzoe.bot.commands.CommandManager;
import fr.bakaaless.botzoe.bot.commands.elections.ElectionsCommand;
import fr.bakaaless.botzoe.bot.commands.misc.ActivityCommand;
import fr.bakaaless.botzoe.bot.commands.misc.EightBallCommand;
import fr.bakaaless.botzoe.bot.commands.misc.ExecuteCommand;
import fr.bakaaless.botzoe.bot.commands.misc.RandomUserCommand;
import fr.bakaaless.botzoe.bot.commands.music.*;
import fr.bakaaless.botzoe.bot.events.Elections;
import fr.bakaaless.botzoe.bot.listeners.BotListener;
import fr.bakaaless.botzoe.bot.music.MusicChannel;
import fr.bakaaless.botzoe.bot.music.MusicModule;
import fr.bakaaless.botzoe.starter.Config;
import fr.bakaaless.botzoe.starter.Starter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.log4j.Level;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Bot {

    private static Bot instance;

    public static Bot get() {
        return instance;
    }

    public static Bot generate(final String token) throws LoginException, InterruptedException {
        if (instance == null)
            instance = new Bot(token);
        return instance;
    }

    private final JDA jda;
    private MusicModule musicModule;

    private Bot(final String token) throws LoginException, InterruptedException {
        this.jda = JDABuilder
                .createDefault(
                        token,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .enableCache(
                        CacheFlag.VOICE_STATE
                )
                .disableCache(
                        CacheFlag.EMOTE
                )
                .setStatus(OnlineStatus.ONLINE)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(new BotListener())
                .build();
        this.jda.awaitReady();
    }

    public void setupElections() {
        final File file = new File(Elections.getPATH());
        if (file.exists()) {
            try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                final StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                final Elections elections = Starter.getGson().fromJson(stringBuilder.toString(), Elections.class);
                Elections.setCurrent(elections);
                elections.update();
                this.jda.addEventListener(elections);
            } catch (IOException e) {
                Starter.getLogger().log(Level.ERROR, "Can't load elections' file", e);
            }
        }
        CommandManager.register("elections", new ElectionsCommand());
    }

    public void setupMusic() {
        this.musicModule = new MusicModule(new MusicChannel(Config.get().getMusicChannelId()));
        CommandManager.register("clear", new ClearCommand());
        CommandManager.register("join", new JoinCommand());
        CommandManager.register("leave", new LeaveCommand());
        CommandManager.register("play", new PlayCommand());
        CommandManager.register("queue", new QueueCommand());
        CommandManager.register("search", new SearchCommand());
        CommandManager.register("skip", new SkipCommand());
    }

    public void setupMisc() {
        CommandManager.register("randomuser", new RandomUserCommand());
        CommandManager.register("8ball", new EightBallCommand());
        CommandManager.register("execute", new ExecuteCommand());
        for (final ActivityCommand.Activities activity : ActivityCommand.Activities.values())
            CommandManager.register(activity.getCommand() + "together", new ActivityCommand(activity));
    }

    public JDA getJda() {
        return this.jda;
    }

    public void shutdown() {
        try {
            MusicModule.get().getChannel().resetMessageId();
            this.jda.getPresence().setStatus(OnlineStatus.OFFLINE);
            this.jda.shutdown();
        } catch (Exception e) {
            Starter.getLogger().log(Level.FATAL, "Can't shutdown properly", e);
        }
    }
}
