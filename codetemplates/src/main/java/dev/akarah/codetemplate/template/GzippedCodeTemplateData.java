package dev.akarah.codetemplate.template;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

public record GzippedCodeTemplateData(
        String author,
        String name,
        String version,
        String code
) {
    public static Codec<GzippedCodeTemplateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("author").forGetter(GzippedCodeTemplateData::author),
            Codec.STRING.fieldOf("name").forGetter(GzippedCodeTemplateData::name),
            Codec.STRING.fieldOf("version").forGetter(GzippedCodeTemplateData::version),
            Codec.STRING.fieldOf("code").forGetter(GzippedCodeTemplateData::code)
    ).apply(instance, GzippedCodeTemplateData::new));

    public static GzippedCodeTemplateData from(CodeTemplateData data) {
        var b64 = Base64.getEncoder();

        var byteStream = new ByteArrayOutputStream();
        var encoded = CodeTemplate.CODEC.encodeStart(JsonOps.INSTANCE, data.code()).getOrThrow();

        try(var stream = new GZIPOutputStream(byteStream)) {
            stream.write(encoded.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new GzippedCodeTemplateData(
                data.author(),
                data.name(),
                data.version(),
                b64.encodeToString(byteStream.toByteArray())
        );
    }
}
