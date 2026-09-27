package eu.pb4.stylednicknames.mixin;

import static eu.pb4.stylednicknames.StyledNicknamesMod.id;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.stylednicknames.FabricPermissionBridge;
import eu.pb4.stylednicknames.NicknameCache;
import eu.pb4.stylednicknames.NicknameHolder;
import eu.pb4.stylednicknames.ParserUtils;
import eu.pb4.stylednicknames.config.Config;
import eu.pb4.stylednicknames.config.ConfigManager;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.permissions.PermissionLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements NicknameHolder {

    @Shadow
    public ServerPlayer player;

    @Unique
    private String styledNicknames$nickname = null;

    @Unique
    private Component styledNicknames$parsedNicknameRaw = null;

    @Unique
    private boolean colorOnlyMode = false;

    @Unique
    private boolean styledNicknames$requirePermission = true;

    @Override
    public void styledNicknames$loadData() {
        try {
            StringTag nickname = PlayerDataApi.getGlobalDataFor(
                player,
                id("nickname"),
                StringTag.TYPE
            );
            ByteTag permissions = PlayerDataApi.getGlobalDataFor(
                player,
                id("permission"),
                ByteTag.TYPE
            );

            if (nickname != null) {
                this.styledNicknames$set(
                    nickname.value(),
                    permissions != null && permissions.byteValue() > 0
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void styledNicknames$set(
        String nickname,
        boolean requirePermission
    ) {
        Config config = ConfigManager.getConfig();
        CommandSourceStack source = player.createCommandSourceStack();
        if (
            nickname == null ||
            nickname.isEmpty() ||
            (requirePermission &&
                !FabricPermissionBridge.checkPermission(
                    player,
                    id("use"),
                    ConfigManager.getConfig().configData.allowByDefault
                        ? PermissionLevel.ALL
                        : PermissionLevel.GAMEMASTERS
                ))
        ) {
            this.styledNicknames$nickname = null;
            this.styledNicknames$requirePermission = false;
            this.styledNicknames$parsedNicknameRaw = null;
            PlayerDataApi.setGlobalDataFor(this.player, id("nickname"), null);
            PlayerDataApi.setGlobalDataFor(
                this.player,
                id("permission"),
                ByteTag.valueOf(false)
            );
        } else {
            this.styledNicknames$nickname = nickname;
            this.styledNicknames$requirePermission = requirePermission;
            PlayerDataApi.setGlobalDataFor(
                this.player,
                id("nickname"),
                StringTag.valueOf(nickname)
            );
            PlayerDataApi.setGlobalDataFor(
                this.player,
                id("permission"),
                ByteTag.valueOf(requirePermission)
            );

            var text = ParserUtils.getParser(
                requirePermission ? this.player : null
            ).parseComponent(nickname, ParserContext.of());

            this.colorOnlyMode = text
                .getString()
                .toLowerCase(Locale.ROOT)
                .equals(
                    this.player.getGameProfile().name().toLowerCase(Locale.ROOT)
                );
            this.styledNicknames$parsedNicknameRaw = text;
        }

        if (config.configData.changePlayerListName) {
            Objects.requireNonNull(this.player.level().getServer())
                .getPlayerList()
                .broadcastAll(
                    new ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                        this.player
                    )
                );
        }
        ((NicknameCache) this.player).styledNicknames$invalidateCache();
    }

    @Override
    public @Nullable String styledNicknames$get() {
        return this.styledNicknames$nickname;
    }

    @Override
    public @Nullable Component styledNicknames$getParsed() {
        return this.styledNicknames$parsedNicknameRaw;
    }

    @Override
    public @Nullable MutableComponent styledNicknames$getOutput() {
        return this.styledNicknames$parsedNicknameRaw != null
            ? (this.colorOnlyMode
                  ? ConfigManager.getConfig().nicknameFormatColor
                  : ConfigManager.getConfig().nicknameFormat
              )
                  .toComponent(
                      ParserContext.of(
                          Config.KEY,
                          this::styledNicknames$placeholderResolver
                      )
                  )
                  .copy()
            : null;
    }

    @Override
    public MutableComponent styledNicknames$getOutputOrVanilla() {
        return this.styledNicknames$parsedNicknameRaw != null
            ? (this.colorOnlyMode
                  ? ConfigManager.getConfig().nicknameFormatColor
                  : ConfigManager.getConfig().nicknameFormat
              )
                  .toComponent(
                      ParserContext.of(
                          Config.KEY,
                          this::styledNicknames$placeholderResolver
                      )
                  )
                  .copy()
            : this.player.getName().copy();
    }

    @Override
    public boolean styledNicknames$requiresPermission() {
        return this.styledNicknames$requirePermission;
    }

    @Override
    public boolean styledNicknames$shouldDisplay() {
        return (
            this.styledNicknames$parsedNicknameRaw != null &&
            (!this.styledNicknames$requirePermission ||
                FabricPermissionBridge.checkPermission(
                    player,
                    id("use"),
                    ConfigManager.getConfig().configData.allowByDefault
                        ? PermissionLevel.ALL
                        : PermissionLevel.GAMEMASTERS
                ))
        );
    }

    @Override
    public Function<String, Component> styledNicknames$placeholdersCommand() {
        return this::styledNicknames$placeholderResolver;
    }

    @Unique
    private Component styledNicknames$placeholderResolver(String key) {
        if ("playername".equals(key)) {
            return Component.literal(this.player.getGameProfile().name());
        }
        if ("nickname".equals(key)) {
            return this.styledNicknames$parsedNicknameRaw != null
                ? this.styledNicknames$parsedNicknameRaw.copy()
                : Component.empty();
        }
        return Component.empty();
    }
}
