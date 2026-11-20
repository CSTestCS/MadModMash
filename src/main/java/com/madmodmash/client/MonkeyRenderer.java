package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.Monkey;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

/** MonkeyBusiness monkeys: the model and skin follow the gear the monkey has been given. */
public class MonkeyRenderer extends MashMobRenderer<Monkey> {
    private final MashModel[] models;
    private static final String[] TEX = {"monkey", "monkey_sword", "monkey_pickaxe", "monkey_crossbow", "monkey_chest", "monkey_boots"};

    public MonkeyRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, GeneratedModels.MONKEY, "entity/monkey/monkey", null, 0.4F, 1F);
        models = new MashModel[]{model, new MashModel(ctx.bakeLayer(GeneratedModels.MONKEY_SWORD)), new MashModel(ctx.bakeLayer(GeneratedModels.MONKEY_PICKAXE)),
                new MashModel(ctx.bakeLayer(GeneratedModels.MONKEY_CROSSBOW)), new MashModel(ctx.bakeLayer(GeneratedModels.MONKEY_CHEST)), model};
    }

    @Override
    protected Identifier texture(Monkey m) {
        return MadModMash.id("textures/entity/monkey/" + TEX[Math.max(0, Math.min(5, m.getGear()))] + ".png");
    }

    @Override
    public void extractRenderState(Monkey entity, MashRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.variant = entity.getGear();
    }

    @Override
    public void submit(MashRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = models[Math.max(0, Math.min(5, state.variant))];
        super.submit(state, pose, collector, camera);
    }
}
