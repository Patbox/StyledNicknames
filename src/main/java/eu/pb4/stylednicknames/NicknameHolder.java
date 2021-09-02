package eu.pb4.stylednicknames;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

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
            return LiteralText.EMPTY.shallowCopy();
        }

        @Override
        public boolean sn_requiresPermission() {
            return false;
        }

        @Override
        public boolean sn_shouldDisplay() {
            return false;
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

    boolean sn_shouldDisplay();
}
