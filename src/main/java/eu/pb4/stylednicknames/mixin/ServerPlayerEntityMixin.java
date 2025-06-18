package eu.pb4.stylednicknames.mixin;

import com.mojang.authlib.GameProfile;
import eu.pb4.stylednicknames.NicknameHolder;
import eu.pb4.stylednicknames.config.ConfigManager;
import eu.pb4.stylednicknames.config.data.ConfigData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "getPlayerListName", at = @At("TAIL"), cancellable = true)
    private void styledNicknames$replacePlayerListName(CallbackInfoReturnable<Text> cir) {
        try {
            if (ConfigManager.isEnabled()) {
                ConfigData data = ConfigManager.getConfig().configData;
                if (data.changePlayerListName) {
                    var holder = NicknameHolder.of(this);
                    if (holder != null && holder.styledNicknames$shouldDisplay()) {
                        cir.setReturnValue(Team.decorateName(this.getScoreboardTeam(), holder.styledNicknames$getOutput()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
