package fr.bakaaless.botzoe.bot.commands.misc;

import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import fr.bakaaless.botzoe.starter.Starter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class ExecuteCommand implements CommandExecutor {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        final Optional<Message> code = Optional.ofNullable(event.getMessage().getReferencedMessage());
        final StringBuilder args = new StringBuilder();
        for (final String part : arguments) {
            args.append(" ").append(part);
        }
        code.ifPresent(message -> {
            if (message.getContentRaw().toLowerCase().startsWith("```python")) {
                final File executeCode = new File("tempo", "tempopython_" + message.getAuthor().getId() + "_" + System.currentTimeMillis() + ".py");
                try {
                    executeCode.getParentFile().mkdirs();
                    executeCode.createNewFile();
                    FileUtils.writeByteArrayToFile(executeCode, message.getContentStripped().getBytes(StandardCharsets.UTF_8));
                    final Process pythonProcess = Runtime.getRuntime().exec("python3 " + executeCode.getAbsolutePath() + args);
                    final InputStream stdout = pythonProcess.getInputStream();
                    String line;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdout,StandardCharsets.UTF_8))) {
                        try {
                            while ((line = reader.readLine()) != null) {
                                event.getChannel().sendMessage(line).queue();
                                Starter.getLogger().log(Level.INFO, line);
                            }
                        } catch(IOException e){
                            event.getChannel().sendMessage("Exception in reading output"+ e).queue();
                            e.printStackTrace();
                        }
                    } catch(IOException e){
                        event.getChannel().sendMessage("Exception in reading output"+ e).queue();
                        e.printStackTrace();
                    }
                    new Thread(() -> {
                        final File parent = executeCode.getParentFile();
                        boolean delete = !pythonProcess.isAlive() && executeCode.delete();
                        while (!delete) {
                            delete = !pythonProcess.isAlive() && executeCode.delete();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        delete = !pythonProcess.isAlive() && parent.delete();
                        while (!delete) {
                            delete = !pythonProcess.isAlive() && parent.delete();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (IOException exception) {
                    event.getChannel().sendMessage(exception.toString()).queue();
                    exception.printStackTrace();
                }
            }
        });
    }

}
