package eu.pb4.stylednicknames;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;

public interface NicknameHolder {
    NicknameHolder EMPTY = new NicknameHolder() {
        @Override
        public void styledNicknames$set(String nickname, boolean requirePermission) {
        }

        @Override
        public @Nullable String styledNicknames$get() {
            return null;
        }

        @Override
        public @Nullable Text styledNicknames$getParsed() {
            return null;
        }

        @Override
        public @Nullable MutableText styledNicknames$getOutput() {
            return null;
        }

        @Override
        public MutableText styledNicknames$getOutputOrVanilla() {
            return Text.empty();
        }

        @Override
        public boolean styledNicknames$requiresPermission() {
            return false;
        }

        @Override
        public void styledNicknames$loadData() {}

        @Override
        public boolean styledNicknames$shouldDisplay() {
            return false;
        }

        @Override
        public Function<String, Text> styledNicknames$placeholdersCommand() {
            return x -> Text.empty();
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

    void styledNicknames$set(String nickname, boolean requirePermission);

    @Nullable
    String styledNicknames$get();

    @Nullable
    Text styledNicknames$getParsed();

    @Nullable
    MutableText styledNicknames$getOutput();

    MutableText styledNicknames$getOutputOrVanilla();

    boolean styledNicknames$requiresPermission();

    void styledNicknames$loadData();

    boolean styledNicknames$shouldDisplay();

    Function<String, Text> styledNicknames$placeholdersCommand();
}
