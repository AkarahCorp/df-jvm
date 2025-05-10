package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.TemplateBlock;

public record PlayerAction(String action, Args args) implements ActionBlock {
    public static MapCodec<PlayerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(PlayerAction::action),
            Args.CODEC.fieldOf("args").forGetter(PlayerAction::args)
    ).apply(instance, PlayerAction::new));

    @Override
    public String getBlockId() {
        return "player_action";
    }

}
