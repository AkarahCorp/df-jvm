package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.CallFunctionAction;
import dev.akarah.codetemplate.blocks.PlayerEvent;
import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.blocks.types.SelectionTarget;
import dev.akarah.codetemplate.template.CodeTemplate;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.template.GzippedCodeTemplateData;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

public class GenerateEvents {
    public static List<TemplateBlock> generatePlayerEvent(String eventName, String functionToCall, List<EventParameter> parameters) {
        var blocks = new ArrayList<TemplateBlock>();
        blocks.add(new PlayerEvent(
                eventName,
                new Args(List.of())
        ));
        int idx = 0;
        for(var param : parameters) {
            blocks.addAll(param.templateList().apply(idx));
            idx += 1;
        }
        blocks.add(new CallFunctionAction(
                functionToCall,
                new Args(List.of())
        ));
        return blocks;
    }

    public static List<GzippedCodeTemplateData> allPlayerEvents() {
        return Stream.of(
                        generatePlayerEvent("Join", "df/Events#player$join(Ldf/Player;)V", List.of(
                                target(SelectionTarget.DEFAULT)
                        )),
                        generatePlayerEvent("Leave", "df/Events#player$leave(Ldf/Player;)V", List.of(
                                target(SelectionTarget.DEFAULT)
                        )),
                        generatePlayerEvent("Command", "df/Events#player$command(Ldf/Player;Ljava/lang/String;)V", List.of(
                                target(SelectionTarget.DEFAULT),
                                varItem(new VarGameValue("Event Command", "Default"))
                        )),
                        generatePlayerEvent("LeftClick", "df/Events#player$leftClick(Ldf/Player;)V", List.of(
                                target(SelectionTarget.DEFAULT)
                        )),
                        generatePlayerEvent("RightClick", "df/Events#player$rightClick(Ldf/Player;)V", List.of(
                                target(SelectionTarget.DEFAULT)
                        ))
                )
                .map(
                        x -> new CodeTemplateData(
                                "x",
                                "x",
                                "1",
                                new CodeTemplate(x)
                        ).gzip()
                ).toList();
    }

    public static EventParameter target(SelectionTarget selectionTarget) {
        return new EventParameter(
                idx -> List.of(
                        new SetVarAction(
                                "+=",
                                new Args(List.of(
                                        new Args.Slot(new VarVariable("memory/idx", VarVariable.Scope.GAME), 0),
                                        new Args.Slot(new VarNumber("1"), 1)
                                ))
                        ),
                        new SetVarAction(
                                "=",
                                new Args(List.of(
                                        new Args.Slot(new VarVariable("memory/ref@%var(memory/idx).player", VarVariable.Scope.GAME), 0),
                                        new Args.Slot(new VarString("%" + selectionTarget.name().toLowerCase(Locale.ROOT)), 1)
                                ))
                        ),
                        StackInfo.setLocal(idx, new VarString("ref@%var(memory/idx)"))
                )
        );
    }

    public static EventParameter varItem(VarItem varItem) {
        return new EventParameter(
                idx -> List.of(StackInfo.setLocal(idx, varItem))
        );
    }

    public record EventParameter(
            Function<Integer, List<TemplateBlock>> templateList
    ) {}
}