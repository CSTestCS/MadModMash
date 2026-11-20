package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.IllagerHero;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Illager Heroes: an outfit skin plus one of thirty-five faces from the Illager Heroes head collection,
 * layered on top - so every hero looks different.
 */
public class IllagerHeroRenderer extends IllagerRenderer<IllagerHero, IllagerHeroRenderer.HeroState> {
    public static final String[] OUTFITS = {"pillager", "vindicator", "evoker", "illusioner", "illagerking", "illagerrook", "technomancer", "cultist"};
    public static final String[] HEADS = {"base", "armorer", "armorer2", "bandit", "bearded", "brodie", "butcher", "cartographer", "chale", "drunk", "evil",
            "fletcher", "grin", "hansley", "helmet", "herobrine", "illager", "masquerader", "mumbomoustache", "moustache", "odd", "old", "pirate", "scarred",
            "scratchedup", "serene", "shepherd", "skull", "sleepy", "spectacles", "steve", "thief", "villager", "witch", "zal"};

    public IllagerHeroRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new IllagerModel<>(ctx.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this));
        IllagerRenderer<IllagerHero, HeroState> self = this;
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack pose, SubmitNodeCollector collector, int light, HeroState state, float yRot, float xRot) {
                if (state.isInvisible || state.head < 0) return;
                Identifier tex = MadModMash.id("textures/block/illager_head_" + HEADS[state.head % HEADS.length] + ".png");
                collector.order(1).submitModel(self.getModel(), state, pose, RenderTypes.entityCutout(tex), light,
                        LivingEntityRenderer.getOverlayCoords(state, 0.0F), state.outlineColor);
            }
        });
    }

    @Override
    public Identifier getTextureLocation(HeroState state) {
        return MadModMash.id("textures/entity/illager_hero/" + OUTFITS[Math.floorMod(state.outfit, OUTFITS.length)] + ".png");
    }

    @Override
    public HeroState createRenderState() {
        return new HeroState();
    }

    @Override
    public void extractRenderState(IllagerHero entity, HeroState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.outfit = entity.getOutfit();
        state.head = entity.getHead();
    }

    public static class HeroState extends IllagerRenderState {
        public int outfit;
        public int head = -1;
    }
}
