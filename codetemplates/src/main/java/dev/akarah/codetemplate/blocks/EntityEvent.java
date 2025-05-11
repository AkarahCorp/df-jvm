package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;

public record EntityEvent(String action, Args args) implements ActionBlock {
    public static MapCodec<EntityEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(EntityEvent::action),
            Args.CODEC.fieldOf("args").forGetter(EntityEvent::args)
    ).apply(instance, EntityEvent::new));

    @Override
    public String getBlockId() {
        return "entity_event";
    }

}
