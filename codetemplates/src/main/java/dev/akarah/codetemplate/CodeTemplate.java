package dev.akarah.codetemplate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.akarah.codetemplate.blocks.TemplateBlock;

import java.util.List;

public record CodeTemplate(List<TemplateBlock> blocks) {

    public static Codec<CodeTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TemplateBlock.ROOT_CODEC.codec().listOf().fieldOf("blocks").forGetter(CodeTemplate::blocks)
    ).apply(instance, CodeTemplate::new));
}
