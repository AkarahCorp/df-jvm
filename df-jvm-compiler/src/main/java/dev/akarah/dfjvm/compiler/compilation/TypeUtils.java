package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.VarBlockTag;
import dev.akarah.codetemplate.varitem.VarItem;
import dev.akarah.codetemplate.varitem.VarNumber;
import dev.akarah.codetemplate.varitem.VarVariable;

import java.util.List;

public class TypeUtils {
    public static List<TemplateBlock> dictToLoc(VarItem dict, String storeInTempVar) {
        return List.of(
                new SetVarAction(
                        "=",
                        new Args(List.of(
                                new Args.Slot(new VarVariable("refVar", VarVariable.Scope.LINE), 0),
                                new Args.Slot(dict, 1)
                        ))
                ),
                new SetVarAction(
                        "SetAllCoords",
                        new Args(List.of(
                                new Args.Slot(new VarVariable(storeInTempVar, VarVariable.Scope.LINE), 0),
                                new Args.Slot(new VarNumber("%entry(memory/%var(refVar),x)"), 1),
                                new Args.Slot(new VarNumber("%entry(memory/%var(refVar),y)"), 2),
                                new Args.Slot(new VarNumber("%entry(memory/%var(refVar),z)"), 3),
                                new Args.Slot(new VarNumber("%entry(memory/%var(refVar),pitch)"), 4),
                                new Args.Slot(new VarNumber("%entry(memory/%var(refVar),yaw)"), 5),
                                new Args.Slot(new VarBlockTag(
                                        "Plot coordinate",
                                        "Coordinate Type",
                                        "SetAllCoords",
                                        "set_var"
                                ), 26)
                        ))
                )
        );
    }

    public static List<TemplateBlock> dictToVector(VarItem dict, String storeInTempVar){
        return List.of(
                new SetVarAction(
                        "=",
                        new Args(List.of(
                                new Args.Slot(new VarVariable("refVar", VarVariable.Scope.LINE), 0),
                                new Args.Slot(dict, 1)
                        ))
                ),
                new SetVarAction(
                        "Vector",
                        new Args(List.of(
                                new Args.Slot(new VarVariable(storeInTempVar, VarVariable.Scope.LINE), 0),
                                new Args.Slot(new VarNumber("%entry(memory/%var(refVar),x)"), 1),
                                new Args.Slot(new VarNumber("%entry(memory/%var(refVar),y)"), 2),
                                new Args.Slot(new VarNumber("%entry(memory/%var(refVar),z)"), 3)
                        ))
                )
        );
    }

    public static List<TemplateBlock> stringToComponent(VarItem string, String storeInTempVar) {
        return List.of(
                new SetVarAction(
                        "ParseMiniMessageExpr",
                        new Args(List.of(
                                new Args.Slot(new VarVariable(storeInTempVar, VarVariable.Scope.LINE), 0),
                                new Args.Slot(string, 1),

                                new Args.Slot(
                                        new VarBlockTag(
                                                "True",
                                                "Parse Legacy Color Codes",
                                                "ParseMiniMessageExpr",
                                                "set_var"
                                        ),
                                        25
                                ),
                                new Args.Slot(
                                        new VarBlockTag(
                                                "Full",
                                                "Allowed Tags",
                                                "ParseMiniMessageExpr",
                                                "set_var"
                                        ),
                                        26
                                )
                        ))
                )
        );
    }
}
