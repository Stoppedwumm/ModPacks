package net.stoppedwumml.modpacks;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod(ModPacks.MODID)
public class ModPacks {
    public static final String MODID = "modpacks";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File PACK_DIR = new File(CWD, "modpacks");

    // Use a List instead of Array for easier manipulation
    public static List<File> modpacks = new ArrayList<>();

    public ModPacks(IEventBus modEventBus, ModContainer modContainer) {
        // 1. Create directory
        if (PACK_DIR.mkdirs()) {
            LOGGER.info("Created packs directory at: {}", PACK_DIR.getAbsolutePath());
        } else {
            LOGGER.info("Scanning packs directory at: {}", PACK_DIR.getAbsolutePath());
        }

        //NeoForge.EVENT_BUS.register(this);

        // 2. Filter files safely
        File[] allFiles = PACK_DIR.listFiles();

        if (allFiles != null) {
            // Filter: Must be a file AND end with .zip
            modpacks = Arrays.stream(allFiles)
                    .filter(File::isFile)
                    .filter(f -> f.getName().toLowerCase().endsWith(".zip"))
                    .toList();
        }

        // 3. Log what we found
        if (modpacks.isEmpty()) {
            LOGGER.warn("No .zip modpacks found in {}", PACK_DIR.getAbsolutePath());
        } else {
            LOGGER.info("Found {} modpacks. Starting registration...", modpacks.size());

            // 4. Register
            for (File modpack : modpacks) {
                LOGGER.info("Processing pack: {}", modpack.getName());
                RegisterProcess.registerPack(modpack);
            }
        }
        RegisterProcess.register(modEventBus);
    }
}