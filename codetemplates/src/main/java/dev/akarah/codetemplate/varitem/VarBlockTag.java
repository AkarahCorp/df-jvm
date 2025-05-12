package dev.akarah.codetemplate.varitem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record VarBlockTag(String option, String tag, String action, String block) implements VarItem {
    public static Codec<VarBlockTag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("option").forGetter(VarBlockTag::option),
            Codec.STRING.fieldOf("tag").forGetter(VarBlockTag::tag),
            Codec.STRING.fieldOf("action").forGetter(VarBlockTag::action),
            Codec.STRING.fieldOf("block").forGetter(VarBlockTag::block)
    ).apply(instance, VarBlockTag::new));

    @Override
    public String getId() {
        return "bl_tag";
    }

}
