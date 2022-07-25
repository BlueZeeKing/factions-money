package dev.blueish.factions.money;

import io.icker.factions.api.events.MiscEvents;
import io.icker.factions.api.persistents.Claim;
import io.icker.factions.api.persistents.Faction;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class FactionsMoneyMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("factions-money");
	public static Config CONFIG = Config.load();
	private static final HashMap<UUID, Integer> STORE = new HashMap<>();

	@Override
	public void onInitialize() {
		MiscEvents.ON_SAVE.register(FactionsMoneyMod::onSave);
	}

	public static void onSave(MinecraftServer server) {
		for (Faction faction : Faction.all()) {
			int count = 0;
			for (Claim claim : faction.getClaims()) {
				ServerWorld world = getWorld(server, claim.level);
				WorldChunk chunk = world.getChunk(claim.x, claim.z);

				for (BlockPos pos : chunk.getBlockEntities().keySet()) {
					BlockState state = chunk.getBlockState(pos);
					Inventory inventory;
					if (state.getBlock() instanceof ChestBlock && (state.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE || state.get(ChestBlock.CHEST_TYPE) == ChestType.RIGHT) && (inventory = ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, true)) != null) {
						for (String key : CONFIG.ITEMS.keySet()) {
							count += inventory.count(Registry.ITEM.get(new Identifier(key))) * CONFIG.ITEMS.get(key);
						}
					}
				}
			}
			STORE.put(faction.getID(), (int) Math.round(count * CONFIG.MULTIPLIER));
		}
	}

	public static ServerWorld getWorld(MinecraftServer server, String key) {
		return server.getWorld(server.getWorldRegistryKeys().stream().filter((worldRegistryKey -> Objects.equals(worldRegistryKey.getValue().toString(), key))).findAny().orElse(null));
	}

	public static int getMoney(Faction faction) {
		if (STORE.containsKey(faction.getID())) {
			return STORE.get(faction.getID());
		}
		return 0;
	}
}
