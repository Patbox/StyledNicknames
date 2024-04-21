package eu.pb4.stylednicknames.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.pb4.stylednicknames.StyledNicknamesMod;
import eu.pb4.stylednicknames.config.data.ConfigData;
import eu.pb4.stylednicknames.config.data.VersionConfigData;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class ConfigManager {
    public static final int VERSION = 2;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setLenient().create();

    private static Config CONFIG;

    public static Config getConfig() {
        return CONFIG;
    }

    public static boolean loadConfig() {
        CONFIG = null;
        try {
            ConfigData config;
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "styled-nicknames.json");


            if (configFile.exists()) {
                String json = IOUtils.toString(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
                VersionConfigData versionConfigData = GSON.fromJson(json, VersionConfigData.class);

                config = ConfigData.transform(switch (versionConfigData.CONFIG_VERSION_DONT_TOUCH_THIS) {
                    default -> GSON.fromJson(json, ConfigData.class);
                });
            } else {
                config = new ConfigData();
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), "UTF-8"));
            writer.write(GSON.toJson(config));
            writer.close();


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
