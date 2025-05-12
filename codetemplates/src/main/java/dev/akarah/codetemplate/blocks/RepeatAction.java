package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;

public record RepeatAction(String action, Args args) implements ActionBlock {
    public static MapCodec<RepeatAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(RepeatAction::action),
            Args.CODEC.fieldOf("args").forGetter(RepeatAction::args)
    ).apply(instance, RepeatAction::new));

    @Override
    public String getBlockId() {
        return "repeat";
    }

}
