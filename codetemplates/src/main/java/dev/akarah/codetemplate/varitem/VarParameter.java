package dev.akarah.codetemplate.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VarParameter(
        String name,
        String type,
        boolean plural,
        boolean optional
) implements VarItem {
    public static Codec<VarParameter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(VarParameter::name),
            Codec.STRING.fieldOf("type").forGetter(VarParameter::type),
            Codec.BOOL.fieldOf("plural").forGetter(VarParameter::plural),
            Codec.BOOL.fieldOf("optional").forGetter(VarParameter::optional)
    ).apply(instance, VarParameter::new));

    @Override
    public String getId() {
        return "pn_el";
    }

}
