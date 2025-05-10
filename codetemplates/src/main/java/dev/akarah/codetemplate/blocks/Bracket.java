package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.template.TemplateBlock;

public record Bracket(Direction direction, Type type) implements TemplateBlock {
    public static MapCodec<Bracket> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Direction.CODEC.fieldOf("direction").forGetter(Bracket::direction),
            Type.CODEC.fieldOf("type").forGetter(Bracket::type)
    ).apply(instance, Bracket::new));

    @Override
    public String getId() {
        return "bracket";
    }

    public enum Direction {
        OPEN,
        CLOSE;

        public static final Codec<Direction> CODEC = Codec.STRING
                .xmap(String::toUpperCase, String::toLowerCase)
                .xmap(Direction::valueOf, Direction::name);
    }

    public enum Type {
        NORMAL,
        REPEAT;

        public static final Codec<Type> CODEC = Codec.STRING
                .xmap(String::toUpperCase, String::toLowerCase)
                .xmap(Type::valueOf, Type::name);
    }
}
