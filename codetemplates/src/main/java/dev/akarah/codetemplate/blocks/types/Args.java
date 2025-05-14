package dev.akarah.codetemplate.blocks.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.varitem.VarItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class Args {
    Map<Integer, VarItem> varItemMap = new HashMap<>();

    public static Codec<Args> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Args.Slot.CODEC.listOf().fieldOf("items").forGetter(Args::slots)
    ).apply(instance, Args::ofSlots));

    public record Slot(VarItem item, int slot) {
        public static Codec<Slot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                VarItem.CODEC.fieldOf("item").forGetter(Slot::item),
                Codec.INT.fieldOf("slot").forGetter(Slot::slot)
        ).apply(instance, Slot::new));
    }

    public List<Slot> slots() {
        return this.varItemMap
                .entrySet()
                .stream()
                .map(entry -> new Slot(entry.getValue(), entry.getKey()))
                .toList();
    }

    public static Args ofSlots(List<Slot> slots) {
        var args = new Args();
        for(var slot : slots) {
            args.varItemMap.put(slot.slot(), slot.item());
        }
        return args;
    }

    public static Args of(Slot... slots) {
        var args = new Args();
        for(var slot : slots) {
            args.varItemMap.put(slot.slot(), slot.item());
        }
        return args;
    }

    public static Args empty() {
        return new Args();
    }

    public static Args ofVarItems(List<VarItem> parameters) {
        return Args.ofSlots(
                IntStream.range(0, parameters.size())
                    .mapToObj(idx -> new Args.Slot(parameters.get(idx), idx))
                    .toList()
        );
    }

    public static Args of(VarItem... parameters) {
        return Args.ofSlots(
                IntStream.range(0, parameters.length)
                        .mapToObj(idx -> new Args.Slot(parameters[idx], idx))
                        .toList()
        );
    }

    public Args set(VarItem varItem, int slot) {
        this.varItemMap.put(slot, varItem);
        return this;
    }

    public Args insert(VarItem varItem, int slot) {
        var keySet = this.varItemMap.keySet().stream().sorted().toList().reversed();
        for(var key : keySet) {
            if(key >= slot) {
                var vi = this.varItemMap.remove(key);
                this.varItemMap.put(key + 1, vi);
            }
        }
        this.varItemMap.put(slot, varItem);
        return this;
    }
}
