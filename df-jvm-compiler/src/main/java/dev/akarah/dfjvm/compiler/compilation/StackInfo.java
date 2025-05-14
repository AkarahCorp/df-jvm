package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.CallFunctionAction;
import dev.akarah.codetemplate.blocks.FunctionAction;
import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StackInfo {
    AtomicInteger stackPointer = new AtomicInteger(0);

    public AtomicInteger stackPointer() {
        return this.stackPointer;
    }

    public SetVarAction pushStack(VarItem varItem) {
        return new SetVarAction(
                "=",
                new Args(List.of(
                        new Args.Slot(
                                new VarVariable("stack." + this.stackPointer().incrementAndGet(), VarVariable.Scope.LINE),
                                0
                        ),
                        new Args.Slot(varItem, 1)
                ))
        );
    }


    public List<TemplateBlock> pushReturnValue() {
        return List.of(
                this.pushStack(new VarVariable("returned", VarVariable.Scope.LINE))
        );
    }

    public VarVariable popStack() {
        return new VarVariable("stack." + this.stackPointer().getAndDecrement(), VarVariable.Scope.LINE);
    }

    public static SetVarAction setLocal(int local, VarItem varItem) {
        return new SetVarAction(
                "=",
                new Args(List.of(
                        new Args.Slot(
                                new VarVariable("local." + local, VarVariable.Scope.LINE),
                                0
                        ),
                        new Args.Slot(varItem, 1)
                ))
        );
    }

    public static VarVariable getLocal(int local) {
        return new VarVariable("local." + local, VarVariable.Scope.LINE);
    }

    public static List<TemplateBlock> allocateMemory(String placeholderVariable) {
        return List.of(
                new SetVarAction(
                        "+=",
                        new Args(List.of(
                                new Args.Slot(new VarVariable("memory/idx", VarVariable.Scope.GAME), 0)
                        ))
                ),
                new SetVarAction(
                        "=",
                        new Args(List.of(
                                new Args.Slot(new VarVariable(placeholderVariable, VarVariable.Scope.LINE), 0),
                                new Args.Slot(new VarString("ref@%var(memory/idx)"), 1)
                        ))
                )
        );
    }

    public static TemplateBlock setReturnValue(VarItem varItem) {
        return new SetVarAction(
                "=",
                new Args(List.of(
                        new Args.Slot(new VarVariable("returned", VarVariable.Scope.LINE), 0),
                        new Args.Slot(varItem, 1)
                ))
        );
    }

    public static TemplateBlock callFunction(String name, List<VarItem> parameters) {
        if(!name.contains(")V")) {
            return new CallFunctionAction(
                    name,
                    new Args(
                            Stream.concat(
                                    IntStream.range(1, parameters.size()+1)
                                            .mapToObj(idx -> new Args.Slot(parameters.get(idx-1), idx)),
                                    Stream.of(new Args.Slot(new VarVariable("returned", VarVariable.Scope.LINE), 0))
                            )
                                    .toList()
                    )
            );
        } else {
            return new CallFunctionAction(
                    name,
                    new Args(
                            IntStream.range(0, parameters.size())
                                    .mapToObj(idx -> new Args.Slot(parameters.get(idx), idx))
                                    .toList()
                    )
            );
        }
    }

    public static TemplateBlock beginFunction(String name, List<VarParameter> parameters, String hiddenValue) {
        if(!name.contains(")V")) {
            return new FunctionAction(
                    name,
                    new Args(
                            Stream.concat(
                                            IntStream.range(1, parameters.size()+1)
                                                    .mapToObj(idx -> new Args.Slot(parameters.get(idx-1), idx)),
                                            Stream.of(
                                                    new Args.Slot(new VarParameter("returned", "var", false, false), 0),
                                                    new Args.Slot(
                                                            new VarBlockTag(
                                                                    hiddenValue,
                                                                    "Is Hidden",
                                                                    "dynamic",
                                                                    "func"
                                                            ),
                                                            26
                                                    )
                                            )
                                    )
                                    .toList()
                    )
            );
        } else {
            return new FunctionAction(
                    name,
                    new Args(
                            Stream.concat(
                                    IntStream.range(0, parameters.size())
                                            .mapToObj(idx -> new Args.Slot(parameters.get(idx), idx)),
                                    Stream.of(new Args.Slot(
                                            new VarBlockTag(
                                                    hiddenValue,
                                                    "Is Hidden",
                                                    "dynamic",
                                                    "func"
                                            ), 26))).toList()
                    )
            );
        }
    }

    public static boolean templateIsLoaded(CodeTemplateData templateData, ClassData classData) {
        if(templateData.code().blocks().getFirst() instanceof FunctionAction functionAction) {
            var clazzName = functionAction.data().split("#")[0];
            return classData.getClassLoadingOrder().contains(clazzName);
        }
        return true;
    }
}
