package dev.akarah.codetemplate.blocks.types;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public enum SelectionTarget {
    EMPTY(""),
    ALL_PLAYERS("AllPlayers"),
    VICTIM("Victim"),
    SHOOTER("Shooter"),
    DAMAGER("Damager"),
    KILLER("Killer"),
    DEFAULT("Default"),
    SELECTION("Selection"),
    PROJECTILE("Projectile"),
    LAST_ENTITY("LastEntity");

    final String rawName;

    SelectionTarget(String rawName) {
        this.rawName = rawName;
        MapHolder.selectionMap.put(rawName, this);
    }

    public static Codec<SelectionTarget> CODEC = Codec.STRING.xmap(
            string -> MapHolder.selectionMap.get(string),
            attribute -> attribute.rawName
    );

    private static final class MapHolder {
        public static Map<String, SelectionTarget> selectionMap = new HashMap<>();
    }
}
