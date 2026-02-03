package net.stoppedwumml.modpacks;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.stoppedwumml.modpacks.blocks.ScriptedBlock;
import net.stoppedwumml.modpacks.blocks.ScriptedBlockEntity;
import net.stoppedwumml.modpacks.items.ScriptedItem;
import net.stoppedwumml.modpacks.scripting.ScriptManager;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RegisterProcess {
    private static final List<DeferredRegister<?>> ALL_REGISTRIES = new ArrayList<>();

    // We need a single BlockEntityType that handles all our custom scripted blocks
    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES;
    public static Supplier<BlockEntityType<ScriptedBlockEntity>> SCRIPTED_BE_TYPE;

    public static void registerPack(File pack) {
        try (ZipFile zip = new ZipFile(pack)) {
            String packJson = Utils.getZipContentAsString(zip, "pack.json");
            JsonObject packInfo = JsonParser.parseString(packJson).getAsJsonObject();
            String modId = packInfo.get("modid").getAsString();

            DeferredRegister<Item> itemReg = DeferredRegister.createItems(modId);
            DeferredRegister<Block> blockReg = DeferredRegister.create(Registries.BLOCK, modId);
            BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modId);

            ALL_REGISTRIES.add(itemReg);
            ALL_REGISTRIES.add(blockReg);
            ALL_REGISTRIES.add(BLOCK_ENTITIES);

            // 1. Load Global Script
            ZipEntry global = zip.getEntry("mod/scripts/global.js");
            if (global != null) {
                ScriptManager.loadGlobalScript(Utils.getZipContentAsString(zip, "mod/scripts/global.js"));
            }

            // 2. Load Blocks
            loadBlocks(zip, modId, blockReg, itemReg);

            // 3. Load Items
            loadItems(zip, modId, itemReg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadBlocks(ZipFile zip, String modId, DeferredRegister<Block> bReg, DeferredRegister<Item> iReg) {
        Enumeration<? extends ZipEntry> entries = zip.entries();
        List<Block> blocksToTrack = new ArrayList<>();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith("mod/block/") && entry.getName().endsWith(".json")) {
                try {
                    String id = Utils.getFileName(entry.getName());
                    JsonObject json = JsonParser.parseString(Utils.getZipContentAsString(zip, entry.getName())).getAsJsonObject();

                    boolean ticks = json.has("ticks") && json.get("ticks").getAsBoolean();

                    BlockBehaviour.Properties props = BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
                    if (json.has("hardness")) props.strength(json.get("hardness").getAsFloat());

                    // Register Block
                    Supplier<Block> bSub = bReg.register(id, () -> new ScriptedBlock(props, id, ticks));

                    // Register BlockItem
                    iReg.register(id, () -> new BlockItem(bSub.get(), new Item.Properties()));

                    // Handle Scripting
                    String scriptPath = "mod/scripts/" + id + ".js";
                    if (zip.getEntry(scriptPath) != null) {
                        ScriptManager.loadScript(id, Utils.getZipContentAsString(zip, scriptPath));
                    }

                } catch (Exception e) { e.printStackTrace(); }
            }
        }

        // Finalize BlockEntity registration (this is a simplified version for dynamic loading)
        if (SCRIPTED_BE_TYPE == null) {
            SCRIPTED_BE_TYPE = BLOCK_ENTITIES.register("scripted_be", () ->
                    BlockEntityType.Builder.of((pos, state) -> new ScriptedBlockEntity(pos, state, "dynamic"), Blocks.STONE).build(null));
        }
    }

    private static void loadItems(ZipFile zip, String modId, DeferredRegister<Item> iReg) {
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().startsWith("mod/item/") && entry.getName().endsWith(".json")) {
                try {
                    String id = Utils.getFileName(entry.getName());
                    JsonObject json = JsonParser.parseString(Utils.getZipContentAsString(zip, entry.getName())).getAsJsonObject();

                    Item.Properties props = new Item.Properties();

                    String scriptPath = "mod/scripts/" + id + ".js";
                    if (zip.getEntry(scriptPath) != null) {
                        ScriptManager.loadScript(id, Utils.getZipContentAsString(zip, scriptPath));
                        iReg.register(id, () -> new ScriptedItem(props, id));
                    } else {
                        iReg.register(id, () -> new Item(props));
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    public static void register(IEventBus bus) {
        for (DeferredRegister<?> r : ALL_REGISTRIES) {
            r.register(bus);
        }
    }
}