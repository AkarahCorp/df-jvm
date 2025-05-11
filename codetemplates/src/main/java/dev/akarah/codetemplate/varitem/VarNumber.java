package dev.akarah.codetemplate.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VarNumber(String name) implements VarItem {
    public static Codec<VarNumber> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(VarNumber::name)
    ).apply(instance, VarNumber::new));

    @Override
    public String getId() {
        return "num";
    }

}
