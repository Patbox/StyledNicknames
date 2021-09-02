package eu.pb4.stylednicknames.mixin;

import eu.pb4.stylednicknames.NicknameHolder;
import eu.pb4.stylednicknames.config.ConfigManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Unique boolean sn_ignoreNextCall = false;

    @ModifyArg(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"))
    private Text replaceName(Text text) {
        if (ConfigManager.getConfig().configData.changeDisplayName) {
            if (!this.sn_ignoreNextCall) {
                this.sn_ignoreNextCall = true;
                var holder = NicknameHolder.of(this);
                if (holder.sn_shouldDisplay()) {
                    Text name = holder.sn_getOutput();
                    if (name != null) {
                        this.sn_ignoreNextCall = false;
                        return name;
                    }
                }
                this.sn_ignoreNextCall = false;
            }
        }
        return text;
    }

}