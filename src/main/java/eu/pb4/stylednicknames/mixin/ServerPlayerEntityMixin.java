package eu.pb4.stylednicknames.mixin;

import eu.pb4.stylednicknames.NicknameHolder;
import eu.pb4.stylednicknames.config.ConfigManager;
import eu.pb4.stylednicknames.config.data.ConfigData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "getPlayerListName", at = @At("TAIL"), cancellable = true)
    private void replacePlayerListName(CallbackInfoReturnable<Text> cir) {
        try {
            if (ConfigManager.isEnabled()) {
                ConfigData data = ConfigManager.getConfig().configData;
                if (data.changePlayerListName) {
                    var holder = NicknameHolder.of(this);
                    if (holder != null && holder.sn_shouldDisplay()) {
                        cir.setReturnValue(holder.sn_getOutput());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
