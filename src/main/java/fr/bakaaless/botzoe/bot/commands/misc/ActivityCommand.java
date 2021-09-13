package fr.bakaaless.botzoe.bot.commands.misc;

import com.google.gson.JsonParser;
import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import fr.bakaaless.botzoe.starter.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import okhttp3.*;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActivityCommand implements CommandExecutor {

    private final Activities activity;

    public ActivityCommand(final Activities activity) {
        this.activity = activity;
    }

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        if (event.getMember() == null)
            return;
        if (event.getMember().getVoiceState() == null || !event.getMember().getVoiceState().inVoiceChannel()) {
            event.getChannel().sendMessage(":warning: Vous devez être dans un salon vocal.").queue(deleteMessage -> {
                try {
                    deleteMessage.delete().queueAfter(5, TimeUnit.SECONDS);
                } catch (Exception ignored) {}
            });
            return;
        }
        try {
            final OkHttpClient client = new OkHttpClient();
            final String json = "{\"max_age\":3600,"                                    +
                        "\"max_uses\":0,"                                               +
                        "\"target_application_id\" :\"" + this.activity.id + "\","      +
                        "\"target_type\":2,"                                            +
                        "\"temporary\":false,"                                          +
                        "\"validate\":null}";
            final Request request = new Request.Builder().url("https://discord.com/api/v8/channels/269871916041502721" + event.getMember().getVoiceState().getChannel().getId() + "/invites")
                    .addHeader("Authorization", "Bot " + Config.get().getToken())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(MediaType.get("application/json"), json))
                    .build();
            final Response response = client.newCall(request).execute();
            if (response.body() == null) {
                return;
            }
            final String code = JsonParser.parseString(response.body().string()).getAsJsonObject().get("code").getAsString();
            response.close();
            final EmbedBuilder embed = new EmbedBuilder();
            embed.setFooter("Exécuté par " + (event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getName())).setTimestamp(Instant.now());
            embed.setDescription("[(Cliquez ici pour accéder " + this.activity.name + " via le channel " + event.getMember().getVoiceState().getChannel().getName() + ")](https://discord.gg/" + code + ")");
            event.getChannel().sendMessage(embed.build()).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum Activities {

        BETRAYAL("à Betrayal.io", "betrayal", 773336526917861400L),
        FISHINGTON("à Fishington.io", "fishington", 814288819477020702L),
        POKER("au Poker", "poker", 755827207812677713L),
        YOUTUBE("à YouTube", "youtube", 755827207812677713L);

        private final String name;
        private final String command;
        final long id;

        Activities(final String name, final String command, final long id) {
            this.name = name;
            this.command = command;
            this.id = id;
        }

        public String getCommand() {
            return this.command;
        }

    }
}
