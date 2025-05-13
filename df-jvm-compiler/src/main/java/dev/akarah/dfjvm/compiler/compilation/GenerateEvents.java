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
import dev.akarah.codetemplate.varitem.VarString;
import dev.akarah.codetemplate.varitem.VarVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class GenerateEvents {
    public static List<TemplateBlock> generatePlayerEvent(String eventName, String functionToCall, List<SelectionTarget> targets) {
        var blocks = new ArrayList<TemplateBlock>();
        blocks.add(new PlayerEvent(
                eventName,
                new Args(List.of())
        ));
        int idx = 0;
        for(var target : targets) {
            blocks.addAll(StackInfo.allocateMemory("tmp_target"));
            blocks.add(new SetVarAction(
                    "=",
                    new Args(List.of(
                            new Args.Slot(new VarVariable("memory/%var(tmp_target).player", VarVariable.Scope.GAME), 0),
                            new Args.Slot(new VarString("%" + target.name().toLowerCase(Locale.ROOT)), 2)
                    ))
            ));
            blocks.add(StackInfo.setLocal(idx, new VarVariable("tmp_target", VarVariable.Scope.LINE)));
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
                generatePlayerEvent("Join", "df/Events#playerJoin(Ldf/Player;)V", List.of(SelectionTarget.DEFAULT)),
                generatePlayerEvent("Leave", "df/Events#playerLeave(Ldf/Player;)V", List.of(SelectionTarget.DEFAULT))
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
}
