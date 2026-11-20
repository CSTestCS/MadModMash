package com.madmodmash.client;

import com.madmodmash.MadModMash;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.state.PiglinRenderState;
import net.minecraft.resources.Identifier;

public class OverworldPiglinRenderer extends PiglinRenderer {
    private static final Identifier TEX = MadModMash.id("textures/entity/overworld_piglin.png");

    public OverworldPiglinRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, ModelLayers.PIGLIN, ModelLayers.PIGLIN_BABY, ModelLayers.PIGLIN_ARMOR, ModelLayers.PIGLIN_BABY_ARMOR);
    }

    @Override
    public Identifier getTextureLocation(PiglinRenderState state) {
        return TEX;
    }
}
