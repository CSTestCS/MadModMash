package com.madmodmash.client;

import com.madmodmash.entity.MortisAnimal;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Mortis pig: the classic 64x32 pig shape, rebuilt for the Zombie Plague texture. */
public class MortisPigRenderer extends MashMobRenderer<MortisAnimal> {
    public MortisPigRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, ClientSetup.CLASSIC_PIG, "entity/mortis/pig", "entity/mortis/pig_e", 0.7F, 1F);
    }
}
