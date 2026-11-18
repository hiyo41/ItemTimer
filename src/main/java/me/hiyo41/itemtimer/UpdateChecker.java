package me.hiyo41.itemtimer;

import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static final Pattern TAG_PATTERN = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern URL_PATTERN = Pattern.compile("\"html_url\"\\s*:\\s*\"([^\"]+)\"");
    private static final int TIMEOUT_MS = 5000;

    private final ItemTimer plugin;
    private final String githubUrl = "https://api.github.com/repos/hiyo41/ItemTimer/releases/latest";

    public UpdateChecker(ItemTimer plugin) {
        this.plugin = plugin;
    }

    public void checkUpdate(final Consumer<UpdateResult> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            UpdateResult githubResult = checkGitHub();
            if (githubResult != null && isNewer(githubResult.version)) {
                consumer.accept(githubResult);
            } else {
                consumer.accept(null);
            }
        });
    }

    private UpdateResult checkGitHub() {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) URI.create(githubUrl).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("User-Agent", "ItemTimer-Plugin");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String version = extract(TAG_PATTERN, response.toString());
            String url = extract(URL_PATTERN, response.toString());
            return version != null && url != null ? new UpdateResult(version, url, "GitHub") : null;
        } catch (Exception ignored) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String extract(Pattern pattern, String json) {
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isNewer(String remoteVersion) {
        try {
            String current = plugin.getPluginMeta().getVersion().replace("-SNAPSHOT", "").replace("v", "");
            String remote = remoteVersion.replace("v", "").replace("-SNAPSHOT", "");

            String[] currentParts = current.split("\\.");
            String[] remoteParts = remote.split("\\.");

            int length = Math.max(currentParts.length, remoteParts.length);
            for (int i = 0; i < length; i++) {
                String cPart = i < currentParts.length ? currentParts[i].replaceAll("[^0-9]", "") : "";
                String rPart = i < remoteParts.length ? remoteParts[i].replaceAll("[^0-9]", "") : "";

                int c = !cPart.isEmpty() ? Integer.parseInt(cPart) : 0;
                int r = !rPart.isEmpty() ? Integer.parseInt(rPart) : 0;

                if (r > c) return true;
                if (c > r) return false;
            }
        } catch (NumberFormatException ignored) {
            return false;
        }
        return false;
    }

    public static class UpdateResult {
        public final String version;
        public final String url;
        public final String source;

        public UpdateResult(String version, String url, String source) {
            this.version = version;
            this.url = url;
            this.source = source;
        }
    }
}
