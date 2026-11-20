package com.madmodmash.client;

import com.madmodmash.MadModMash;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** Villager-shaped NPCs with their own full skins (RPG travellers, mourner, statue carver). */
public class TravelerRenderer<T extends Mob> extends MobRenderer<T, VillagerRenderState, VillagerModel> {
    private final Identifier texture;

    public TravelerRenderer(EntityRendererProvider.Context ctx, String texture) {
        super(ctx, new VillagerModel(ctx.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
        this.texture = texture.contains(":") ? Identifier.parse(texture) : MadModMash.id("textures/" + texture + ".png");
    }

    protected Identifier texture(VillagerRenderState state) {
        return texture;
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return texture(state);
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }
}
