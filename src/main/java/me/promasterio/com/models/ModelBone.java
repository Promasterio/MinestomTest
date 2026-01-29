package me.promasterio.com.models;

import net.minestom.server.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ModelBone {
    private final String tag; // e.g., "head", "upper-arm-left"
    private final ItemStack item;
    private final Vector3f translation;
    private final Vector3f scale;
    private final Quaternionf rotation;

    public ModelBone(String tag, ItemStack item, Vector3f translation, Vector3f scale, Quaternionf rotation) {
        this.tag = tag;
        this.item = item;
        this.translation = translation;
        this.scale = scale;
        this.rotation = rotation;
    }

    // Getters
    public String getTag() { return tag; }
    public ItemStack getItem() { return item; }
    public Vector3f getTranslation() { return translation; }
    public Vector3f getScale() { return scale; }
    public Quaternionf getRotation() { return rotation; }
}