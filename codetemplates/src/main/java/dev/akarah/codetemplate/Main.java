package dev.akarah.codetemplate;

import dev.akarah.codetemplate.blocks.PlayerAction;
import dev.akarah.codetemplate.blocks.PlayerEvent;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.CodeTemplate;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.varitem.VarComponent;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        var template = new CodeTemplateData(
                "Endistic",
                "template",
                "1",
                new CodeTemplate(List.of(
                    new PlayerEvent(
                        "Join",
                        new Args(List.of())
                    ),
                        new PlayerAction(
                                "SendMessage",
                                new Args(List.of(
                                        new Args.Slot(
                                                new VarComponent("Hello %default!"),
                                                0
                                        )
                                ))
                        )
                ))
        );

        System.out.println(template.toFlatJson());
        System.out.println(template.toGzipJson());
    }
}