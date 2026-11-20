package com.madmodmash.client;

import com.madmodmash.MadModMash;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MadModMash.MODID, dist = Dist.CLIENT)
public class ClientSetup {
    public static final ModelLayerLocation CLASSIC_PIG = new ModelLayerLocation(MadModMash.id("classic_pig"), "main");

    public ClientSetup(IEventBus modBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modBus.addListener(ClientSetup::layers);
        modBus.addListener(GeneratedRenderers::register);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        GeneratedModels.register(event);
        event.registerLayerDefinition(CLASSIC_PIG, ClientSetup::classicPig);
    }

    /** The pre-1.21 pig layout that the Zombie Plague mortis pig texture was painted for. */
    private static LayerDefinition classicPig() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation g = CubeDeformation.NONE;
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4F, -4F, -8F, 8F, 8F, 8F, g).texOffs(16, 16).addBox(-2F, 0F, -9F, 4F, 3F, 1F, g),
                PartPose.offset(0F, 12F, -6F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(28, 8).addBox(-5F, -10F, -7F, 10F, 16F, 8F, g),
                PartPose.offsetAndRotation(0F, 11F, 2F, (float) (Math.PI / 2), 0F, 0F));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(0, 16).addBox(-2F, 0F, -2F, 4F, 6F, 4F, g);
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(-3F, 18F, 7F));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(3F, 18F, 7F));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(-3F, 18F, -5F));
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(3F, 18F, -5F));
        return LayerDefinition.create(mesh, 64, 32);
    }
}
