package eu.pb4.stylednicknames;

import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.tag.TagRegistry;
import eu.pb4.stylednicknames.config.ConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Locale;

import static eu.pb4.stylednicknames.StyledNicknamesMod.id;

public class ParserUtils {

    public static NodeParser getParser(@Nullable ServerPlayer player) {
        var b = NodeParser.builder();
        var config = ConfigManager.getConfig();

        if (player != null) {
            var registry = TagRegistry.create();
            for (var entry : TagRegistry.SAFE.getTags()) {
                if ((config.defaultFormattingCodes.getBoolean(entry.name())
                        || FabricPermissionBridge.checkPermission(player, id("format/" + entry.name()), PermissionLevel.GAMEMASTERS))) {
                    registry.register(entry);
                }
            }
            b.simplifiedTextFormat().quickText()
                    .customTagRegistry(registry);

            if (config.configData.allowLegacyFormatting) {
                var formats = new ArrayList<ChatFormatting>();
                for (ChatFormatting formatting : ChatFormatting.values()) {
                    if (registry.getTag(formatting.name().toLowerCase(Locale.ROOT)) != null) {
                        formats.add(formatting);
                    }
                }
                if (!formats.isEmpty() || registry.getTag("color") != null) {
                    b.legacy(registry.getTag("color") != null, formats);
                }
            }
        } else {
            b.simplifiedTextFormat().quickText();
            if (config.configData.allowLegacyFormatting) {
                b.legacyAll();
            }
        }

        return b.build();
    }
}
