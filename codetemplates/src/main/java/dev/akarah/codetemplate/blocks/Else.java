package dev.akarah.codetemplate.blocks;

import com.mojang.serialization.MapCodec;

public record Else() implements ActionBlock {
    public static MapCodec<Else> CODEC = MapCodec.unit(new Else());

    @Override
    public String getBlockId() {
        return "else";
    }
}
