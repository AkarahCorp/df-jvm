package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;

public record SetVarAction(String action, Args args) implements ActionBlock {
    public static MapCodec<SetVarAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(SetVarAction::action),
            Args.CODEC.fieldOf("args").forGetter(SetVarAction::args)
    ).apply(instance, SetVarAction::new));

    @Override
    public String getBlockId() {
        return "set_var";
    }

}
