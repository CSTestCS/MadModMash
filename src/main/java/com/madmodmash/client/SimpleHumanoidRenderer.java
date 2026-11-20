package com.madmodmash.client;

import com.madmodmash.MadModMash;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** Player-shaped mobs (Herobrine, the Horseless Headman, companions). */
public class SimpleHumanoidRenderer<T extends Mob> extends HumanoidMobRenderer<T, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
    protected final Identifier texture;

    public SimpleHumanoidRenderer(EntityRendererProvider.Context ctx, String texture, boolean slim) {
        super(ctx, new HumanoidModel<>(ctx.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER)), 0.5F);
        this.texture = texture.contains(":") ? Identifier.parse(texture) : MadModMash.id("textures/" + texture + ".png");
    }

    @Override
    public Identifier getTextureLocation(HumanoidRenderState state) {
        return texture;
    }

    @Override
    public HumanoidRenderState createRenderState() {
        return new HumanoidRenderState();
    }
}
