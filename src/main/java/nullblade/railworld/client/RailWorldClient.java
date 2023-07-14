package nullblade.railworld.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import nullblade.railworld.InfiniteVein;

public class RailWorldClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(InfiniteVein.entity, ctx -> new InfiniteVeinRenderer());
    }
}
