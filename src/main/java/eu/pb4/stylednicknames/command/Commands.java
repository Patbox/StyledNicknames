package eu.pb4.stylednicknames.command;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.TextParser;
import eu.pb4.stylednicknames.NicknameHolder;
import eu.pb4.stylednicknames.StyledNicknamesMod;
import eu.pb4.stylednicknames.config.ConfigManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class Commands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
                    literal("styled-nicknames")
                            .requires(Permissions.require("stylednicknames.main", true))
                            .executes(Commands::about)

                            .then(literal("reload")
                                    .requires(Permissions.require("stylednicknames.reload", 3))
                                    .executes(Commands::reloadConfig)
                            )

                            .then(literal("set")
                                    .requires(Permissions.require("stylednicknames.change_others", 3))
                                    .then(argument("player", EntityArgumentType.player())
                                            .then(argument("nickname", StringArgumentType.greedyString())
                                                    .executes(Commands::changeOther)
                                            )
                                    )
                            )
                            .then(literal("clear")
                                    .requires(Permissions.require("stylednicknames.change_others", 3))
                                    .then(argument("player", EntityArgumentType.player())
                                            .executes(Commands::resetOther)
                                    )
                            )
            );

            var node = dispatcher.register(
                    literal("nickname")
                            .requires(Permissions.require("stylednicknames.use", 3).or((s) -> ConfigManager.getConfig().configData.allowByDefault))

                            .then(literal("set")
                                    .then(argument("nickname", StringArgumentType.greedyString())
                                            .executes(Commands::change)
                                    )
                            )
                            .then(literal("clear").executes(Commands::reset))
            );

            dispatcher.register(
                    literal("nick")
                            .requires(Permissions.require("stylednicknames.use", 3).or((s) -> ConfigManager.getConfig().configData.allowByDefault))
                            .redirect(node)
            );
        });
    }

    private static int change(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        NicknameHolder holder = NicknameHolder.of(context.getSource().getPlayer());
        var config = ConfigManager.getConfig();
        var nickname = context.getArgument("nickname", String.class);
        if (config.configData.maxLength > 0) {
            Map<String, TextParser.TextFormatterHandler> handlers = new HashMap<>();
            for (Map.Entry<String, TextParser.TextFormatterHandler> entry : TextParser.getRegisteredTags().entrySet()) {
                if (!entry.getKey().equals("click")
                        && (config.defaultFormattingCodes.getBoolean(entry.getKey())
                        || Permissions.check(context.getSource(), "stylednicknames.format." + entry.getKey(), 2))) {
                    handlers.put(entry.getKey(), entry.getValue());
                }
            }

            if (config.configData.allowLegacyFormatting) {
                for (Formatting formatting : Formatting.values()) {
                    if (handlers.get(formatting.getName()) != null) {
                        nickname = nickname.replace(String.copyValueOf(new char[]{'&', formatting.getCode()}), "<" + formatting.getName() + ">");
                    }
                }
            }

            var output = TextParser.parse(nickname, handlers);

            if (output.getString().length() > config.configData.maxLength && !Permissions.check(context.getSource(), "stylednicknames.ignore_limit", 2)) {
                context.getSource().sendFeedback(ConfigManager.getConfig().tooLongText, false);
                return 1;
            }
        }

        holder.sn_set(nickname, true);
        context.getSource().sendFeedback(
                PlaceholderAPI.parsePredefinedText(ConfigManager.getConfig().changeText, PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN, Map.of("nickname", holder.sn_getOutputOrVanilla())),
                false);
        return 0;
    }

    private static int reset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        NicknameHolder.of(context.getSource().getPlayer()).sn_set(null, false);
        context.getSource().sendFeedback(
                PlaceholderAPI.parsePredefinedText(ConfigManager.getConfig().resetText, PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN, Map.of("nickname", context.getSource().getPlayer().getName())),
                false);
        return 0;
    }

    private static int changeOther(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        EntitySelector selector = context.getArgument("player", EntitySelector.class);
        ServerPlayerEntity player = selector.getPlayer(context.getSource());
        NicknameHolder.of(player).sn_set(context.getArgument("nickname", String.class), false);
        context.getSource().sendFeedback(new TranslatableText("Changed nickname of %s to %s", player.getName(), NicknameHolder.of(player).sn_getOutputOrVanilla()), false);
        return 0;
    }

    private static int resetOther(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        EntitySelector selector = context.getArgument("player", EntitySelector.class);
        ServerPlayerEntity player = selector.getPlayer(context.getSource());
        NicknameHolder.of(player).sn_set(null, false);
        context.getSource().sendFeedback(new TranslatableText("Cleared nickname of %s", player.getName()), false);
        return 0;
    }

    private static int reloadConfig(CommandContext<ServerCommandSource> context) {
        if (ConfigManager.loadConfig()) {
            context.getSource().sendFeedback(new LiteralText("Reloaded config!"), false);
        } else {
            context.getSource().sendError(new LiteralText("Error occurred while reloading config!").formatted(Formatting.RED));

        }
        return 1;
    }

    private static int about(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText("Styled Nicknames")
                .formatted(Formatting.BLUE)
                .append(new LiteralText( " - " + StyledNicknamesMod.VERSION)
                        .formatted(Formatting.WHITE)
                ), false);

        return 1;
    }
}
