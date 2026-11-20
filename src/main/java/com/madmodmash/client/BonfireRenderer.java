package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.Bonfire;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class BonfireRenderer extends MashMobRenderer<Bonfire> {
    public BonfireRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, GeneratedModels.BONFIRE, "entity/bonfire/tier1", null, 0.5F, 1F);
    }

    @Override
    protected Identifier texture(Bonfire b) {
        return MadModMash.id("textures/entity/bonfire/tier" + Math.max(1, Math.min(3, b.getTier())) + ".png");
    }

    @Override
    protected int getBlockLightLevel(Bonfire entity, net.minecraft.core.BlockPos pos) {
        return 15;
    }
}
