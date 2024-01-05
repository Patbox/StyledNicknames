package eu.pb4.stylednicknames.mixin;

import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.stylednicknames.NicknameCache;
import eu.pb4.stylednicknames.NicknameHolder;
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
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

            var handlers = new HashMap<String, TextParserV1.TagNodeBuilder>();


            for (var entry : TextParserV1.SAFE.getTags()) {
                if ((config.defaultFormattingCodes.getBoolean(entry.name())
                        || Permissions.check(this.player, "stylednicknames.format." + entry.name(), 2))) {

                    handlers.put(entry.name(), entry.parser());

                    if (entry.aliases() != null) {
                        for (var a : entry.aliases()) {
                            handlers.put(a, entry.parser());
                        }
                    }
                }
            }

            if (config.configData.allowLegacyFormatting) {
                for (Formatting formatting : Formatting.values()) {
                    if (handlers.get(formatting.getName()) != null) {
                        nickname = nickname.replace(String.copyValueOf(new char[]{'&', formatting.getCode()}), "<" + formatting.getName() + ">");
                    }
                }
            }

            this.styledNicknames$parsedNicknameRaw = TextParserUtils.formatText(nickname, handlers::get);
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
        return this.styledNicknames$parsedNicknameRaw != null ? (MutableText) Placeholders.parseText(ConfigManager.getConfig().nicknameFormat, Placeholders.PREDEFINED_PLACEHOLDER_PATTERN, Map.of("nickname", this.styledNicknames$parsedNicknameRaw, "name", this.styledNicknames$parsedNicknameRaw)) : null;
    }

    @Override
    public MutableText styledNicknames$getOutputOrVanilla() {
        return this.styledNicknames$parsedNicknameRaw != null ? (MutableText) Placeholders.parseText(ConfigManager.getConfig().nicknameFormat, Placeholders.PREDEFINED_PLACEHOLDER_PATTERN, Map.of("nickname", this.styledNicknames$parsedNicknameRaw, "name", this.styledNicknames$parsedNicknameRaw)) : this.player.getName().copy();
    }

    @Override
    public boolean styledNicknames$requiresPermission() {
        return this.styledNicknames$requirePermission;
    }

    @Override
    public boolean styledNicknames$shouldDisplay() {
        return this.styledNicknames$parsedNicknameRaw != null && (!this.styledNicknames$requirePermission || Permissions.check(this.player, "stylednicknames.use", ConfigManager.getConfig().configData.allowByDefault ? 0 : 3));
    }

    @Override
    public Map<String, Text> styledNicknames$placeholdersCommand() {
        var name = this.styledNicknames$getOutputOrVanilla();
        return Map.of("nickname", name, "name", name);
    }
}
