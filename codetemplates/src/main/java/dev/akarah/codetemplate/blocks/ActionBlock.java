package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.akarah.codetemplate.template.TemplateBlock;

public interface ActionBlock extends TemplateBlock {
    MapCodec<ActionBlock> BLOCK_CODEC = Codec.STRING.dispatchMap(
            "block",
            ActionBlock::getBlockId,
            id -> switch(id) {
                case "else" -> Else.CODEC;
                case "player_action" -> PlayerAction.CODEC;
                case "event" -> PlayerEvent.CODEC;
                default -> null;
            }
    );

    @Override
    default String getId() {
        return "block";
    }

    String getBlockId();
}
