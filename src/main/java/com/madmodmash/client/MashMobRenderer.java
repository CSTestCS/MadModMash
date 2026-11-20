package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** Renders a mob with recovered geometry, a texture and an optional emissive ("_e"/glow) layer. */
public class MashMobRenderer<T extends Mob> extends MobRenderer<T, MashRenderState, MashModel> {
    private final Identifier texture;
    private final float scale;

    public MashMobRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer, String texture, String glow, float shadow, float scale) {
        super(ctx, new MashModel(ctx.bakeLayer(layer)), shadow * scale);
        this.texture = MadModMash.id("textures/" + texture + ".png");
        this.scale = scale;
        if (glow != null) {
            RenderType eyes = RenderTypes.eyes(MadModMash.id("textures/" + glow + ".png"));
            this.addLayer(new EyesLayer<>(this) {
                @Override public RenderType renderType() { return eyes; }
            });
        }
    }

    protected Identifier texture(T entity) {
        return texture;
    }

    @Override
    public Identifier getTextureLocation(MashRenderState state) {
        return state.texture != null ? state.texture : texture;
    }

    @Override
    public MashRenderState createRenderState() {
        return new MashRenderState();
    }

    @Override
    public void extractRenderState(T entity, MashRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.texture = texture(entity);
        state.aggressive = entity.isAggressive();
        state.attackAnim = 0F;
    }

    @Override
    protected void scale(MashRenderState state, PoseStack poseStack) {
        if (scale != 1F) poseStack.scale(scale, scale, scale);
    }
}
