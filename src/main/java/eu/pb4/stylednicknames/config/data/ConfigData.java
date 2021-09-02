package eu.pb4.stylednicknames.config.data;

import eu.pb4.placeholders.TextParser;
import eu.pb4.stylednicknames.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

public class ConfigData {
    public int CONFIG_VERSION_DONT_TOUCH_THIS = ConfigManager.VERSION;
    public String _comment = "Before changing anything, see https://github.com/Patbox/StyledNicknames#configuration";
    public boolean allowByDefault = false;
    public String defaultPrefix = "#";
    public int maxLength = 32;
    public boolean changeDisplayName = true;
    public boolean changePlayerListName = false;
    public boolean allowLegacyFormatting = false;
    public String nicknameChangedMessage = "Your nickname has been changed to ${nickname}";
    public String nicknameResetMessage = "Your nickname has been removed!";
    public HashMap<String, Boolean> defaultEnabledFormatting = getDefaultFormatting();
    public String tooLongMessage = "This nickname is too long!";

    private static HashMap<String, Boolean> getDefaultFormatting() {
        HashMap<String, Boolean> map = new HashMap<>();
        for (String string : TextParser.getRegisteredSafeTags().keySet()) {
            map.put(string, false);
        }
        return map;
    }

    public static ConfigData transform(ConfigData configData) {
        for (Map.Entry<String, Boolean> entry : getDefaultFormatting().entrySet()) {
            configData.defaultEnabledFormatting.putIfAbsent(entry.getKey(), entry.getValue());
        }
        return configData;
    }
}
