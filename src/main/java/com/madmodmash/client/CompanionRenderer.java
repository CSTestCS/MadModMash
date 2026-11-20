package com.madmodmash.client;

import com.madmodmash.entity.Companion;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** CSA companions wear one of the default adventurer skins. */
public class CompanionRenderer extends SimpleHumanoidRenderer<Companion> {
    static final String[] SKINS = {"steve", "alex", "ari", "efe", "kai", "makena", "noor", "sunny", "zuri"};
    private final Identifier[] skins = new Identifier[SKINS.length];

    public CompanionRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, "minecraft:textures/entity/player/wide/steve.png", false);
        for (int i = 0; i < SKINS.length; i++) skins[i] = Identifier.withDefaultNamespace("textures/entity/player/wide/" + SKINS[i] + ".png");
    }

    private int skinOf(HumanoidRenderState s) {
        return s instanceof CompanionState cs ? cs.skin : 0;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new CompanionState();
    }

    @Override
    public void extractRenderState(Companion entity, HumanoidRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof CompanionState cs) cs.skin = Math.floorMod(entity.getSkin(), SKINS.length);
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return skins[skinOf(state)];
    }

    public static class CompanionState extends HumanoidRenderState {
        public int skin;
    }
}
