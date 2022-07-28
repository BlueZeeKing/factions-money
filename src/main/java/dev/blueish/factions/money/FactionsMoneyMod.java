package dev.blueish.factions.money;

import io.icker.factions.api.persistents.Claim;
import io.icker.factions.api.persistents.Faction;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
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
		LOGGER.info(String.valueOf(CONFIG.TICKS_TO_RELOAD));
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

				for (BlockPos pos : chunk.getBlockEntities().keySet()) {
					BlockState state = chunk.getBlockState(pos);
					BlockEntity entity;

					if ((entity = chunk.getBlockEntity(pos)) == null) continue;

					if (state.getBlock() instanceof ChestBlock && (state.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE || state.get(ChestBlock.CHEST_TYPE) == ChestType.RIGHT)) {
						count += countInventory(Objects.requireNonNull(ChestBlock.getInventory((ChestBlock) state.getBlock(), state, world, pos, true))::count);
						continue;
					}
					if (state.getBlock() instanceof BarrelBlock) {
						count += countInventory(((BarrelBlockEntity)entity)::count);
						continue;
					}
					if (state.getBlock() instanceof ShulkerBoxBlock) {
						count += countInventory(((ShulkerBoxBlockEntity)entity)::count);
					}
				}
			}
			count += countInventory(faction.getSafe()::count);
			STORE.put(faction.getID(), (int) Math.round(count * CONFIG.MULTIPLIER));
		}
	}

	private static int countInventory(InventoryCounter counter) {
		if (counter == null) return 0;
		int count = 0;
		for (String key : CONFIG.ITEMS.keySet()) {
			count += counter.count(Registry.ITEM.get(new Identifier(key))) * CONFIG.ITEMS.get(key);
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

	@FunctionalInterface
	public interface InventoryCounter {
		int count(Item item);
	}
}
