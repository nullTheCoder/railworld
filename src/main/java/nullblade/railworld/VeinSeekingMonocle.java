package nullblade.railworld;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ParticleCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Vector3d;

public class VeinSeekingMonocle extends ArmorItem {

    public VeinSeekingMonocle() {
        super(new ArmorMaterial() {
            @Override
            public int getDurability(Type type) {
                return 240;
            }

            @Override
            public int getProtection(Type type) {
                return 1;
            }

            @Override
            public int getEnchantability() {
                return 1;
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ITEM_ARMOR_EQUIP_GOLD;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.ofStacks(Items.IRON_INGOT.getDefaultStack());
            }

            @Override
            public String getName() {
                return "railworld_monocle";
            }

            @Override
            public float getToughness() {
                return 1;
            }

            @Override
            public float getKnockbackResistance() {
                return 0;
            }
        }, Type.HELMET, new Settings().maxCount(1));
    }



    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof ServerPlayerEntity player) || world.isClient || !(world instanceof ServerWorld serWorld) || slot != EquipmentSlot.HEAD.getEntitySlotId()) {
            return;
        }

        var nbt = stack.getNbt();
        if (nbt == null) {
            nbt = new NbtCompound();
        }
        int run = nbt.getInt("run");

        Random rand = world.getRandom();

        if (run <= 0) {
            var found = serWorld.getChunkManager().getChunkGenerator().locateStructure(serWorld, RailWorld.modStructures, entity.getBlockPos(), 20, false);
            if (found == null || found.getFirst() == null) {
                run = 200;
                nbt.putBoolean("targeting", false);
            } else {
                BlockPos pos = found.getFirst();
                nbt.putInt("tX", pos.getX());
                nbt.putInt("tZ", pos.getZ());
                run = 30;

                double x = entity.getX() + (rand.nextDouble()-0.5)*5;
                nbt.putDouble("x", x);
                double z = entity.getZ() + (rand.nextDouble()-0.5)*5;
                nbt.putDouble("z", z);
                double y = entity.getY() + rand.nextDouble() * 0.5;
                nbt.putDouble("y", y);
                
                if (new Vector3d(x, y, z).distance(new Vector3d(pos.getX(), y, pos.getZ())) < 48) {
                    nbt.putBoolean("targeting", false);
                    run = 100;
                } else {
                    nbt.putBoolean("targeting", true);
                }
            }
        } else {
            if (nbt.getBoolean("targeting")) {
                int targetX = nbt.getInt("tX");
                int targetZ = nbt.getInt("tZ");

                double x = nbt.getDouble("x");
                double y = nbt.getDouble("y");
                double z = nbt.getDouble("z");
                Vector3d pos = new Vector3d(x, y, z);

                Vector3d target = new Vector3d(targetX, pos.y, targetZ);

                double dist = pos.distance(target);
                Vector3d hypot = target.sub(pos);


                pos.add(hypot.div(dist).mul(0.5)).add((rand.nextDouble() - 0.5) * 0.5,(rand.nextDouble() - 0.5) * 0.5, (rand.nextDouble() - 0.5)  * 0.5);

                ((ServerWorld) world).spawnParticles(player, ParticleTypes.HAPPY_VILLAGER, true, pos.x, pos.y, pos.z, 5, 0.0, 0.0, 0.0, 1.0);

                nbt.putDouble("x", pos.x);
                nbt.putDouble("y", pos.y);
                nbt.putDouble("z", pos.z);
            }
        }
        run--;
        nbt.putInt("run", run);

    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack item = user.getStackInHand(hand);

        if (world.isClient || !(world instanceof ServerWorld serWorld)) {
            return TypedActionResult.success(item);
        }


        return TypedActionResult.fail(item);
    }
}
