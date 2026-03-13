package me.davidjawhar.oneblock.level;

import org.bukkit.Material;

public class GenerationOutcome {
    public enum Type {
        NORMAL,
        CHEST_BLOCK,
        TREE_BLOCK,
        ORE_CLUSTER_BLOCK,
        TNT_BLOCK
    }

    private final Type type;
    private final Material displayMaterial;

    public GenerationOutcome(Type type, Material displayMaterial) {
        this.type = type;
        this.displayMaterial = displayMaterial;
    }

    public Type getType() {
        return type;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public boolean isSolidPlacement() {
        return displayMaterial.isSolid();
    }
}
