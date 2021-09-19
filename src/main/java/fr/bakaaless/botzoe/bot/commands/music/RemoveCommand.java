package fr.bakaaless.botzoe.bot.commands.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class RemoveCommand extends MusicCommand {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        if (!super.canSendHere(event) || event.getMember() == null)
            return;
        if (!super.isInChannel(event.getChannel(), event.getMember(), false))
            return;
        event.getMessage().delete().queue();
        final MessageEmbed embed = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setFooter(event.getMember().getEffectiveName(), event.getMember().getUser().getAvatarUrl())
                .setColor(Color.BLACK)
                .setTitle("Vider la file d'attente des musiques")
                .build();
        String number = "all";
        getNumber:
        if (arguments.size() > 0) {
            if (arguments.size() == 1) {
                if (arguments.get(0).contains("-")) {
                    final String[] args = arguments.get(0).split("-");
                    if (args.length == 2) {
                        try {
                            final int start = Integer.parseInt(args[0]);
                            final int end = Integer.parseInt(args[1]);
                            number = start + "-" + end;
                            if (start > end)
                                number = end + "-" + start;
                            break getNumber;
                        } catch (Exception ignored) {
                        }
                    }
                }
                try {
                    final int size = Integer.parseInt(arguments.get(0));
                    number = arguments.get(0);
                    break getNumber;
                } catch (Exception ignored) {
                }
            }
            try {
                final int start = Integer.parseInt(arguments.get(0));
                final int end = Integer.parseInt(arguments.get(1));
                number = start + "-" + end;
                if (start > end)
                    number = end + "-" + start;
                break getNumber;
            } catch (Exception ignored) {
            }
        }
        final Button button = Button.danger("removeMusic:" + event.getMember().getId() + ":" + number, "Confirmation");
        event.getMessage().replyEmbeds(embed).setActionRow(button).queue();
    }
}
