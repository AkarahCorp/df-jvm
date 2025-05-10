package dev.akarah.codetemplate.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VarComponent(String name) implements VarItem {
    public static Codec<VarComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(VarComponent::getId)
    ).apply(instance, VarComponent::new));

    @Override
    public String getId() {
        return "comp";
    }

}
