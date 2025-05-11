package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.blocks.types.SelectionTarget;

import java.util.Optional;

public record GameAction(String action, Args args) implements ActionBlock {
    public static MapCodec<GameAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(GameAction::action),
            Args.CODEC.fieldOf("args").forGetter(GameAction::args)
    ).apply(instance, GameAction::new));

    @Override
    public String getBlockId() {
        return "game_action";
    }

}
