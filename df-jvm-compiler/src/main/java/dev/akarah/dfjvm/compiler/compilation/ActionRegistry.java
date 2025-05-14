package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.PlayerAction;
import dev.akarah.codetemplate.blocks.SelectObjectAction;
import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.blocks.types.SelectionTarget;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.*;
import dev.akarah.dfjvm.compiler.ListUtils;
import dev.akarah.dfjvm.compiler.compilation.util.CodeHelper;
import dev.akarah.dfjvm.compiler.compilation.util.TypeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ActionRegistry {
    Map<String, Function<List<VarItem>, List<TemplateBlock>>> actionSuppliers = new HashMap<>();

    public static ActionRegistry create() {
        return new ActionRegistry()
                .withJavaMethods()
                .withPlayerActions()
                .withLocationMethods();
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

    public ActionRegistry withLocationMethods() {
        return this.with("df/Location#x()D", locals -> List.of(
                new SetVarAction(
                        "GetCoord",
                        Args.empty()
                                .set(CodeHelper.returnVariable(), 0)
                                .set(locals.getFirst(), 1)
                                .set(new VarBlockTag("Plot coordinate", "Coordinate Type", "GetCoord", "set_var"), 25)
                                .set(new VarBlockTag("X", "Coordinate", "GetCoord", "set_var"), 26)
                )
        )).with("df/Location#y()D", locals -> List.of(
                new SetVarAction(
                        "GetCoord",
                        Args.empty()
                                .set(CodeHelper.returnVariable(), 0)
                                .set(locals.getFirst(), 1)
                                .set(new VarBlockTag("Plot coordinate", "Coordinate Type", "GetCoord", "set_var"), 25)
                                .set(new VarBlockTag("Y", "Coordinate", "GetCoord", "set_var"), 26)
                )
        )).with("df/Location#z()D", locals -> List.of(
                new SetVarAction(
                        "GetCoord",
                        Args.empty()
                                .set(CodeHelper.returnVariable(), 0)
                                .set(locals.getFirst(), 1)
                                .set(new VarBlockTag("Plot coordinate", "Coordinate Type", "GetCoord", "set_var"), 25)
                                .set(new VarBlockTag("Z", "Coordinate", "GetCoord", "set_var"), 26)
                )
        )).with("df/Location#of(DDD)Ldf/Location;", locals -> List.of(
                new SetVarAction(
                        "SetAllCoords",
                        Args.empty()
                                .set(CodeHelper.returnVariable(), 0)
                                .set(locals.getFirst(), 1)
                                .set(locals.get(2), 2)
                                .set(locals.get(4), 3)
                                .set(new VarBlockTag("Plot coordinate", "Coordinate Type", "SetAllCoords", "set_var"), 26)
                )
        )).with("df/Location#zeroed()Ldf/Location;", locals -> List.of(
                new SetVarAction(
                        "SetAllCoords",
                        Args.empty()
                                .set(CodeHelper.returnVariable(), 0)
                                .set(new VarNumber("0"), 1)
                                .set(new VarNumber("0"), 2)
                                .set(new VarNumber("0"), 3)
                                .set(new VarBlockTag("Plot coordinate", "Coordinate Type", "SetAllCoords", "set_var"), 26)
                )
        )).with("df/Location#withX(D)Ldf/Location;", locals -> List.of(
                new SetVarAction(
                        "SetCoord",
                        Args.empty()
                                .set(CodeHelper.returnVariable(), 0)
                                .set(locals.getFirst(), 1)
                                .set(locals.get(1), 2)
                                .set(new VarBlockTag("Plot coordinate", "Coordinate Type", "SetCoord", "set_var"), 25)
                                .set(new VarBlockTag("X", "Coordinate", "SetCoord", "set_var"), 26)
                )
        )).with("df/Location#withY(D)Ldf/Location;", locals -> List.of(
                new SetVarAction(
                        "SetCoord",
                        Args.empty()
                                .set(CodeHelper.returnVariable(), 0)
                                .set(locals.getFirst(), 1)
                                .set(locals.get(1), 2)
                                .set(new VarBlockTag("Plot coordinate", "Coordinate Type", "SetCoord", "set_var"), 25)
                                .set(new VarBlockTag("Y", "Coordinate", "SetCoord", "set_var"), 26)
                )
        )).with("df/Location#withZ(D)Ldf/Location;", locals -> List.of(
                new SetVarAction(
                        "SetCoord",
                        Args.empty()
                                .set(CodeHelper.returnVariable(), 0)
                                .set(locals.getFirst(), 1)
                                .set(locals.get(1), 2)
                                .set(new VarBlockTag("Plot coordinate", "Coordinate Type", "SetCoord", "set_var"), 25)
                                .set(new VarBlockTag("Z", "Coordinate", "SetCoord", "set_var"), 26)
                )
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
                ActionRegistry.runForPlayer(
                        "Teleport",
                        List.of(
                                locals.get(1)
                        ),
                        locals
                ),
                ActionRegistry.runForPlayer(
                        "SendMessage",
                        List.of(
                                locals.get(1)
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
