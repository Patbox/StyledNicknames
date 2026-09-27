package eu.pb4.stylednicknames.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import eu.pb4.stylednicknames.StyledNicknamesMod;
import eu.pb4.stylednicknames.config.data.ConfigData;
import eu.pb4.stylednicknames.config.data.VersionConfigData;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    public static final int VERSION = 2;

    // Removed .setLenient() – builder is no longer globally lenient
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static Config CONFIG;

    public static Config getConfig() {
        return CONFIG;
    }

    // Helper method: enables lenient mode only for parsing operations
    private static <T> T fromJsonLenient(String json, Class<T> classOfT) throws IOException {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setLenient(true);
            return GSON.fromJson(reader, classOfT);
        }
    }

    public static boolean loadConfig() {
        CONFIG = null;
        try {
            ConfigData config;
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "styled-nicknames.json");


            if (configFile.exists()) {
                String json = IOUtils.toString(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));

                // Use lenient helper here
                VersionConfigData versionConfigData = fromJsonLenient(json, VersionConfigData.class);

                config = ConfigData.transform(switch (versionConfigData.CONFIG_VERSION_DONT_TOUCH_THIS) {
                    default -> fromJsonLenient(json, ConfigData.class); // Use lenient helper here too
                });
            } else {
                config = new ConfigData();
            }

            // Writing doesn't need leniency – keep as is
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8))) {
                writer.write(GSON.toJson(config));
            }

            CONFIG = new Config(config);
            return true;
        }
        catch(IOException exception) {
            StyledNicknamesMod.LOGGER.error("Something went wrong while reading config!");
            exception.printStackTrace();
            CONFIG = new Config(new ConfigData());
            return false;
        }
    }

    public static boolean isEnabled() {
        return CONFIG != null;
    }
}
