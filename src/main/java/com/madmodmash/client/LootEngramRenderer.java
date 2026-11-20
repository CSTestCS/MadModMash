package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.LootEngram;
import com.madmodmash.system.Engrams;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/** WildLoot loot engrams: spinning, bobbing, glowing loot. */
public class LootEngramRenderer extends EntityRenderer<LootEngram, LootEngramRenderer.State> {
    private final EntityModel<State> model;

    public LootEngramRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        ModelPart root = ctx.bakeLayer(GeneratedModels.LOOT_ENGRAM);
        this.model = new EntityModel<>(root, RenderTypes::entityCutout) {};
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.0F, 0.4F + (float) Math.sin(state.ageInTicks * 0.1F) * 0.12F, 0.0F);
        pose.rotate(Axis.YP.rotationDegrees(state.ageInTicks * 3F));
        pose.scale(-1.0F, -1.0F, 1.0F);
        pose.translate(0.0F, -1.501F, 0.0F);
        Identifier tex = MadModMash.id("textures/entity/engram/" + Engrams.TIERS[Math.max(0, Math.min(Engrams.TIERS.length - 1, state.tier))] + ".png");
        collector.submitModel(model, state, pose, RenderTypes.entityTranslucentEmissive(tex), 0xF000F0, OverlayTexture.NO_OVERLAY, state.outlineColor);
        pose.popPose();
        super.submit(state, pose, collector, camera);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(LootEngram entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tier = entity.getTier();
    }

    public static class State extends EntityRenderState {
        public int tier;
    }
}
