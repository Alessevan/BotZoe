package fr.bakaaless.botzoe.starter;

public class Config {

    private static Config instance;

    public static Config get() {
        if (instance == null)
            instance = new Config();
        return instance;
    }

    static void setConfig(final Config config) {
        instance = config;
    }

    static Config createBlank() {
        return new Config();
    }

    private String version = "1.0.2";
    private String token = "";
    private String prefix = "&";
    private Long musicChannel = 0L;
    private Long electionsWinnerId = 0L;
    private Long electionsDuration = 10 * 60 * 60L;
    private Long[] electionsRanks = new Long[0];

    private Config() {
    }

    public String getVersion() {
        return this.version;
    }

    public String getToken() {
        return this.token;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public Long getMusicChannelId() {
        return this.musicChannel;
    }

    public Long getElectionsWinnerId() {
        return this.electionsWinnerId;
    }

    public Long getElectionsDuration() {
        return this.electionsDuration;
    }

    public Long[] getElectionsRanks() {
        return this.electionsRanks;
    }
}
