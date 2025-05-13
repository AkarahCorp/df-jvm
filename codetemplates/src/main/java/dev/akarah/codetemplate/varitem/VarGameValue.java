package dev.akarah.codetemplate.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VarGameValue(String type, String target) implements VarItem {
    public static Codec<VarGameValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(VarGameValue::type),
            Codec.STRING.fieldOf("target").forGetter(VarGameValue::target)
    ).apply(instance, VarGameValue::new));

    @Override
    public String getId() {
        return "g_val";
    }

}