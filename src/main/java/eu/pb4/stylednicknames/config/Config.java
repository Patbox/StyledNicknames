package eu.pb4.stylednicknames.config;


import eu.pb4.placeholders.TextParser;
import eu.pb4.stylednicknames.config.data.ConfigData;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import net.minecraft.text.Text;


public final class Config {
    public final ConfigData configData;
    public final Object2BooleanArrayMap<String> defaultFormattingCodes;
    public final Text defaultPrefix;
    public final Text changeText;
    public final Text resetText;
    public final Text tooLongText;

    public Config(ConfigData data) {
        this.configData = data;
        this.defaultPrefix = TextParser.parse(data.defaultPrefix);
        this.changeText = TextParser.parse(data.nicknameChangedMessage);
        this.resetText = TextParser.parse(data.nicknameResetMessage);
        this.tooLongText = TextParser.parse(data.tooLongMessage);
        this.defaultFormattingCodes = new Object2BooleanArrayMap<>(this.configData.defaultEnabledFormatting);
    }

}
