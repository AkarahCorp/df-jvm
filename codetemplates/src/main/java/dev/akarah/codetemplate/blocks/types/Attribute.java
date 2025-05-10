package dev.akarah.codetemplate.blocks.types;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public enum Attribute {
    NONE(""),
    LS_CANCEL("LS-CANCEL"),
    NOT("NOT");

    final String rawName;

    Attribute(String rawName) {
        this.rawName = rawName;
        MapHolder.selectionMap.put(rawName, this);
    }

    public static Codec<Attribute> CODEC = Codec.STRING.xmap(
            string -> MapHolder.selectionMap.get(string),
            attribute -> attribute.rawName
    );

    private static final class MapHolder {
        public static Map<String, Attribute> selectionMap = new HashMap<>();
    }
}
