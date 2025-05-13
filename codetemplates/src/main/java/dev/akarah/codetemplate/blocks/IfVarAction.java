package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;

public record IfVarAction(String action, Args args) implements ActionBlock {
    public static MapCodec<IfVarAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(IfVarAction::action),
            Args.CODEC.fieldOf("args").forGetter(IfVarAction::args)
    ).apply(instance, IfVarAction::new));

    @Override
    public String getBlockId() {
        return "if_var";
    }

}
