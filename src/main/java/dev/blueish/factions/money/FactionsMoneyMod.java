package dev.blueish.factions.money;

import io.icker.factions.api.persistents.Claim;
import io.icker.factions.api.persistents.Faction;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
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
		ServerLifecycleEvents.SERVER_STARTED.register(FactionsMoneyMod::checkClaims);
		ServerTickEvents.END_SERVER_TICK.register((server -> {
			if (server.getTicks() % CONFIG.TICKS_TO_RELOAD == 0) {
				checkClaims(server);
			}
		}));
	}

	public static void checkClaims(MinecraftServer server) {
		for (Faction faction : Faction.all()) {
			int count = 0;
			for (Claim claim : faction.getClaims()) {
				ServerWorld world = getWorld(server, claim.level);
				WorldChunk chunk = world.getChunk(claim.x, claim.z);
				BarrelBlockEntity entity;

				for (BlockPos pos : chunk.getBlockEntities().keySet()) {
					BlockState state = chunk.getBlockState(pos);
					if (state.getBlock() instanceof ChestBlock && (state.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE || state.get(ChestBlock.CHEST_TYPE) == ChestType.RIGHT)) {
						count += countInventory(ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, true));
					} else if (state.getBlock() instanceof BarrelBlock && (entity = (BarrelBlockEntity)chunk.getBlockEntity(pos)) != null) {
						for (String key : CONFIG.ITEMS.keySet()) {
							count += entity.count(Registry.ITEM.get(new Identifier(key))) * CONFIG.ITEMS.get(key);
						}
					}
				}
			}
			count += countInventory(faction.getSafe());
			STORE.put(faction.getID(), (int) Math.round(count * CONFIG.MULTIPLIER));
		}
	}

	private static int countInventory(Inventory inventory) {
		if (inventory == null) return 0;
		int count = 0;
		for (String key : CONFIG.ITEMS.keySet()) {
			count += inventory.count(Registry.ITEM.get(new Identifier(key))) * CONFIG.ITEMS.get(key);
		}
		return count;
	}

	private static ServerWorld getWorld(MinecraftServer server, String key) {
		return server.getWorld(server.getWorldRegistryKeys().stream().filter((worldRegistryKey -> Objects.equals(worldRegistryKey.getValue().toString(), key))).findAny().orElse(null));
	}

	public static int getMoney(Faction faction) {
		if (STORE.containsKey(faction.getID())) {
			return STORE.get(faction.getID());
		}
		return 0;
	}
}
