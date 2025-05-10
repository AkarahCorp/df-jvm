package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface ActionBlock extends TemplateBlock {
    MapCodec<ActionBlock> BLOCK_CODEC = Codec.STRING.dispatchMap(
            "block",
            ActionBlock::getBlockId,
            id -> switch(id) {
                case "else" -> Else.CODEC;
                default -> null;
            }
    );

    @Override
    default String getId() {
        return "block";
    }

    String getBlockId();
}
