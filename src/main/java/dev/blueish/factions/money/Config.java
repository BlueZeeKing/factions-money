package dev.blueish.factions.money;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

public class Config {
    private static final File file = FabricLoader.getInstance().getGameDir().resolve("config").resolve("factions-money.json").toFile();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Config load() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        try {
            if (!file.exists()) {
                file.getParentFile().mkdir();

                Config defaults = new Config();

                FileWriter writer = new FileWriter(file);
                gson.toJson(defaults, writer);
                writer.close();

                return defaults;
            }

            return gson.fromJson(new FileReader(file), Config.class);
        } catch (Exception e) {
            FactionsMoneyMod.LOGGER.error("An error occurred reading the factions money config file", e);
            return new Config();
        }
    }

    @SerializedName("items")
    public HashMap<String, Integer> ITEMS = new HashMap<>();

    @SerializedName("multiplier")
    public double MULTIPLIER = 0.01;

    @SerializedName("useMax")
    public boolean USE_MAX = false;

    @SerializedName("ticksToReload")
    public int TICKS_TO_RELOAD = 4800;
}