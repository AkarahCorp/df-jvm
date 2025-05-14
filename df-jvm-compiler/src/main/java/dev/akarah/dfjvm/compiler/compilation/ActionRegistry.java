package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.PlayerAction;
import dev.akarah.codetemplate.blocks.SelectObjectAction;
import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.blocks.types.SelectionTarget;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.VarItem;
import dev.akarah.codetemplate.varitem.VarString;
import dev.akarah.codetemplate.varitem.VarVariable;
import dev.akarah.dfjvm.compiler.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ActionRegistry {
    Map<String, Function<List<VarItem>, List<TemplateBlock>>> actionSuppliers = new HashMap<>();

    public static ActionRegistry create() {
        return new ActionRegistry();
    }

    public ActionRegistry withJavaMethods() {
        return this.with("java/lang/Object#toString()Ljava/lang/String;", locals -> List.of(
            new SetVarAction(
                    "String",
                    Args.of(
                            new VarVariable("tmp", VarVariable.Scope.LINE),
                            locals.getFirst()
                    )
            ),
            CodeHelper.setReturnValue(new VarVariable("tmp", VarVariable.Scope.LINE))
        ));
    }

    public ActionRegistry withPlayerActions() {
        return this.with("df/Player#sendMessage(Ljava/lang/String;)V", locals -> ListUtils.join(
                TypeUtils.stringToComponent(locals.get(1), "comp"),
                ActionRegistry.runForPlayer(
                        "SendMessage",
                        List.of(new VarVariable("comp", VarVariable.Scope.LINE)),
                        locals
                )
        )).with("df/Player#sendActionBar(Ljava/lang/String;)V", locals -> ListUtils.join(
                TypeUtils.stringToComponent(locals.get(1), "comp"),
                ActionRegistry.runForPlayer(
                        "ActionBar",
                        List.of(new VarVariable("comp", VarVariable.Scope.LINE)),
                        locals
                )
        )).with("df/Player#heal(I)V", locals -> ActionRegistry.runForPlayer(
                "Heal",
                List.of(locals.get(1)),
                locals
        )).with("df/Player#damage(I)V", locals -> ActionRegistry.runForPlayer(
                "Damage",
                List.of(locals.get(1)),
                locals
        )).with("df/Player#setHealth(I)V", locals -> ActionRegistry.runForPlayer(
                "SetHealth",
                List.of(locals.get(1)),
                locals
        )).with("df/Player#teleport(Ldf/Location;)V", locals -> ListUtils.join(
                TypeUtils.dictToLoc(locals.get(1), "loc"),
                ActionRegistry.runForPlayer(
                        "Teleport",
                        List.of(
                                new VarVariable("loc", VarVariable.Scope.LINE)
                        ),
                        locals
                )
        ));
    }

    public ActionRegistry with(String descriptor, Function<List<VarItem>, List<TemplateBlock>> function) {
        this.actionSuppliers.put(descriptor, function);
        return this;
    }

    public static List<TemplateBlock> runForPlayer(String action, Args args, List<VarItem> locals) {
        return List.of(
                new SetVarAction(
                        "=",
                        Args.of(
                                new VarVariable("tmp.player_name", VarVariable.Scope.LINE),
                                locals.getFirst()
                        )
                ),
                new SelectObjectAction(
                        "PlayerName",
                        Args.of(
                                new VarString("%var(memory/%var(tmp.player_name).player)")
                        )
                ),
                new PlayerAction(
                        action,
                        args,
                        Optional.of(SelectionTarget.SELECTION)
                ),
                new SelectObjectAction(
                        "Reset",
                        Args.empty()
                )
        );
    }

    public static List<TemplateBlock> runForPlayer(String action, List<VarItem> parameters, List<VarItem> locals) {
        return runForPlayer(action, Args.ofVarItems(parameters), locals);
    }
}
