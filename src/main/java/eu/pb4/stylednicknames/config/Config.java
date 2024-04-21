package eu.pb4.stylednicknames.config;


import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.DynamicTextNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagLikeParser;
import eu.pb4.placeholders.api.parsers.TagParser;
import eu.pb4.stylednicknames.config.data.ConfigData;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import net.minecraft.text.Text;

import java.util.function.Function;


public final class Config {
    public static final ParserContext.Key<Function<String, Text>> KEY = DynamicTextNode.key("styled_nicknames");

    public static final NodeParser PARSER = NodeParser.builder()
            .simplifiedTextFormat()
            .quickText()
            .placeholders(TagLikeParser.PLACEHOLDER_USER, KEY)
            .staticPreParsing()
            .build();

    public final ConfigData configData;
    public final Object2BooleanArrayMap<String> defaultFormattingCodes;
    public final TextNode nicknameFormat;
    public final TextNode nicknameFormatColor;
    public final TextNode changeText;
    public final TextNode resetText;
    public final Text tooLongText;

    public Config(ConfigData data) {
        this.configData = data;
        this.nicknameFormat = PARSER.parseNode(data.nicknameFormat);
        this.nicknameFormatColor = PARSER.parseNode(data.nicknameFormatColor);
        this.changeText = PARSER.parseNode(data.nicknameChangedMessage);
        this.resetText = PARSER.parseNode(data.nicknameResetMessage);
        this.tooLongText = PARSER.parseText(data.tooLongMessage, ParserContext.of());
        this.defaultFormattingCodes = new Object2BooleanArrayMap<>(this.configData.defaultEnabledFormatting);
    }

}
