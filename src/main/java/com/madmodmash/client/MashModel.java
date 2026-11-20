package com.madmodmash.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * A model built from geometry recovered out of the original mods' compiled model classes.
 * The originals each hand-animated their parts; here parts are animated by what they are called:
 * heads look, legs and arms swing, wings flap, tails sway and "rotato"/core parts spin.
 */
public class MashModel extends EntityModel<MashRenderState> {
    private record Animated(ModelPart part, int kind, int side) {}
    private static final int HEAD = 0, LEG = 1, ARM = 2, WING = 3, TAIL = 4, SPIN = 5, BOB = 6;
    private final List<Animated> parts = new ArrayList<>();

    public MashModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        collect(root, "", 0);
    }

    private void collect(ModelPart part, String name, int depth) {
        String n = name.toLowerCase(Locale.ROOT);
        int side = n.contains("left") || n.endsWith("l") || n.contains("_l") ? 1 : -1;
        if (depth == 1 || depth == 2) {
            if (n.contains("head") && !n.contains("headless")) parts.add(new Animated(part, HEAD, 0));
            else if (n.contains("leg") || n.contains("foot") || n.contains("feet")) parts.add(new Animated(part, LEG, legPhase(n)));
            else if (n.contains("arm") || n.contains("hand")) parts.add(new Animated(part, ARM, legPhase(n)));
            else if (n.contains("wing")) parts.add(new Animated(part, WING, side));
            else if (n.contains("tail")) parts.add(new Animated(part, TAIL, 0));
            else if (n.contains("rotato") || n.contains("spin") || n.contains("core") || n.contains("ring") || n.contains("tornado")) parts.add(new Animated(part, SPIN, depth));
        }
        for (var e : childrenOf(part)) collect(e.part, e.name, depth + 1);
    }

    private static int legPhase(String n) {
        boolean left = n.contains("left") || n.contains("_l") || n.endsWith("l") || n.contains("2");
        boolean front = n.contains("front") || n.contains("fore");
        int p = left ? 1 : 0;
        if (front) p ^= 1;
        return p;
    }

    private record Named(String name, ModelPart part) {}

    private static List<Named> childrenOf(ModelPart part) {
        List<Named> out = new ArrayList<>();
        for (var e : PartAccess.children(part).entrySet()) out.add(new Named(e.getKey(), e.getValue()));
        return out;
    }

    @Override
    public void setupAnim(MashRenderState state) {
        super.setupAnim(state);
        float pos = state.walkAnimationPos, speed = Math.min(1F, state.walkAnimationSpeed), age = state.ageInTicks;
        for (Animated a : parts) {
            switch (a.kind) {
                case HEAD -> { a.part.yRot += state.yRot * Mth.DEG_TO_RAD; a.part.xRot += state.xRot * Mth.DEG_TO_RAD; }
                case LEG -> a.part.xRot += Mth.cos(pos * 0.6662F + (a.side == 1 ? Mth.PI : 0)) * 1.2F * speed;
                case ARM -> a.part.xRot += Mth.cos(pos * 0.6662F + (a.side == 1 ? 0 : Mth.PI)) * 0.9F * speed - state.attackAnim * 1.2F;
                case WING -> a.part.zRot += Mth.cos(age * 0.5F) * 0.5F * a.side;
                case TAIL -> a.part.yRot += Mth.cos(age * 0.2F) * 0.3F;
                case SPIN -> a.part.yRot += age * 0.08F * (a.side == 2 ? -1.5F : 1F);
                default -> {}
            }
        }
    }
}
