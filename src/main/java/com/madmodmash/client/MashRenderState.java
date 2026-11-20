package com.madmodmash.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/** Render state shared by all generic MadModMash mobs. */
public class MashRenderState extends LivingEntityRenderState {
    public Identifier texture;
    public Identifier glow;
    public boolean aggressive;
    public int variant;
    public float extraScale = 1F;
    public float attackAnim;
}
