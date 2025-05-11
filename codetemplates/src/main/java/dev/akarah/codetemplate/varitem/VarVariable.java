package dev.akarah.codetemplate.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Attribute;

import java.util.HashMap;
import java.util.Map;

public record VarVariable(String name, Scope scope) implements VarItem {
    public static Codec<VarVariable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(VarVariable::name),
            Scope.CODEC.fieldOf("scope").forGetter(VarVariable::scope)
    ).apply(instance, VarVariable::new));

    public enum Scope {
        SAVED("saved"),
        GAME("unsaved"),
        LOCAL("local"),
        LINE("line");

        final String rawName;

        Scope(String rawName) {
            this.rawName = rawName;
            MapHolder.selectionMap.put(rawName, this);
        }

        public static Codec<Scope> CODEC = Codec.STRING.xmap(
                string -> MapHolder.selectionMap.get(string),
                scope -> scope.rawName
        );

        private static final class MapHolder {
            public static Map<String, Scope> selectionMap = new HashMap<>();
        }
    }


    @Override
    public String getId() {
        return "var";
    }

}
