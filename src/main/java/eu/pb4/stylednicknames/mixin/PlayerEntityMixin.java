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
    @Unique boolean styledNicknames$ignoreNextCall = false;

    @ModifyArg(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"))
    private Text styledNicknames$replaceName(Text text) {
        try {
            if (ConfigManager.isEnabled() && ConfigManager.getConfig().configData.changeDisplayName) {
                if (!this.styledNicknames$ignoreNextCall) {
                    this.styledNicknames$ignoreNextCall = true;
                    var holder = NicknameHolder.of(this);
                    if (holder != null && holder.styledNicknames$shouldDisplay()) {
                        Text name = holder.styledNicknames$getOutput();
                        if (name != null) {
                            this.styledNicknames$ignoreNextCall = false;
                            return name;
                        }
                    }
                    this.styledNicknames$ignoreNextCall = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

}