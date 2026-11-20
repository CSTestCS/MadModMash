package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractCubeMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.cubemob.Slime;

/** Flub's material slimes: vanilla slime shape, their own skins inside and out. */
public class MaterialSlimeRenderer extends AbstractCubeMobRenderer<Slime, SlimeRenderState, SlimeModel> {
    private final Identifier texture;

    public MaterialSlimeRenderer(EntityRendererProvider.Context ctx, String name) {
        super(ctx, new SlimeModel(ctx.bakeLayer(ModelLayers.SLIME)));
        this.texture = MadModMash.id("textures/entity/slime/" + name + ".png");
        SlimeModel outer = new SlimeModel(ctx.bakeLayer(ModelLayers.SLIME_OUTER));
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, SlimeRenderState state, float yRot, float xRot) {
                if (state.isInvisible) return;
                collector.order(1).submitModel(outer, state, pose, RenderTypes.entityTranslucent(texture), light,
                        LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
            }
        });
    }

    @Override
    protected void scale(SlimeRenderState state, PoseStack poseStack) {
        this.downscaleSlightly(poseStack);
        super.scale(state, poseStack);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return texture;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }
}
