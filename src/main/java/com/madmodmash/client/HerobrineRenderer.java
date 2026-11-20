package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.Herobrine;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class HerobrineRenderer extends SimpleHumanoidRenderer<Herobrine> {
    public HerobrineRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, "entity/herobrine", false);
        RenderType eyes = RenderTypes.eyes(MadModMash.id("textures/entity/herobrine_eyes.png"));
        this.addLayer(new EyesLayer<>(this) {
            @Override public RenderType renderType() { return eyes; }
        });
    }
}
