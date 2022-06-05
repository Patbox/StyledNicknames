package eu.pb4.stylednicknames.config;


import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.stylednicknames.config.data.ConfigData;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import net.minecraft.text.Text;


public final class Config {
    public final ConfigData configData;
    public final Object2BooleanArrayMap<String> defaultFormattingCodes;
    public final TextNode nicknameFormat;
    public final TextNode changeText;
    public final TextNode resetText;
    public final Text tooLongText;

    public Config(ConfigData data) {
        this.configData = data;
        this.nicknameFormat = TextParserUtils.formatNodes(data.nicknameFormat);
        this.changeText = TextParserUtils.formatNodes(data.nicknameChangedMessage);
        this.resetText = TextParserUtils.formatNodes(data.nicknameResetMessage);
        this.tooLongText = TextParserUtils.formatText(data.tooLongMessage);
        this.defaultFormattingCodes = new Object2BooleanArrayMap<>(this.configData.defaultEnabledFormatting);
    }

}
