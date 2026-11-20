package com.madmodmash.client;

import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

/** Reads a ModelPart's named children (made public by our access transformer). */
final class PartAccess {
    static Map<String, ModelPart> children(ModelPart part) {
        return part.children;
    }

    private PartAccess() {}
}
