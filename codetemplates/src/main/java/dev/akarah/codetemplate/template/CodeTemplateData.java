package dev.akarah.codetemplate.template;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CodeTemplateData(
        String author,
        String name,
        String version,
        CodeTemplate code
) {
    public static Codec<CodeTemplateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("author").forGetter(CodeTemplateData::author),
            Codec.STRING.fieldOf("name").forGetter(CodeTemplateData::name),
            Codec.STRING.fieldOf("version").forGetter(CodeTemplateData::version),
            CodeTemplate.CODEC.fieldOf("code").forGetter(CodeTemplateData::code)
    ).apply(instance, CodeTemplateData::new));

    public GzippedCodeTemplateData gzip() {
        return GzippedCodeTemplateData.from(this);
    }

    public String toFlatJson() {
        return CodeTemplateData.CODEC.encodeStart(JsonOps.INSTANCE, this).getOrThrow().toString();
    }

    public String toGzipJson() {
        return GzippedCodeTemplateData.CODEC.encodeStart(JsonOps.INSTANCE, this.gzip()).getOrThrow().toString();
    }
}
