package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.*;

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
                Args.of(
                        new VarVariable("stack." + this.stackPointer().incrementAndGet(), VarVariable.Scope.LINE),
                        varItem
                )
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
}
