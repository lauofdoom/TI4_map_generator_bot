package ti4.local;

/**
 * LOCAL CUSTOMIZATION: Configuration values specific to the lausync self-hosted deployment.
 * All entries in this class are local-only and have no equivalent in upstream.
 *
 * <p>Environment variables are read at runtime so that the Docker image does not need to be
 * rebuilt when deployment settings change — only a container restart is required.
 */
public class LocalConfig {

    private static final String ENV_VAR_WEB_BASE_URL = "WEB_BASE_URL";
    private static final String DEFAULT_WEB_BASE_URL = "https://lausyncti4.thefords.cloud/game/";

    /** Returns the base URL for the web frontend game view, e.g. {@code https://host/game/}. */
    public static String getWebBaseUrl() {
        String url = System.getenv(ENV_VAR_WEB_BASE_URL);
        return url != null ? url : DEFAULT_WEB_BASE_URL;
    }
}
