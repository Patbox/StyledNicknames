package eu.pb4.stylednicknames.config.data;

import eu.pb4.placeholders.api.parsers.TextParserV1;
import eu.pb4.placeholders.api.parsers.tag.TagRegistry;
import eu.pb4.stylednicknames.config.ConfigManager;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;

public class ConfigData {
    public int CONFIG_VERSION_DONT_TOUCH_THIS = ConfigManager.VERSION;
    public String _comment = "Before changing anything, see https://github.com/Patbox/StyledNicknames#configuration";
    public boolean allowByDefault = true;
    public String nicknameFormat = "#${nickname}";
    public String nicknameFormatColor = "${nickname}";
    public int maxLength = 48;
    public boolean changeDisplayName = true;
    public boolean changePlayerListName = false;
    public boolean allowLegacyFormatting = false;
    public String nicknameChangedMessage = "Your nickname has been changed to ${nickname}";
    public String nicknameResetMessage = "Your nickname has been removed!";
    public HashMap<String, Boolean> defaultEnabledFormatting = getDefaultFormatting();
    public String tooLongMessage = "This nickname is too long!";

    private static HashMap<String, Boolean> getDefaultFormatting() {
        HashMap<String, Boolean> map = new HashMap<>();
        for (var tag : TagRegistry.SAFE.getTags()) {
            var color = Formatting.byName(tag.name());
            map.put(tag.name(), color != null && color != Formatting.BLACK);
        }
        return map;
    }

    public static ConfigData transform(ConfigData configData) {
        var def = getDefaultFormatting();
        for (var entry : def.entrySet()) {
            configData.defaultEnabledFormatting.putIfAbsent(entry.getKey(), entry.getValue());
        }

        for (var e : new ArrayList<>(configData.defaultEnabledFormatting.keySet())) {
            if (!def.containsKey(e)) {
                configData.defaultEnabledFormatting.remove(e);
            }
        }
        return configData;
    }
}
