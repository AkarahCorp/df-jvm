package dev.akarah.codetemplate.varitem;

import com.mojang.serialization.Codec;

public interface VarItem {
    Codec<VarItem> CODEC = Codec.STRING.dispatch(
            "id",
            VarItem::getId,
            id -> {
                var subCodec = switch(id) {
                    case "comp" -> VarComponent.CODEC;
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
