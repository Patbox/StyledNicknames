package eu.pb4.stylednicknames.mixin;

import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import eu.pb4.playerdata.api.PlayerDataApi;
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
    private String sn_nickname = null;
    @Unique
    private Text sn_parsedNickname = null;
    @Unique
    private boolean sn_requirePermission = true;

    @Override
    public void sn_loadData() {
        try {
            NbtString nickname = PlayerDataApi.getGlobalDataFor(player, id("nickname"), NbtString.TYPE);
            NbtByte permissions = PlayerDataApi.getGlobalDataFor(player, id("permission"), NbtByte.TYPE);

            if (nickname != null) {
                this.sn_set(nickname.asString(), permissions.byteValue() > 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sn_set(String nickname, boolean requirePermission) {
        Config config = ConfigManager.getConfig();
        ServerCommandSource source = player.getCommandSource();
        if (nickname == null || nickname.isEmpty() || (requirePermission && !Permissions.check(source, "stylednicknames.use", ConfigManager.getConfig().configData.allowByDefault ? 0 : 2))) {
            this.sn_nickname = null;
            this.sn_requirePermission = false;
            this.sn_parsedNickname = null;
            PlayerDataApi.setGlobalDataFor(this.player, id("nickname"), null);
            PlayerDataApi.setGlobalDataFor(this.player, id("permission"), NbtByte.of(false));
        } else {
            this.sn_nickname = nickname;
            this.sn_requirePermission = requirePermission;
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

            this.sn_parsedNickname = TextParserUtils.formatText(nickname, handlers::get);
        }

        if (config.configData.changePlayerListName) {
            Objects.requireNonNull(this.player.getServer()).getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, this.player));
        }
    }

    @Override
    public @Nullable String sn_get() {
        return this.sn_nickname;
    }

    @Override
    public @Nullable Text sn_getParsed() {
        return this.sn_parsedNickname;
    }

    @Override
    public @Nullable MutableText sn_getOutput() {
        return this.sn_parsedNickname != null ? (MutableText) Placeholders.parseText(ConfigManager.getConfig().nicknameFormat, Placeholders.PREDEFINED_PLACEHOLDER_PATTERN, Map.of("nickname", this.sn_parsedNickname, "name", this.sn_parsedNickname)) : null;
    }

    @Override
    public MutableText sn_getOutputOrVanilla() {
        return this.sn_parsedNickname != null ? (MutableText) Placeholders.parseText(ConfigManager.getConfig().nicknameFormat, Placeholders.PREDEFINED_PLACEHOLDER_PATTERN, Map.of("nickname", this.sn_parsedNickname, "name", this.sn_parsedNickname)) : this.player.getName().copy();
    }

    @Override
    public boolean sn_requiresPermission() {
        return this.sn_requirePermission;
    }

    @Override
    public boolean sn_shouldDisplay() {
        return this.sn_parsedNickname != null && (!this.sn_requirePermission || Permissions.check(this.player, "stylednicknames.use", ConfigManager.getConfig().configData.allowByDefault ? 0 : 3));
    }
}
