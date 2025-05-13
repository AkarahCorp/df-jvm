package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.VarItem;
import dev.akarah.codetemplate.varitem.VarNumber;
import dev.akarah.codetemplate.varitem.VarString;
import dev.akarah.codetemplate.varitem.VarVariable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static SetVarAction recurseDeeper() {
        return new SetVarAction(
                "+=",
                new Args(List.of(
                        new Args.Slot(
                                new VarVariable("r_depth", VarVariable.Scope.LOCAL),
                                0
                        ),
                        new Args.Slot(new VarNumber("1"), 1)
                ))
        );
    }

    public static SetVarAction recurseHigher() {
        return new SetVarAction(
                "-=",
                new Args(List.of(
                        new Args.Slot(
                                new VarVariable("r_depth", VarVariable.Scope.LOCAL),
                                0
                        ),
                        new Args.Slot(new VarNumber("1"), 1)
                ))
        );
    }

    public static List<TemplateBlock> pushParameter(VarItem varItem, int index) {
        return List.of(
                new SetVarAction(
                        "+",
                        new Args(List.of(
                                new Args.Slot(new VarVariable("r1_depth", VarVariable.Scope.LINE), 0),
                                new Args.Slot(new VarVariable("r_depth", VarVariable.Scope.LOCAL), 1),
                                new Args.Slot(new VarNumber("1"), 2)
                        ))
                ),
                new SetVarAction(
                        "=",
                        new Args(List.of(
                                new Args.Slot(
                                        new VarVariable("local[%var(r1_depth)][" + index + "]", VarVariable.Scope.LOCAL),
                                        0
                                ),
                                new Args.Slot(varItem, 1)
                        ))
                )
        );
    }

    public List<TemplateBlock> pushReturnValue() {
        return List.of(
                new SetVarAction(
                        "+",
                        new Args(List.of(
                                new Args.Slot(new VarVariable("r1_depth", VarVariable.Scope.LINE), 0),
                                new Args.Slot(new VarVariable("r_depth", VarVariable.Scope.LOCAL), 1),
                                new Args.Slot(new VarNumber("1"), 2)
                        ))
                ),
                this.pushStack(new VarVariable("rt.%var(r1_depth)", VarVariable.Scope.LOCAL))
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
                                new VarVariable("local[%var(r_depth)][" + local + "]", VarVariable.Scope.LOCAL),
                                0
                        ),
                        new Args.Slot(varItem, 1)
                ))
        );
    }

    public static VarVariable getLocal(int local) {
        return new VarVariable("local[%var(r_depth)][" + local + "]", VarVariable.Scope.LOCAL);
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
                        new Args.Slot(new VarVariable("rt.%var(r_depth)", VarVariable.Scope.LOCAL), 0),
                        new Args.Slot(varItem, 1)
                ))
        );
    }
}
