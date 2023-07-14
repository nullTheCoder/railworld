package nullblade.railworld;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

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
//        if (world.isClient || !(world instanceof ServerWorld serWorld)) {
//            return;
//        }
//
//        world.getRegistryManager().get(RegistryKeys.STRUCTURE).stream().forEach((str) -> {
//            System.out.println(str.toString());
//        });
        //serWorld.getChunkManager().getChunkGenerator().locateStructure(serWorld, );


    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack item = user.getStackInHand(hand);

        if (world.isClient || !(world instanceof ServerWorld serWorld)) {
            return TypedActionResult.success(item);
        }

        world.getRegistryManager().get(RegistryKeys.STRUCTURE).stream().forEach((str) -> {
            System.out.println(str.toString());
        });


        return TypedActionResult.fail(item);
    }
}
