package nullblade.railworld;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    public static InfiniteVein infiniteVein;

    public static StructureType<InfiniteVeinStructure> structure;

    public static StructurePieceType structurePieceType;
}
