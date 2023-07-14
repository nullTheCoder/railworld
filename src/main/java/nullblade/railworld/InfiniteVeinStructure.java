package nullblade.railworld;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.PlaceCommand;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.*;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.*;

import java.util.*;

public class InfiniteVeinStructure extends Structure {

    public final List<BlockConfig> blocks;
    private final int x;
    private final int y;
    private final int z;
    private final int size;
    private final int ySize;
    private final int perOnce;
    private final int ticksPerPlace;
    private final int coreY;
    private final List<Identifier> decors;
    private final TagKey<Block> replaces;

    private final static TagKey<Block> replaces_foundation = BlockTags.REPLACEABLE;

    protected InfiniteVeinStructure(Config config, List<BlockConfig> blocks, int x, int y, int z, int size, int ySize, int perOnce, int ticksPerPlace, int coreY, List<Identifier> decors, TagKey<Block> replaces) {
        super(config);
        this.blocks = blocks;
        this.x = x;
        this.y = y;
        this.z = z;
        this.size = size;
        this.ySize = ySize;
        this.perOnce = perOnce;
        this.ticksPerPlace = ticksPerPlace;
        this.coreY = coreY;
        this.decors = decors;
        this.replaces = replaces;
    }

    public static Codec<BlockConfig> ORE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codecs.POSITIVE_FLOAT.fieldOf("probability").forGetter((obj) -> obj.probability),
                    Identifier.CODEC.fieldOf("id").forGetter((obj) -> obj.id)
            ).apply(instance, BlockConfig::new)
    );

    public static class BlockConfig {

        public final float probability;
        private final Identifier id;

        public BlockConfig (float probability, Identifier id) {

            this.probability = probability;
            this.id = id;
        }

    }

    public static Codec<InfiniteVeinStructure> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(configCodecBuilder(instance),
                    Codecs.nonEmptyList(ORE_CODEC.listOf()).fieldOf("ores").forGetter((obj) -> obj.blocks),
                    Codecs.rangedInt(-10000, 10000).fieldOf("x").forGetter((obj) -> obj.x),
                    Codecs.rangedInt(-10000, 10000).fieldOf("y").forGetter((obj) -> obj.y),
                    Codecs.rangedInt(-10000, 10000).fieldOf("z").forGetter((obj) -> obj.z),
                    Codecs.POSITIVE_INT.fieldOf("size").forGetter((obj) -> obj.size),
                    Codecs.POSITIVE_INT.fieldOf("ySize").forGetter((obj) -> obj.ySize),
                    Codecs.POSITIVE_INT.fieldOf("perOnce").forGetter((obj) -> obj.perOnce),
                    Codecs.POSITIVE_INT.fieldOf("ticksPerPlace").forGetter((obj) -> obj.ticksPerPlace),
                    Codecs.rangedInt(-500, 500).fieldOf("coreY").forGetter((obj) -> obj.coreY),
                    Codecs.nonEmptyList(Identifier.CODEC.listOf()).fieldOf("decor").forGetter((obj) -> obj.decors),
                    TagKey.codec(Registries.BLOCK.getKey()).fieldOf("replaces").forGetter((obj) -> obj.replaces)
            ).apply(instance, InfiniteVeinStructure::new)
    );


    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getCenterX();
        int z = chunkPos.getCenterZ();
        int y = context.chunkGenerator().getHeightInGround(x, z, Heightmap.Type.WORLD_SURFACE, context.world(), context.noiseConfig());
        var pos = new BlockPos(x, y, z);
        return Optional.of(new StructurePosition(pos, (collector) -> {
            try {
                Identifier template = decors.get(context.random().nextBetween(0, decors.size()-1));
                collector.addPiece(new Piece(
                        this, RailWorld.structurePieceType, context.structureTemplateManager(), template, template.toString(), new StructurePlacementData().setPosition(pos).setBoundingBox(BlockBox.create(pos, pos)), pos
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public static class Piece extends SimpleStructurePiece {

        private final List<BlockConfig> configs;
        private final int xOffset;
        private int yOffset;
        private int zOffset;

        private final int size;
        private final int ySize;
        private final int perOnce;
        private final int ticksPerPlace;
        private final int coreY;

        private final String replaces;

        public Piece(InfiniteVeinStructure structure, StructurePieceType type, StructureTemplateManager structureTemplateManager, Identifier id, String template, StructurePlacementData placementData, BlockPos pos) {
            super(type, 0, structureTemplateManager, id, template, placementData, pos);
            this.configs = structure.blocks;
            this.xOffset = structure.x;
            this.yOffset = structure.y;
            this.zOffset = structure.z;
            this.size = structure.size;
            this.ySize = structure.ySize;
            this.perOnce = structure.perOnce;
            this.ticksPerPlace = structure.ticksPerPlace;
            this.coreY = structure.coreY;
            this.replaces = structure.replaces.id().toString();
        }

        public Piece(StructureTemplateManager manager, NbtCompound nbt) {
            super(RailWorld.structurePieceType, nbt, manager, (identifier) -> new StructurePlacementData().setRotation(BlockRotation.NONE));
            configs = new ArrayList<>();
            int len = nbt.getInt("len");
            for (int i = 0 ; i < len ; i++) {
                NbtCompound element = nbt.getCompound(String.valueOf(i));
                float probability = element.getFloat("probability");
                Identifier id = new Identifier(element.getString("id"));
                BlockConfig cfg = new BlockConfig(probability, id);
                configs.add(cfg);
            }
            xOffset = nbt.getInt("xOffset");
            yOffset = nbt.getInt("yOffset");
            zOffset = nbt.getInt("zOffset");
            this.size = nbt.getInt("size");
            this.ySize = nbt.getInt("ySize");
            this.perOnce = nbt.getInt("perOnce");
            this.ticksPerPlace = nbt.getInt("ticksPerPlace");
            this.coreY = nbt.getInt("coreY");
            this.replaces = nbt.getString("replaces");
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            saveToNbt(nbt);

            nbt.putInt("len", configs.size());
        }

        private void saveToNbt(NbtCompound nbt) {
            nbt.putInt("xOffset", xOffset);
            nbt.putInt("yOffset", yOffset);
            nbt.putInt("zOffset", zOffset);

            nbt.putInt("size", size);
            nbt.putInt("ySize", ySize);
            nbt.putInt("perOnce", perOnce);
            nbt.putInt("ticksPerPlace", ticksPerPlace);
            nbt.putString("replaces", replaces);


            nbt.putInt("len", configs.size());

            for (int i = 0 ; i < configs.size() ; i++) {
                BlockConfig cfg = configs.get(i);
                NbtCompound element = new NbtCompound();
                element.putString("id", cfg.id.toString());
                element.putFloat("probability", cfg.probability);
                nbt.put(String.valueOf(i), element);
            }
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            try {
                var nbt = new NbtCompound();
                saveToNbt(nbt);
                var size = template.getSize();

                BlockPos add = new BlockPos((int)Math.floor((double) size.getX() /2), 0, (int)Math.floor((double) size.getZ() /2));

                pos = pos.withY(world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX(), pos.getZ())).subtract(add);
                BlockState stateGround = world.getBlockState(pos.add(0, -3, 0));
                if (stateGround.isIn(replaces_foundation)) {
                    stateGround = Blocks.DIRT.getDefaultState();
                }
                super.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, pivot);
                pos = pos.add(0, coreY, 0).add(add);
                for (int x = -size.getX()/2-1 ; x < size.getX()/2+1 ; x++) {
                    for (int z = -size.getZ()/2-1 ;z < size.getZ()/2+1 ; z++) {
                        for (int y = 0; y < 10; y++) {
                            BlockPos p = pos.add(x, -2 - y, z);
                            BlockState state = world.getBlockState(p);
                            if (state.isIn(replaces_foundation) && !state.isLiquid()) {
                                world.setBlockState(p, stateGround, 3);
                            } else {
                                break;
                            }
                        }
                    }
                }

                world.setBlockState(pos, RailWorld.infiniteVein.getDefaultState(), 3);
                var ent = world.getBlockEntity(pos);
                assert ent != null;
                ent.readNbt(nbt);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {

        }
    }

    @Override
    public StructureType<?> getType() {
        System.out.println("Got type");
        return RailWorld.structure;
    }
}
