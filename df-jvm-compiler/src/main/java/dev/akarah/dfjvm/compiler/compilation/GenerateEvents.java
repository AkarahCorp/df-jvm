package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.*;
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

        var parameterItems = new ArrayList<Args.Slot>();
        for(var param : parameters) {
            blocks.addAll(param.templateList().apply(idx));
            parameterItems.add(new Args.Slot(new VarVariable("local." + idx, VarVariable.Scope.LINE), idx));
            idx += 1;
        }
        blocks.add(new CallFunctionAction(
                functionToCall,
                new Args(parameterItems)
        ));
        return blocks;
    }

    public static List<TemplateBlock> generateJoinEvent(List<EventParameter> parameters, ClassData classData) {
        var base = generatePlayerEvent("Join", "df/Events#player$join(Ldf/Player;)V", parameters);
        var newCodeblocks = new ArrayList<TemplateBlock>();
        // TODO: call <clinit> methods for classes

        newCodeblocks.add(new IfVarAction(
                "=",
                new Args(List.of(
                        new Args.Slot(new VarGameValue("Player Count", "Default"), 0),
                        new Args.Slot(new VarNumber("1"), 1)
                ))
        ));
        newCodeblocks.add(new Bracket(Bracket.Direction.OPEN, Bracket.Type.NORMAL));

        for(var loadedClass : classData.getClassLoadingOrder()) {
            newCodeblocks.add(StackInfo.callFunction(loadedClass + "#<clinit>()V", List.of()));
        }

        newCodeblocks.add(new Bracket(Bracket.Direction.CLOSE, Bracket.Type.NORMAL));

        base.addAll(1, newCodeblocks);


        return base;
    }

    public static List<GzippedCodeTemplateData> allPlayerEvents(ClassData classData) {
        return Stream.of(
                        generateJoinEvent(List.of(target(SelectionTarget.DEFAULT)), classData),
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