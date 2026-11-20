package com.madmodmash.client;

import com.madmodmash.MadModMash;
import com.madmodmash.entity.Boar;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class BoarRenderer extends MashMobRenderer<Boar> {
    private static final Identifier KING = MadModMash.id("textures/entity/boar_king.png");

    public BoarRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, GeneratedModels.BOAR, "entity/boar", null, 0.6F, 1F);
    }

    @Override
    protected Identifier texture(Boar b) {
        return b.isKing() ? KING : super.texture(b);
    }
}
