package eu.pb4.stylednicknames;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface NicknameHolder {
    NicknameHolder EMPTY = new NicknameHolder() {
        @Override
        public void sn_set(String nickname, boolean requirePermission) {
        }

        @Override
        public @Nullable String sn_get() {
            return null;
        }

        @Override
        public @Nullable Text sn_getParsed() {
            return null;
        }

        @Override
        public @Nullable MutableText sn_getOutput() {
            return null;
        }

        @Override
        public MutableText sn_getOutputOrVanilla() {
            return Text.empty();
        }

        @Override
        public boolean sn_requiresPermission() {
            return false;
        }

        @Override
        public void sn_loadData() {}

        @Override
        public boolean sn_shouldDisplay() {
            return false;
        }

        @Override
        public Map<String, Text> sn_placeholdersCommand() {
            return Map.of("nickname", Text.empty(), "name", Text.empty());
        }
    };

    static NicknameHolder of(ServerPlayerEntity player) {
        return (NicknameHolder) player.networkHandler;
    }

    static NicknameHolder of(ServerPlayNetworkHandler handler) {
        return (NicknameHolder) handler;
    }

    static NicknameHolder of(Object possiblePlayer) {
        if (possiblePlayer instanceof ServerPlayerEntity player) {
            return (NicknameHolder) player.networkHandler;
        }
        return EMPTY;
    }

    void sn_set(String nickname, boolean requirePermission);

    @Nullable
    String sn_get();

    @Nullable
    Text sn_getParsed();

    @Nullable
    MutableText sn_getOutput();

    MutableText sn_getOutputOrVanilla();

    boolean sn_requiresPermission();

    void sn_loadData();

    boolean sn_shouldDisplay();

    Map<String, Text> sn_placeholdersCommand();
}
