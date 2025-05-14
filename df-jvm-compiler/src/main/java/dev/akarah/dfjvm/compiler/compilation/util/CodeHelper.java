package dev.akarah.dfjvm.compiler.compilation.util;

import dev.akarah.codetemplate.blocks.CallFunctionAction;
import dev.akarah.codetemplate.blocks.FunctionAction;
import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.*;
import dev.akarah.dfjvm.compiler.compilation.info.ClassData;

import java.util.List;
import java.util.stream.IntStream;

public class CodeHelper {
    public static SetVarAction setLocal(int local, VarItem varItem) {
        return new SetVarAction(
                "=",
                Args.of(
                        new VarVariable("local." + local, VarVariable.Scope.LINE),
                        varItem
                )
        );
    }

    public static VarVariable getLocal(int local) {
        return new VarVariable("local." + local, VarVariable.Scope.LINE);
    }

    public static List<TemplateBlock> allocateMemory(String placeholderVariable) {
        return List.of(
                new SetVarAction(
                        "+=",
                        Args.of(
                                new VarVariable("memory/idx", VarVariable.Scope.GAME)
                        )
                ),
                new SetVarAction(
                        "=",
                        Args.of(
                                new VarVariable(placeholderVariable, VarVariable.Scope.LINE),
                                new VarString("ref@%var(memory/idx)")
                        )
                )
        );
    }

    public static TemplateBlock setReturnValue(VarItem varItem) {
        return new SetVarAction(
                "=",
                Args.of(
                        new VarVariable("returned", VarVariable.Scope.LINE),
                        varItem
                )
        );
    }

    public static VarVariable returnVariable() {
        return new VarVariable("returned", VarVariable.Scope.LINE);
    }



    public static TemplateBlock callFunction(String name, List<VarItem> parameters) {
        if(!name.contains(")V")) {
            var args = Args.ofSlots(
                    IntStream.range(1, parameters.size()+1)
                            .mapToObj(idx -> new Args.Slot(parameters.get(idx-1), idx))
                            .toList());
            args.insert(new VarVariable("returned", VarVariable.Scope.LINE), 0);
            return new CallFunctionAction(
                    name,
                    args
            );
        } else {
            return new CallFunctionAction(
                    name,
                    Args.ofVarItems(parameters)
            );
        }
    }

    public static TemplateBlock beginFunction(String name, List<VarParameter> parameters, String hiddenValue) {
        var args = Args.ofSlots(
                        IntStream.range(1, parameters.size()+1)
                                .mapToObj(idx -> new Args.Slot(parameters.get(idx-1), idx))
                                .toList());
        args.set(new VarBlockTag(hiddenValue, "Is Hidden", "dynamic", "func"), 26);
        if(!name.contains(")V")) {
            args.insert(new VarParameter("returned", "var", false, false), 0);
        }
        return new FunctionAction(
                name,
                args
        );
    }

    public static boolean templateIsLoaded(CodeTemplateData templateData, ClassData classData) {
        if(templateData.code().blocks().getFirst() instanceof FunctionAction functionAction) {
            var clazzName = functionAction.data().split("#")[0];
            return classData.getClassLoadingOrder().contains(clazzName);
        }
        return true;
    }
}
