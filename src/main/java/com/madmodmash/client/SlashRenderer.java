package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.SlashProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** Dark Adventures claymore slash. */
public class SlashRenderer extends EntityRenderer<SlashProjectile, SlashRenderer.State> {
    private static final Identifier TEX = MadModMash.id("textures/entity/slash.png");
    private final EntityModel<State> model;

    public SlashRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new EntityModel<>(ctx.bakeLayer(GeneratedModels.SLASH), RenderTypes::entityTranslucent) {};
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.rotate(Axis.YP.rotationDegrees(-state.yRot));
        pose.scale(-1.5F, -1.5F, 1.5F);
        pose.translate(0.0F, -1.501F, 0.0F);
        collector.submitModel(model, state, pose, RenderTypes.entityTranslucentEmissive(TEX), 0xF000F0, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(SlashProjectile entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
    }

    public static class State extends EntityRenderState {
        public float yRot;
    }
}
