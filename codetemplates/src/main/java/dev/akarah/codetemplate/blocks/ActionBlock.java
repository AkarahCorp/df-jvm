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
                case "call_func" -> CallFunctionAction.CODEC;
                case "control" -> ControlAction.CODEC;
                case "event" -> PlayerEvent.CODEC;
                case "entity_action" -> EntityAction.CODEC;
                case "entity_event" -> EntityEvent.CODEC;
                case "func" -> FunctionAction.CODEC;
                case "game_action" -> GameAction.CODEC;
                case "player_action" -> PlayerAction.CODEC;
                case "select_obj" -> SelectObjectAction.CODEC;
                case "set_var" -> SetVarAction.CODEC;
                case "repeat" -> RepeatAction.CODEC;
                default -> throw new RuntimeException("could not find" + id);
            }
    );

    @Override
    default String getId() {
        return "block";
    }

    String getBlockId();
}
