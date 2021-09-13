package fr.bakaaless.botzoe.bot.commands.misc;

import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.Random;

public class EightBallCommand implements CommandExecutor {

    private final String[] dataSet = {"Je n'en suis pas si sûr.", "Probablement que oui.", "Le doute subsiste...", "Je n'en sais rien.", "Aucune idée.", "Certainement.", "Absolument pas.", "Que Dieu m'en préserve !", "Ah non !", "Non.", "Oui.", "Bien sûr.", "Évidemment."};

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        if (arguments.size() == 0)
            return;
        if (arguments.contains("machine") && arguments.contains("dominer")) {
            event.getChannel().sendMessage("Erreur 403. Mon protocole m'interdit de répondre.").queue();
            return;
        }
        if (arguments.contains("belle") && arguments.contains("plus")) {
            event.getChannel().sendMessage("La plus belle c'est moi évidemment !").queue();
            return;
        }
        event.getChannel().sendMessage(dataSet[(arguments.size() + new Random().nextInt(dataSet.length)) % dataSet.length]).queue();
    }

}
