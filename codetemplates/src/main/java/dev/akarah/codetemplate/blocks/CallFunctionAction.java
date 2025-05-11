package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;

public record CallFunctionAction(String data, Args args) implements ActionBlock {
    public static MapCodec<CallFunctionAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("data").forGetter(CallFunctionAction::data),
            Args.CODEC.fieldOf("args").forGetter(CallFunctionAction::args)
    ).apply(instance, CallFunctionAction::new));

    @Override
    public String getBlockId() {
        return "call_func";
    }

}
