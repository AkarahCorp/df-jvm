package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.PlayerAction;
import dev.akarah.codetemplate.blocks.SelectObjectAction;
import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.blocks.types.SelectionTarget;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.VarString;
import dev.akarah.codetemplate.varitem.VarVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ActionRegistry {
    Map<String, Supplier<List<TemplateBlock>>> actionSuppliers = new HashMap<>();

    public static ActionRegistry create() {
        return new ActionRegistry();
    }

    public ActionRegistry withPlayerActions() {
        return this.with("df/Player#sendMessage(Ljava/lang/String;)V", () -> ActionRegistry.runForPlayer(
                new PlayerAction(
                        "SendMessage",
                        new Args(List.of(
                                new Args.Slot(ClassCompiler.CompilerPoint.getLocal(1), 0)
                        )),
                        Optional.of(SelectionTarget.SELECTION)
                )
        )).with("df/Player#sendActionBar(Ljava/lang/String;)V", () -> ActionRegistry.runForPlayer(
                new PlayerAction(
                        "ActionBar",
                        new Args(List.of(
                                new Args.Slot(ClassCompiler.CompilerPoint.getLocal(1), 0)
                        )),
                        Optional.of(SelectionTarget.SELECTION)
                )
        )).with("df/Player#heal(I)V", () -> ActionRegistry.runForPlayer(
                new PlayerAction(
                        "Heal",
                        new Args(List.of(
                                new Args.Slot(ClassCompiler.CompilerPoint.getLocal(1), 0)
                        )),
                        Optional.of(SelectionTarget.SELECTION)
                )
        )).with("df/Player#damage(I)V", () -> ActionRegistry.runForPlayer(
                new PlayerAction(
                        "Damage",
                        new Args(List.of(
                                new Args.Slot(ClassCompiler.CompilerPoint.getLocal(1), 0)
                        )),
                        Optional.of(SelectionTarget.SELECTION)
                )
        )).with("df/Player#setHealth(I)V", () -> ActionRegistry.runForPlayer(
                new PlayerAction(
                        "SetHealth",
                        new Args(List.of(
                                new Args.Slot(ClassCompiler.CompilerPoint.getLocal(1), 0)
                        )),
                        Optional.of(SelectionTarget.SELECTION)
                )
        ));
    }

    public ActionRegistry with(String descriptor, Supplier<List<TemplateBlock>> function) {
        this.actionSuppliers.put(descriptor, function);
        return this;
    }

    public static List<TemplateBlock> runForPlayer(PlayerAction base) {
        return List.of(
                new SetVarAction(
                        "=",
                        new Args(List.of(
                                new Args.Slot(new VarVariable("tmp", VarVariable.Scope.LINE), 0),
                                new Args.Slot(ClassCompiler.CompilerPoint.getLocal(0), 1)
                        ))
                ),
                new SelectObjectAction(
                        "PlayerName",
                        new Args(List.of(
                                new Args.Slot(new VarString("%entry(memory/%var(tmp),player)"), 0)
                        ))
                ),
                base,
                new SelectObjectAction(
                        "Reset",
                        new Args(List.of())
                )
        );
    }
}
