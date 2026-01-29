package me.promasterio.com.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ComponentUtil {
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public static Component nonItalic(String input) {
        return MINI.deserialize("<!italic>" + input);
    }

    public static Component deserialize(String input) {
        return MINI.deserialize(input);
    }

    public static Component merge(Component main, Component... append) {
        return main.append(append);
    }

}
