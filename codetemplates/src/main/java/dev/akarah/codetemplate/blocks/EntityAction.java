package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.blocks.types.SelectionTarget;

import java.util.Optional;

public record EntityAction(String action, Args args, Optional<SelectionTarget> target) implements ActionBlock {
    public static MapCodec<EntityAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(EntityAction::action),
            Args.CODEC.fieldOf("args").forGetter(EntityAction::args),
            SelectionTarget.CODEC.optionalFieldOf("target").forGetter(EntityAction::target)
    ).apply(instance, EntityAction::new));

    @Override
    public String getBlockId() {
        return "entity_action";
    }

}
