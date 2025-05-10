package dev.akarah.codetemplate.template;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.codetemplate.blocks.ActionBlock;
import dev.akarah.codetemplate.blocks.Bracket;

public interface TemplateBlock {
    MapCodec<TemplateBlock> ROOT_CODEC = Codec.STRING.dispatchMap(
            "id",
            TemplateBlock::getId,
            id -> switch(id) {
                case "bracket" -> Bracket.CODEC;
                case "block" -> ActionBlock.BLOCK_CODEC;
                default -> null;
            }
    );

    public String getId();
}
