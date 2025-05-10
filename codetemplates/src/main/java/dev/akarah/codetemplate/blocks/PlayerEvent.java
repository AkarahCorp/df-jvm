package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.TemplateBlock;

public record PlayerEvent(String action, Args args) implements ActionBlock {
    public static MapCodec<PlayerEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("action").forGetter(PlayerEvent::action),
            Args.CODEC.fieldOf("args").forGetter(PlayerEvent::args)
    ).apply(instance, PlayerEvent::new));

    @Override
    public String getBlockId() {
        return "event";
    }

}
