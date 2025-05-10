package dev.akarah.codetemplate;

import dev.akarah.codetemplate.blocks.Bracket;
import dev.akarah.codetemplate.blocks.Else;
import dev.akarah.codetemplate.template.CodeTemplate;
import dev.akarah.codetemplate.template.CodeTemplateData;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        var template = new CodeTemplateData(
                "Endistic",
                "template",
                "1",
                new CodeTemplate(List.of(
                    new Else(),
                    new Bracket(
                        Bracket.Direction.OPEN,
                        Bracket.Type.NORMAL
                    ),
                    new Bracket(
                        Bracket.Direction.CLOSE,
                        Bracket.Type.NORMAL
                    )
                ))
        );

        System.out.println(template.toFlatJson());
        System.out.println(template.toGzipJson());
    }
}