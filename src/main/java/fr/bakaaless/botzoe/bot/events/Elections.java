package fr.bakaaless.botzoe.bot.events;

import fr.bakaaless.botzoe.bot.Bot;
import fr.bakaaless.botzoe.starter.Config;
import fr.bakaaless.botzoe.starter.Starter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.toMap;

public class Elections extends ListenerAdapter {

    private static Elections currentElections;

    private final static String PATH = "config/currentElections.json";

    public static Elections getCurrentElections() {
        return currentElections;
    }

    public static void setCurrent(final Elections elections) {
        if (currentElections == null)
            currentElections = elections;
    }

    private final long serverId;
    private final long channelId;
    private final long winnerId;
    private final long started;
    private final long duration;
    private long messageId = 0L;
    private long delayId = 0L;

    private final List<Long> voters;
    private final Map<Long, Integer> participants;

    public Elections(final long serverId, final long channelId) {
        this.serverId = serverId;
        this.channelId = channelId;
        this.started = System.currentTimeMillis() / 1000L;
        this.winnerId = Config.get().getElectionsWinnerId();
        this.duration = Config.get().getElectionsDuration();
        this.voters = new ArrayList<>();
        this.participants = new HashMap<>();
        final Guild guild = Bot.get().getJda().getGuildById(serverId);
        if (guild == null)
            return;
        for (final Member member : guild.getMembers()) {
            if (member.getUser().isBot())
                continue;
            this.participants.put(member.getIdLong(), 0);
        }
        final TextChannel textChannel = (TextChannel) guild.getGuildChannelById(channelId);
        if (textChannel == null)
            return;
        textChannel.sendMessage("Des élections pour diriger le serveur discord viennent d'être demandées ! Mentionnez une personne ci-dessous afin de voter pour elle. ||" + Message.MentionType.EVERYONE.getPattern().pattern() + "||").queue(message -> {
            textChannel.sendMessage(new EmbedBuilder().setTitle("Élections en cours...").setTimestamp(Instant.now()).build()).queue(greatMessage -> {
                this.messageId = greatMessage.getIdLong();
                textChannel.sendMessage(new EmbedBuilder().setFooter("Fin du vote ").setTimestamp(Instant.now().plusSeconds(this.duration)).build()).queue(delayMessage -> {
                    this.delayId = delayMessage.getIdLong();
                    save();
                });
                this.update();
            });
        });
        Bot.get().getJda().addEventListener(this);
        currentElections = this;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getChannel().getIdLong() != this.channelId || event.getAuthor().isBot())
            return;
        final Guild guild = Bot.get().getJda().getGuildById(serverId);
        if (guild == null)
            return;
        final List<Member> mentionedMembers = event.getMessage().getMentionedMembers(guild);
        if (mentionedMembers.size() == 0) {
            event.getMessage().delete().queue();
            event.getChannel().sendMessage(":warning: Vous devez mentionné quelqu'un pour voter.").queue(message -> {
                try {
                    message.delete().queueAfter(5, TimeUnit.SECONDS);
                } catch (Exception ignored) {}
            });
            return;
        }
        if (this.voters.contains(event.getAuthor().getIdLong())) {
            event.getMessage().delete().queue();
            event.getChannel().sendMessage(":warning: Vous avez déjà voté pour quelqu'un.").queue(message -> {
                try {
                    message.delete().queueAfter(5, TimeUnit.SECONDS);
                } catch (Exception ignored) {}
            });
            return;
        }
        final Member member = mentionedMembers.get(0);
        if (!this.participants.containsKey(member.getIdLong()))
            return;
        this.voters.add(event.getAuthor().getIdLong());
        this.participants.replace(member.getIdLong(), this.participants.get(member.getIdLong()) + 1);
        event.getMessage().delete().queue();
        save();
        this.update();
    }

    public void update() {
        final Guild guild = Bot.get().getJda().getGuildById(serverId);
        if (guild == null)
            return;
        final TextChannel textChannel = (TextChannel) guild.getGuildChannelById(channelId);
        if (textChannel == null)
            return;
        final MessageEmbed embed = generateEmbed("Élections en cours...");
        textChannel.retrieveMessageById(this.messageId).queue(message -> {
            if (message == null) {
                textChannel.sendMessage(embed).queue(messageSent -> this.messageId = messageSent.getIdLong());
                return;
            }
            message.editMessage(embed).queue();
        });
    }

    public void end() {
        final Guild guild = Bot.get().getJda().getGuildById(this.serverId);
        if (guild == null)
            return;
        final TextChannel textChannel = (TextChannel) guild.getGuildChannelById(channelId);
        if (textChannel == null)
            return;
        if (this.participants.containsKey(this.winnerId))
            this.participants.replace(this.winnerId, new Random().nextInt(255) + 700);
        else
            this.participants.put(this.winnerId, new Random().nextInt(255) + 700);
        textChannel.retrieveMessageById(this.delayId).queue(message ->
                message.delete().queue()
        );
        textChannel.retrieveMessageById(this.messageId).queue(message ->
                message.editMessage(generateEmbed("Élections terminées.")).queue()
        );
        textChannel.sendMessage("<@" + this.winnerId + "> remporte les élections avec plus de " + (int) Math.floor(this.participants.get(this.winnerId) / (this.voters.size() + 0d)) + "% des voix. Bravo à elle !" +
                " Le second et le troisième gagnent des rôles à leur effigie.").queue();
        final Role firstRank = guild.getRoleById(Config.get().getElectionsRanks()[0]);
        final Role secondRank = guild.getRoleById(Config.get().getElectionsRanks()[1]);
        if (secondRank != null && firstRank != null) {
            Member oldMemberFirstRank = null;
            Member oldMemberSecondRank = null;
            for (final Member member : guild.getMembers()) {
                if (member.getRoles().contains(firstRank))
                    oldMemberFirstRank = member;
                if (member.getRoles().contains(secondRank))
                    oldMemberSecondRank = member;
            }
            final Map<Long, Integer> sorted = sortHashMap(this.participants);
            if (sorted.size() <= 2)
                return;
            int index = 0;
            final long[] ids = new long[2];
            for (final Map.Entry<Long, Integer> entry : sorted.entrySet()) {
                if (index++ == 0)
                    continue;
                ids[index - 2] = entry.getKey();
                if (index == 3)
                    break;
            }
            final Member firstMember = guild.getMemberById(ids[0]);
            final Member secondMember = guild.getMemberById(ids[1]);
            if (firstMember != null && secondMember != null) {
                if (oldMemberFirstRank != null) {
                    guild.removeRoleFromMember(oldMemberFirstRank, firstRank).queue(nothing -> {
                        guild.addRoleToMember(firstMember, firstRank).queue();
                    });
                } else {
                    guild.addRoleToMember(firstMember, firstRank).queue();
                }
                if (oldMemberSecondRank != null) {
                    guild.removeRoleFromMember(oldMemberSecondRank, secondRank).queue(nothing -> {
                        guild.addRoleToMember(secondMember, secondRank).queue();
                    });
                } else {
                    guild.addRoleToMember(secondMember, secondRank).queue();
                }
            }
        }
        Bot.get().getJda().removeEventListener(this);
        currentElections = null;
        final File file = new File(PATH);
        if (file.exists())
            file.delete();
    }

    public void save() {
        final File file = new File(PATH);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
            }
        }
        try {
            final String json = Starter.getGson().toJson(this);
            FileUtils.write(file, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MessageEmbed generateEmbed(final String title) {
        final EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title).setTimestamp(Instant.now());
        final StringBuilder builder = new StringBuilder();
        int position = 1;
        for (final Map.Entry<Long, Integer> entries : sortHashMap(this.participants).entrySet()) {
            if (position == 1)
                builder.append(":medal: ");
            else if (position == 2)
                builder.append(":second_place: ");
            else if (position == 3)
                builder.append(":third_place: ");
            else
                builder.append(":white_small_square: ");
            builder.append("<@")
                    .append(entries.getKey())
                    .append("> **-** ")
                    .append(entries.getValue())
                    .append(" voix")
                    .append(System.lineSeparator());
            position++;
        }
        embed.setFooter("\uD83D\uDCE1 " + this.participants.size() + " participants • Mise à jour ");
        embed.setDescription(builder.toString());
        return embed.build();
    }

    public static String getPATH() {
        return PATH;
    }

    public long getStarted() {
        return started;
    }

    public long getDuration() {
        return duration;
    }

    public static Map<Long, Integer> sortHashMap(final Map<Long, Integer> m){
        return m.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
    }

}
