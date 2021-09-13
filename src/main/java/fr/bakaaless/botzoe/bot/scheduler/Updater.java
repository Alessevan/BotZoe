package fr.bakaaless.botzoe.bot.scheduler;

import fr.bakaaless.botzoe.bot.events.Elections;

import java.util.Timer;
import java.util.TimerTask;

public class Updater {

    private static Updater instance;

    public static Updater get() {
        if (instance == null)
            instance = new Updater();
        return instance;
    }

    private Timer timer;
    private TimerTask task;

    private Updater() {
        this.init();
    }

    public void update() {
        if (Elections.getCurrentElections() != null) {
            final Elections elections = Elections.getCurrentElections();
            elections.update();
            if (elections.getStarted() + elections.getDuration() <= System.currentTimeMillis() / 1000L)
                elections.end();
        }
    }

    private void init() {
        this.timer = new Timer();
        this.task = new TimerTask() {
            @Override
            public void run() {
                update();
            }
        };
    }

    public void stop() {
        this.timer.cancel();
        this.timer.purge();
    }

    public void start() {
        this.timer.schedule(this.task, 0L, 60 * 1000L);
    }
}
