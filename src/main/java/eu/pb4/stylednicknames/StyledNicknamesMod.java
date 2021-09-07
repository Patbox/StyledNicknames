package eu.pb4.stylednicknames;

import eu.pb4.stylednicknames.command.Commands;
import eu.pb4.stylednicknames.config.ConfigManager;
import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.PlaceholderResult;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class StyledNicknamesMod implements ModInitializer {
	public static final String ID = "stylednicknames";

	public static final Logger LOGGER = LogManager.getLogger("StyledNicknames");
	public static String VERSION = FabricLoader.getInstance().getModContainer("styled-nicknames").get().getMetadata().getVersion().getFriendlyString();

	@Override
	public void onInitialize() {
		Commands.register();
		ServerLifecycleEvents.SERVER_STARTING.register((s) -> ConfigManager.loadConfig());

		PlaceholderAPI.register(new Identifier("styled-nicknames","display_name"), (ctx) -> {
			if (ctx.hasPlayer()) {
				return PlaceholderResult.value(NicknameHolder.of(ctx.getPlayer().networkHandler).sn_getOutputOrVanilla());
			} else {
				return PlaceholderResult.invalid("Not a player!");
			}
		});
	}

	public static final Identifier id(String path) {
		return new Identifier(ID, path);
	}
}
