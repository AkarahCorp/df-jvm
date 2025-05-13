package dev.akarah.codetemplate.varitem;

import com.mojang.serialization.Codec;

public interface VarItem {
    Codec<VarItem> CODEC = Codec.STRING.dispatch(
            "id",
            VarItem::getId,
            id -> {
                var subCodec = switch(id) {
                    case "comp" -> VarComponent.CODEC;
                    case "var" -> VarVariable.CODEC;
                    case "num" -> VarNumber.CODEC;
                    case "txt" -> VarString.CODEC;
                    case "bl_tag" -> VarBlockTag.CODEC;
                    case "g_val" -> VarGameValue.CODEC;
                    default -> null;
                };
                if(subCodec == null) {
                    throw new RuntimeException("h");
                }
                return subCodec.fieldOf("data");
            }
    );

    public String getId();
}
