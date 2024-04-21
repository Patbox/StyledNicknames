package eu.pb4.stylednicknames.mixin;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.stylednicknames.NicknameCache;
import eu.pb4.stylednicknames.NicknameHolder;
import eu.pb4.stylednicknames.ParserUtils;
import eu.pb4.stylednicknames.config.Config;
import eu.pb4.stylednicknames.config.ConfigManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static eu.pb4.stylednicknames.StyledNicknamesMod.id;


@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin implements NicknameHolder {
    @Shadow
    public ServerPlayerEntity player;
    @Unique
    private String styledNicknames$nickname = null;
    @Unique
    private Text styledNicknames$parsedNicknameRaw = null;
    @Unique
    private boolean colorOnlyMode = false;
    @Unique
    private boolean styledNicknames$requirePermission = true;

    @Override
    public void styledNicknames$loadData() {
        try {
            NbtString nickname = PlayerDataApi.getGlobalDataFor(player, id("nickname"), NbtString.TYPE);
            NbtByte permissions = PlayerDataApi.getGlobalDataFor(player, id("permission"), NbtByte.TYPE);

            if (nickname != null) {
                this.styledNicknames$set(nickname.asString(), permissions.byteValue() > 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void styledNicknames$set(String nickname, boolean requirePermission) {
        Config config = ConfigManager.getConfig();
        ServerCommandSource source = player.getCommandSource();
        if (nickname == null || nickname.isEmpty() || (requirePermission && !Permissions.check(source, "stylednicknames.use", ConfigManager.getConfig().configData.allowByDefault ? 0 : 2))) {
            this.styledNicknames$nickname = null;
            this.styledNicknames$requirePermission = false;
            this.styledNicknames$parsedNicknameRaw = null;
            PlayerDataApi.setGlobalDataFor(this.player, id("nickname"), null);
            PlayerDataApi.setGlobalDataFor(this.player, id("permission"), NbtByte.of(false));
        } else {
            this.styledNicknames$nickname = nickname;
            this.styledNicknames$requirePermission = requirePermission;
            PlayerDataApi.setGlobalDataFor(this.player, id("nickname"), NbtString.of(nickname));
            PlayerDataApi.setGlobalDataFor(this.player, id("permission"), NbtByte.of(requirePermission));

            var text = ParserUtils.getParser(requirePermission ? this.player : null)
                    .parseText(nickname, ParserContext.of());

            this.colorOnlyMode = text.getString().toLowerCase(Locale.ROOT).equals(this.player.getGameProfile().getName().toLowerCase(Locale.ROOT));
            this.styledNicknames$parsedNicknameRaw = text;
        }

        if (config.configData.changePlayerListName) {
            Objects.requireNonNull(this.player.getServer()).getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, this.player));
        }
        ((NicknameCache) this.player).styledNicknames$invalidateCache();
    }

    @Override
    public @Nullable String styledNicknames$get() {
        return this.styledNicknames$nickname;
    }

    @Override
    public @Nullable Text styledNicknames$getParsed() {
        return this.styledNicknames$parsedNicknameRaw;
    }

    @Override
    public @Nullable MutableText styledNicknames$getOutput() {
        return this.styledNicknames$parsedNicknameRaw != null
                ? (this.colorOnlyMode ? ConfigManager.getConfig().nicknameFormatColor : ConfigManager.getConfig().nicknameFormat)
                .toText(ParserContext.of(Config.KEY, (s) -> this.styledNicknames$parsedNicknameRaw)).copy() : null;
    }

    @Override
    public MutableText styledNicknames$getOutputOrVanilla() {
        return this.styledNicknames$parsedNicknameRaw != null
                ? (this.colorOnlyMode ? ConfigManager.getConfig().nicknameFormatColor : ConfigManager.getConfig().nicknameFormat)
                .toText(ParserContext.of(Config.KEY, (s) -> this.styledNicknames$parsedNicknameRaw)).copy() : this.player.getName().copy();    }

    @Override
    public boolean styledNicknames$requiresPermission() {
        return this.styledNicknames$requirePermission;
    }

    @Override
    public boolean styledNicknames$shouldDisplay() {
        return this.styledNicknames$parsedNicknameRaw != null && (!this.styledNicknames$requirePermission || Permissions.check(this.player, "stylednicknames.use", ConfigManager.getConfig().configData.allowByDefault ? 0 : 3));
    }

    @Override
    public Function<String, Text> styledNicknames$placeholdersCommand() {
        var name = this.styledNicknames$getOutputOrVanilla();
        return x -> name;
    }
}
