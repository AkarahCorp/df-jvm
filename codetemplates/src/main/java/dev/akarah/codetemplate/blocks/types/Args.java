package dev.akarah.codetemplate.blocks.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.varitem.VarItem;

import java.util.List;

public record Args(List<Args.Slot> items) {
    public static Codec<Args> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Args.Slot.CODEC.listOf().fieldOf("items").forGetter(Args::items)
    ).apply(instance, Args::new));

    public record Slot(VarItem item, int slot) {
        public static Codec<Slot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                VarItem.CODEC.fieldOf("item").forGetter(Slot::item),
                Codec.INT.fieldOf("slot").forGetter(Slot::slot)
        ).apply(instance, Slot::new));
    }
}
