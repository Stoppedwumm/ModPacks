package net.stoppedwumml.modpacks;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.FilePackResources;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@EventBusSubscriber(modid = ModPacks.MODID, value = Dist.CLIENT)
public class ResourcePackLoader {

    // You need to set this to the folder where you keep your modpack zips
    public static final File PACKS_FOLDER = new File("modpacks");

    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;

        if (!PACKS_FOLDER.exists()) PACKS_FOLDER.mkdirs();

        // 1. Find all zip files
        File[] files = PACKS_FOLDER.listFiles((dir, name) -> name.endsWith(".zip"));
        if (files == null) return;

        for (File file : files) {
            // 2. Register each zip as a Resource Pack
            String packId = "custompack_" + file.getName().toLowerCase().replace(".zip", "");

            // Create a PackLocationInfo (Name, Title, Source, IsHidden)
            PackLocationInfo info = new PackLocationInfo(
                    packId,
                    Component.literal("Modpack: " + file.getName()),
                    PackSource.BUILT_IN,
                    Optional.empty()
            );

            // Create configuration (Always enabled = true)
            PackSelectionConfig config = new PackSelectionConfig(true, Pack.Position.TOP, false);

            // Create the Pack object using FilePackResources (reads from Zip)
            Pack pack = Pack.readMetaAndCreate(
                    info,
                    new FilePackResources.FileResourcesSupplier(file),
                    PackType.CLIENT_RESOURCES,
                    config
            );

            if (pack != null) {
                event.addRepositorySource(new RepositorySource() {
                    @Override
                    public void loadPacks(Consumer<Pack> consumer) {
                        consumer.accept(pack);
                    }
                });
            }
        }
    }
}