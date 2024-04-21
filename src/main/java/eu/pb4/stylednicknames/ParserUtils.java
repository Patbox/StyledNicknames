package eu.pb4.stylednicknames;

import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.tag.TagRegistry;
import eu.pb4.stylednicknames.config.ConfigManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ParserUtils {

    public static NodeParser getParser(@Nullable ServerPlayerEntity player) {
        var b = NodeParser.builder();
        var config = ConfigManager.getConfig();

        if (player != null) {
            var registry = TagRegistry.create();
            for (var entry : TagRegistry.SAFE.getTags()) {
                if ((config.defaultFormattingCodes.getBoolean(entry.name())
                        || Permissions.check(player, "stylednicknames.format." + entry.name(), 2))) {
                    registry.register(entry);
                }
            }
            b.simplifiedTextFormat().quickText()
                    .customTagRegistry(registry);

            if (config.configData.allowLegacyFormatting) {
                var formats = new ArrayList<Formatting>();
                for (Formatting formatting : Formatting.values()) {
                    if (registry.getTag(formatting.getName()) != null) {
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
