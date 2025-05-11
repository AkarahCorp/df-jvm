package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;

public record ControlAction(String action, Args args) implements ActionBlock {
    public static MapCodec<ControlAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(ControlAction::action),
            Args.CODEC.fieldOf("args").forGetter(ControlAction::args)
    ).apply(instance, ControlAction::new));

    @Override
    public String getBlockId() {
        return "control";
    }

}
