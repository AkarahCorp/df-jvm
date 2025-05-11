package dev.akarah.codetemplate.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VarString(String name) implements VarItem {
    public static Codec<VarString> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(VarString::name)
    ).apply(instance, VarString::new));

    @Override
    public String getId() {
        return "txt";
    }

}
