package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;

public record FunctionAction(String data, Args args) implements ActionBlock {
    public static MapCodec<FunctionAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("data").forGetter(FunctionAction::data),
            Args.CODEC.fieldOf("args").forGetter(FunctionAction::args)
    ).apply(instance, FunctionAction::new));

    @Override
    public String getBlockId() {
        return "function";
    }

}
