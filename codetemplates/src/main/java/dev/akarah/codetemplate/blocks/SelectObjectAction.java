package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;

public record SelectObjectAction(String action, Args args) implements ActionBlock {
    public static MapCodec<SelectObjectAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(SelectObjectAction::action),
            Args.CODEC.fieldOf("args").forGetter(SelectObjectAction::args)
    ).apply(instance, SelectObjectAction::new));

    @Override
    public String getBlockId() {
        return "select_obj";
    }

}
