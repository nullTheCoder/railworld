package nullblade.railworld;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.util.ArrayList;

public class RailWorld implements ModInitializer {

    public static Logger logger = LoggerFactory.getLogger("Railworld");

    @Override
    public void onInitialize() {
        infiniteVein = Registry.register(Registries.BLOCK, new Identifier("railworld:infinite_vein"), new InfiniteVein());
        structure =  Registry.register(Registries.STRUCTURE_TYPE, new Identifier("railworld:vein_structure"), () -> InfiniteVeinStructure.CODEC);
        structurePieceType = Registry.register(Registries.STRUCTURE_PIECE, new Identifier("railworld:vein_structure_piece"), (context, nbt) -> new InfiniteVeinStructure.Piece(context.structureTemplateManager(), nbt));
        Item compass = Registry.register(Registries.ITEM, new Identifier("railworld:vein_seeking_monocle"), new VeinSeekingMonocle());
        try {
            var group = ItemGroups.class.getField("TOOLS").get(null);
            ItemGroupEvents.modifyEntriesEvent((RegistryKey<ItemGroup>) group).register(content -> {
                content.add(compass);
            });
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            buildStructureList(server);
        });

        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            buildStructureList(server);
        });

    }

    private void buildStructureList(MinecraftServer server) {
        var structures = server.getRegistryManager().get(RegistryKeys.STRUCTURE);

        var modsStructures = new ArrayList<RegistryEntry<Structure>>();
        structures.stream().forEach(
                (entry) -> {
                    if (entry instanceof InfiniteVeinStructure) {
                        modsStructures.add(structures.getEntry(entry));
                    }
                }
        );

        modStructures = RegistryEntryList.of(modsStructures);
    }

    public static RegistryEntryList<Structure> modStructures;

    public static InfiniteVein infiniteVein;

    public static StructureType<InfiniteVeinStructure> structure;

    public static StructurePieceType structurePieceType;
}
