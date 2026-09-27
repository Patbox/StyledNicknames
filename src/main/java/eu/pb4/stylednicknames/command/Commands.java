package eu.pb4.stylednicknames.command;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.stylednicknames.FabricPermissionBridge;
import eu.pb4.stylednicknames.NicknameHolder;
import eu.pb4.stylednicknames.ParserUtils;
import eu.pb4.stylednicknames.StyledNicknamesMod;
import eu.pb4.stylednicknames.config.Config;
import eu.pb4.stylednicknames.config.ConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.*;
import java.util.stream.Collectors;

import static eu.pb4.stylednicknames.StyledNicknamesMod.id;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class Commands {
    public static final boolean VANISH = FabricLoader.getInstance().isModLoaded("melius-vanish");

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("styled-nicknames")
                            .requires(FabricPermissionBridge.require(id("main"), true))
                            .executes(Commands::about)

                            .then(literal("reload")
                                    .requires(FabricPermissionBridge.require(id("reload"), PermissionLevel.ADMINS))
                                    .executes(Commands::reloadConfig)
                            )

                            .then(literal("set")
                                    .requires(FabricPermissionBridge.require(id("change_others"), PermissionLevel.ADMINS))
                                    .then(argument("player", EntityArgument.player())
                                            .then(argument("nickname", StringArgumentType.greedyString()).suggests(OTHER_PREVIOUS_NICKNAME_PROVIDER)
                                                    .executes(Commands::changeOther)
                                            )
                                    )
                            )
                            .then(literal("clear")
                                    .requires(FabricPermissionBridge.require(id("change_others"), PermissionLevel.ADMINS))
                                    .then(argument("player", EntityArgument.player())
                                            .executes(Commands::resetOther)
                                    )
                            )
            );

            var node = dispatcher.register(
                    literal("nickname")
                            .requires(FabricPermissionBridge.require(id("use"), PermissionLevel.ADMINS).or((s) -> ConfigManager.getConfig().configData.allowByDefault))

                            .then(literal("set")
                                    .then(argument("nickname", StringArgumentType.greedyString()).suggests(PREVIOUS_NICKNAME_PROVIDER)
                                            .executes(Commands::change)
                                    )
                            )
                            .then(literal("clear").executes(Commands::reset))
            );

            dispatcher.register(
                    literal("nick")
                            .requires(FabricPermissionBridge.require(id("use"), PermissionLevel.ADMINS).or((s) -> ConfigManager.getConfig().configData.allowByDefault))
                            .redirect(node)
            );

            dispatcher.register(
                    literal("realname")
                            .requires(FabricPermissionBridge.require(id("realname"), PermissionLevel.ADMINS).or((s) -> ConfigManager.getConfig().configData.allowByDefault))
                            .then(argument("nickname", StringArgumentType.greedyString()).suggests(NICKNAME_PROVIDER)
                                    .executes(Commands::realname)
                            )
            );
        });
    }

    private static int change(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NicknameHolder holder = NicknameHolder.of(context.getSource().getPlayerOrException());
        var config = ConfigManager.getConfig();
        var nickname = context.getArgument("nickname", String.class);
        if (config.configData.maxLength > 0) {
            var parser = ParserUtils.getParser(context.getSource().getPlayerOrException());
            var output = parser.parseComponent(nickname, ParserContext.of());

            if (output.getString().length() > config.configData.maxLength && !FabricPermissionBridge.checkPermission(context.getSource(), id("ignore_limit"), PermissionLevel.GAMEMASTERS)) {
                context.getSource().sendSuccess(() -> ConfigManager.getConfig().tooLongText, false);
                return 1;
            }
        }

        if(nickname.contains(" ") && !config.configData.allowSpacesInNicknames){
            context.getSource().sendSuccess(() -> config.nicknameCantContainSpacesText, false);
            return 1;
        }


        holder.styledNicknames$set(nickname, true);
        context.getSource().sendSuccess(() ->
                        ConfigManager.getConfig().changeText.toComponent(ParserContext.of(Config.KEY, holder.styledNicknames$placeholdersCommand())),
                false);
        return 0;
    }

    private static int reset(CommandContext<CommandSourceStack> context)
        throws CommandSyntaxException {
        var holder = NicknameHolder.of(
            context.getSource().getPlayerOrException()
        );
        holder.styledNicknames$set(null, false);
        context
            .getSource()
            .sendSuccess(
                () ->
                    ConfigManager.getConfig().resetText.toComponent(
                        ParserContext.of(
                            Config.KEY,
                            holder.styledNicknames$placeholdersCommand()
                        )
                    ),
                false
            );
        return 0;
    }

    private static int changeOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        NicknameHolder.of(player).styledNicknames$set(context.getArgument("nickname", String.class), false);
        context.getSource().sendSuccess(() -> Component.translatable("Changed nickname of %s to %s", player.getName(), NicknameHolder.of(player).styledNicknames$getOutputOrVanilla()), false);
        return 0;
    }

    private static int resetOther(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        NicknameHolder.of(player).styledNicknames$set(null, false);
        context.getSource().sendSuccess(() -> Component.translatable("Cleared nickname of %s", player.getName()), false);
        return 0;
    }

    private static int realname(CommandContext<CommandSourceStack> context) {
        String nickname = StringArgumentType.getString(context, "nickname");
        List<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();
        Map<ServerPlayer, MutableComponent> foundPlayers = new HashMap<>();
        for (ServerPlayer player : players) {
            MutableComponent output = NicknameHolder.of(player).styledNicknames$getOutput();
            if (output == null) continue;
            if (output.getString().equals(nickname) && canSeePlayer(player, context.getSource())) {
                foundPlayers.put(player, output);
            }
        }
        if (foundPlayers.isEmpty()) {
            context.getSource().sendFailure(Component.literal("No player with that nickname is currently online."));
        } else {
            if (foundPlayers.size() > 1) {
                context.getSource().sendSuccess(() -> Component.translatable("Found %s players with that nickname:", foundPlayers.size()), false);
            }
            foundPlayers.forEach((serverPlayerEntity, mutableText) -> {
                        context.getSource().sendSuccess(() -> Component.translatable("The real name of %s is %s.", serverPlayerEntity.getDisplayName(), serverPlayerEntity.getScoreboardName()), false);
                    }
            );
        }
        return 0;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        if (ConfigManager.loadConfig()) {
            context.getSource().sendSuccess(() -> Component.literal("Reloaded config!"), false);
        } else {
            context.getSource().sendFailure(Component.literal("Error occurred while reloading config!").withStyle(ChatFormatting.RED));

        }
        return 1;
    }

    private static int about(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Styled Nicknames")
                .withStyle(ChatFormatting.BLUE)
                .append(Component.literal(" - " + StyledNicknamesMod.VERSION)
                        .withStyle(ChatFormatting.WHITE)
                ), false);

        return 1;
    }

    private static final SuggestionProvider<CommandSourceStack> PREVIOUS_NICKNAME_PROVIDER = (source, builder) -> {
        ServerPlayer player = source.getSource().getPlayer();
        return SharedSuggestionProvider.suggest(getNicknameSuggestion(player), builder);
    };

    private static final SuggestionProvider<CommandSourceStack> OTHER_PREVIOUS_NICKNAME_PROVIDER = (source, builder) -> {
        ServerPlayer player = EntityArgument.getPlayer(source, "player");
        return SharedSuggestionProvider.suggest(getNicknameSuggestion(player), builder);
    };

    private static Collection<String> getNicknameSuggestion(ServerPlayer player) {
        if (player != null) {
            String nickname = NicknameHolder.of(player).styledNicknames$get();
            if (nickname != null) {
                return Collections.singletonList(nickname);
            }
        }
        return Collections.emptyList();
    }

    private static boolean canSeePlayer(ServerPlayer player, CommandSourceStack viewing) {
        if (VANISH) {
            //return VanishAPI.canSeePlayer(player.level().getServer(), player.getUUID(), viewing);
        }
        return true;
    }

    private static final SuggestionProvider<CommandSourceStack> NICKNAME_PROVIDER = (context, builder) -> {
        List<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();
        Set<String> nicknames = players.stream()
                .filter(player -> canSeePlayer(player, context.getSource()))
                .map(player -> NicknameHolder.of(player).styledNicknames$getOutput())
                .filter(Objects::nonNull)
                .map(Component::getString)
                .collect(Collectors.toSet());
        return SharedSuggestionProvider.suggest(nicknames, builder);
    };

}
