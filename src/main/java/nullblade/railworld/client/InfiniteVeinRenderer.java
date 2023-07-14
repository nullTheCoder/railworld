package nullblade.railworld.client;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.math.RotationAxis;
import nullblade.railworld.InfiniteVein;
import org.joml.Math;

public class InfiniteVeinRenderer implements BlockEntityRenderer<InfiniteVein.InfiniteVeinEntity> {

    private float state = 0;

    private float timer = 0;

    private long lastTime = 0;

    @Override
    public void render(InfiniteVein.InfiniteVeinEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.blocks == null || entity.blocks.size() == 0 || entity.getWorld() == null)
            return;
        if ((int)state >= entity.blocks.size()) {
            state = 0;
        }
        var got = entity.blocks.get((int)state);
        if (got == null)
            return;

        Block block = got.getRight();
        Item item = block.asItem();

        matrices.push();

        matrices.translate(0.5, 0.3, 0.5);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(timer));
        matrices.scale(1.2f + 0.2f * Math.cos(timer), 1.2f + 0.1f * Math.cos(timer), 1.2f + 0.2f * Math.cos(timer));

        MinecraftClient.getInstance().getItemRenderer().renderItem(item.getDefaultStack(),
                ModelTransformationMode.GROUND, light, overlay, matrices, vertexConsumers, entity.getWorld(), 0);


        matrices.pop();
        state += (entity.getWorld().getTime() - lastTime) * 0.02;
        timer += (entity.getWorld().getTime() - lastTime) * 0.1;
        lastTime = entity.getWorld().getTime();
    }
}
