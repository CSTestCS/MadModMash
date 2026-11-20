package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.Proxy;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

/** The Dark Adventures "Proxy": a spy wearing a villager's face until it is unmasked. */
public class ProxyRenderer extends TravelerRenderer<Proxy> {
    private static final Identifier DISGUISE = Identifier.withDefaultNamespace("textures/entity/villager/villager.png");
    private static final Identifier REVEALED = MadModMash.id("textures/entity/illager_hero/pillager.png");

    public ProxyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, "entity/illager_hero/pillager");
    }

    @Override
    public void extractRenderState(Proxy entity, VillagerRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isUnhappy = entity.isRevealed();
    }

    @Override
    protected Identifier texture(VillagerRenderState state) {
        return state.isUnhappy ? REVEALED : DISGUISE;
    }
}
