package eu.pb4.stylednicknames.mixin;

import eu.pb4.stylednicknames.NicknameHolder;
import eu.pb4.stylednicknames.config.ConfigManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Unique private boolean styledNicknames$ignoreNextCall = false;
    @Unique private Text styledNicknames$cachedName = null;
    @Unique private int styledNicknames$cachedAge = -999;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"))
    private Text styledNicknames$replaceName(Text text) {
        try {
            if (ConfigManager.isEnabled() && ConfigManager.getConfig().configData.changeDisplayName) {
                if (this.styledNicknames$cachedAge == this.age) {
                    return this.styledNicknames$cachedName;
                }

                if (!this.styledNicknames$ignoreNextCall) {
                    this.styledNicknames$ignoreNextCall = true;
                    var holder = NicknameHolder.of(this);
                    if (holder != null && holder.styledNicknames$shouldDisplay()) {
                        Text name = holder.styledNicknames$getOutput();
                        if (name != null) {
                            this.styledNicknames$ignoreNextCall = false;
                            this.styledNicknames$cachedName = name;
                            this.styledNicknames$cachedAge = this.age;
                            return name;
                        }
                    }
                    this.styledNicknames$ignoreNextCall = false;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return text;
    }

}