package nullblade.railworld;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.StructureBlockMode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.structure.*;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;
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
    private final int extraSize;
    private final TagKey<Block> baseBlock;
    private final Identifier defaultBaseBlock;

    protected InfiniteVeinStructure(Config config,
                                    List<BlockConfig> blocks,
                                    int x,
                                    int y,
                                    int z,
                                    int size,
                                    int ySize,
                                    int perOnce,
                                    int ticksPerPlace,
                                    int coreY,
                                    List<Identifier> decors,
                                    TagKey<Block> replaces,
                                    int extraSize,
                                    TagKey<Block> baseBlock, Identifier defaultBaseBlock) {
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
        this.extraSize = extraSize;
        this.baseBlock = baseBlock;
        this.defaultBaseBlock = defaultBaseBlock;
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
                    TagKey.codec(Registries.BLOCK.getKey()).fieldOf("replaces").forGetter((obj) -> obj.replaces),
                    Codecs.POSITIVE_INT.fieldOf("extraSize").forGetter((obj) -> obj.size),
                    TagKey.codec(Registries.BLOCK.getKey()).optionalFieldOf("allowedBase", BlockTags.REPLACEABLE).forGetter((obj) -> obj.baseBlock),
                    Identifier.CODEC.optionalFieldOf("defaultBaseBlock", new Identifier("minecraft:stone")).forGetter((obj) -> obj.defaultBaseBlock)
                    ).apply(instance, InfiniteVeinStructure::new)
    );


    @Override
    protected Optional<StructurePosition> getStructurePosition(Context context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getCenterX();
        int z = chunkPos.getCenterZ();
        var pos = new BlockPos(x, 0, z);

        return Optional.of(new StructurePosition(pos, (collector) -> {
            try {
                Identifier template = decors.get(context.random().nextBetween(0, decors.size()-1));
                collector.addPiece(new Piece(
                        this, RailWorld.structurePieceType, context.structureTemplateManager(), template, template.toString(), new StructurePlacementData().setBoundingBox(BlockBox.create(pos, pos)), pos
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

        private final TagKey<Block> baseBlock;
        private final Block defaultBaseBlock;

        public Piece(InfiniteVeinStructure structure, StructurePieceType type, StructureTemplateManager structureTemplateManager, Identifier id, String template, StructurePlacementData placementData, BlockPos pos) {
            super(type, 0, structureTemplateManager, id, template, placementData, pos);
            this.configs = structure.blocks;
            this.xOffset = structure.x;
            this.yOffset = structure.y;
            this.zOffset = structure.z;
            this.size = structure.size + placementData.getRandom(pos).nextInt(structure.extraSize);
            this.ySize = structure.ySize;
            this.perOnce = structure.perOnce;
            this.ticksPerPlace = structure.ticksPerPlace;
            this.coreY = structure.coreY;
            this.replaces = structure.replaces.id().toString();
            this.baseBlock = structure.baseBlock;
            this.defaultBaseBlock = Registries.BLOCK.get(structure.defaultBaseBlock);
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
            this.baseBlock = TagKey.of(Registries.BLOCK.getKey(), new Identifier(nbt.getString("baseBlock")));
            this.defaultBaseBlock = Registries.BLOCK.get(new Identifier(nbt.getString("defaultBaseBlock")));
        }

        @Override
        protected void writeNbt(StructureContext context, NbtCompound nbt) {
            super.writeNbt(context, nbt);
            saveToNbt(nbt);

            nbt.putString("baseBlock", baseBlock.id().toString());
            nbt.putString("defaultBaseBlock",Registries.BLOCK.getId(defaultBaseBlock).toString());
        }

        @Override
        public StructurePlacementData getPlacementData() {
            return super.getPlacementData().setBoundingBox(BlockBox.create(pos, pos));
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

        int counter = 0;

        public void superGenerate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, BlockPos pivot) {
            this.placementData.setBoundingBox(chunkBox);
            this.boundingBox = this.template.calculateBoundingBox(this.placementData, this.pos);
            if (this.template.place(world, this.pos, pivot, this.placementData, random, 2)) {
                List<StructureTemplate.StructureBlockInfo> list = this.template.getInfosForBlock(this.pos, this.placementData, Blocks.STRUCTURE_BLOCK);

                for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
                    if (structureBlockInfo.nbt() != null) {
                        StructureBlockMode structureBlockMode = StructureBlockMode.valueOf(structureBlockInfo.nbt().getString("mode"));
                        if (structureBlockMode == StructureBlockMode.DATA) {
                            if (structureBlockInfo.nbt().getString("metadata").equals("vein")) {
                                BlockPos pos = structureBlockInfo.pos();
                                var nbt = new NbtCompound();
                                saveToNbt(nbt);

                                world.setBlockState(pos, RailWorld.infiniteVein.getDefaultState(), 3);
                                var state = world.getBlockState(pos);
                                BlockEntity ent = new InfiniteVein.InfiniteVeinEntity(pos, state);
                                ent.readNbt(nbt);
                                world.getChunk(pos).setBlockEntity(ent);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void generate(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot) {
            try {
                var size = template.getSize();

                BlockPos add = new BlockPos((int)Math.floor((double) size.getX() /2), 0, (int)Math.floor((double) size.getZ() /2));

                pos = pos.withY(world.getTopY(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ())).subtract(add);
                placementData.setPosition(new BlockPos(0, 0, 0));
                superGenerate(world, structureAccessor, chunkGenerator, random, chunkBox, pivot);

                pos = pos.add(add);

                BlockState stateGround = world.getBlockState(pos.add(0, -1, 0));
                if (stateGround.isIn(baseBlock)) {
                    stateGround = defaultBaseBlock.getDefaultState();
                }

                for (int x = -size.getX()/2-1 ; x < size.getX()/2+1 ; x++) {
                    for (int z = -size.getZ()/2-1 ;z < size.getZ()/2+1 ; z++) {
                        for (int y = 1; y < 20; y++) {
                            BlockPos p = pos.add(x, - y, z);
                            BlockState state = world.getBlockState(p);
                            if (state.isIn(baseBlock)) {
                                world.setBlockState(p, stateGround, 3);
                            } else {
                                break;
                            }
                        }
                    }
                }

                pos = pos.add(0, coreY, 0);

                counter ++;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void handleMetadata(String metadata, BlockPos pos, ServerWorldAccess world, Random random, BlockBox boundingBox) {}
    }

    @Override
    public StructureType<?> getType() {
        System.out.println("Got type");
        return RailWorld.structure;
    }
}
