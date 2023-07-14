package nullblade.railworld;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class InfiniteVein extends BlockWithEntity {
    public InfiniteVein() {
        super(AbstractBlock.Settings.create().dropsNothing().hardness(10));

        entity = Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier("railworld"),
                FabricBlockEntityTypeBuilder.create(InfiniteVeinEntity::new, this).build()
                );

    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.cuboid(new Box(0.3, 0.3, 0.3, 0.7, 0.7, 0.7));
    }

    public static BlockEntityType<InfiniteVeinEntity> entity;

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new InfiniteVeinEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, entity, (world1, pos, state1, be) -> be.tick(world1, pos, state1));
    }

    public static class InfiniteVeinEntity extends BlockEntity {

        public int xOffset, yOffset, zOffset;

        public int size;
        public int ySize;
        public int perOnce;
        public int ticksPerPlace;

        public TagKey<Block> replaces;
        public List<Pair<Float, Block>> blocks = new ArrayList<>();;

        @Nullable
        @Override
        public Packet<ClientPlayPacketListener> toUpdatePacket() {
            return BlockEntityUpdateS2CPacket.create(this);
        }

        @Override
        public NbtCompound toInitialChunkDataNbt() {
            return createNbt();
        }


        @Override
        public void readNbt(NbtCompound nbt) {
            try {
                xOffset = nbt.getInt("xOffset");
                yOffset = nbt.getInt("yOffset");
                zOffset = nbt.getInt("zOffset");
                this.size = nbt.getInt("size");
                this.ySize = nbt.getInt("ySize");
                this.perOnce = nbt.getInt("perOnce");
                this.ticksPerPlace = nbt.getInt("ticksPerPlace");
                replaces = TagKey.of(Registries.BLOCK.getKey(), new Identifier(nbt.getString("replaces")));

                int len = nbt.getInt("len");
                for (int i = 0; i < len; i++) {
                    NbtCompound element = nbt.getCompound(String.valueOf(i));
                    float probability = element.getFloat("probability");
                    Identifier id = new Identifier(element.getString("id"));
                    Block block = Registries.BLOCK.get(id);
                    blocks.add(new Pair<>(
                            probability,
                            block
                    ));
                }
            } catch (Exception e) {
                if (getWorld() != null) {
                    e.printStackTrace();
                    RailWorld.logger.error("Breaking Ore vein core at " + getPos());
                    getWorld().setBlockState(getPos(), Blocks.AIR.getDefaultState());
                }
            }
        }

        @Override
        protected void writeNbt(NbtCompound nbt) {
            nbt.putInt("xOffset", xOffset);
            nbt.putInt("yOffset", yOffset);
            nbt.putInt("zOffset", zOffset);

            nbt.putInt("size", size);
            nbt.putInt("ySize", ySize);
            nbt.putInt("perOnce", perOnce);
            nbt.putInt("ticksPerPlace", ticksPerPlace);

            nbt.putInt("len", blocks.size());
            nbt.putString("replaces", replaces.id().toString());

            for (int i = 0 ; i < blocks.size() ; i++) {
                var cfg = blocks.get(i);
                NbtCompound element = new NbtCompound();
                element.putString("id", Registries.BLOCK.getId(cfg.getRight()).toString());
                element.putFloat("probability", cfg.getLeft());
                nbt.put(String.valueOf(i), element);
            }

            super.writeNbt(nbt);
        }

        public InfiniteVeinEntity(BlockPos pos, BlockState state) {
            super(entity, pos, state);
        }

        public int ticks = 0;

        public int len = 234051358;

        public Random random;
        public void tick(World world, BlockPos pos, BlockState state) {
            try {
                  ticks++;
                if (ticks >= ticksPerPlace) {
                    ticks = 0;
                    if (world.isClient()) {
                        return;
                    }

                    for (int i = 0; i < perOnce; i++) {
                        if (len >= size) {
                            len = 0;
                            random = new Random(size + ySize + pos.getX() * 200000200000L + pos.getY() * 10000000000L + pos.getZ() * 100000L);
                        }
                        float rot = random.nextFloat() * (float)Math.PI * 2.0f;
                        float dist = Math.sqrt(random.nextInt(len+1));
                        BlockPos p = pos
                                .add(xOffset, yOffset+ySize, zOffset)
                                .add((int) (Math.sin(rot) * dist),
                                        yOffset+ySize/2 + (random.nextInt(0, (int)(ySize*(len / (float) size)) + 1)) * (random.nextBoolean() ? 1 : -1),
                                        (int) (Math.cos(rot)  * dist));

                        BlockState s = null;
                        float rand = random.nextFloat();

                        for (Pair<Float, Block> block : blocks) {
                            rand -= block.getLeft();
                            if (rand <= 0) {
                                s = block.getRight().getDefaultState();
                                break;
                            }
                        }

                        if (s != null && world.getBlockState(p).isIn(replaces))
                            world.setBlockState(p, s);


                        len++;
                    }
                }
            } catch (Exception e) {
            if (getWorld() != null) {
                e.printStackTrace();
                RailWorld.logger.error("Breaking Ore vein core at " + getPos());
                getWorld().setBlockState(getPos(), Blocks.AIR.getDefaultState());
            }
        }
        }
    }

}
