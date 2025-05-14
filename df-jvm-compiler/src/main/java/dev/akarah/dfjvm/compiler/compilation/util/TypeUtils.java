package dev.akarah.dfjvm.compiler.compilation.util;

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
                        Args.of(
                                new VarVariable("refVar", VarVariable.Scope.LINE),
                                dict
                        )
                ),
                new SetVarAction(
                        "SetAllCoords",
                        Args.of(
                                new VarVariable(storeInTempVar, VarVariable.Scope.LINE),
                                new VarNumber("%var(memory/%var(refVar).x)"),
                                new VarNumber("%var(memory/%var(refVar).y)"),
                                new VarNumber("%var(memory/%var(refVar).z)"),
                                new VarNumber("%var(memory/%var(refVar).pitch)"),
                                new VarNumber("%var(memory/%var(refVar).yaw)")
                        ).set(
                                new VarBlockTag("Plot coordinate", "Coordinate Type", "SetAllCoords", "set_var"),
                                26
                        )
                )
        );
    }

    public static List<TemplateBlock> dictToVector(VarItem dict, String storeInTempVar){
        return List.of(
                new SetVarAction(
                        "=",
                        Args.of(
                                new VarVariable("refVar", VarVariable.Scope.LINE),
                                dict
                        )
                ),
                new SetVarAction(
                        "Vector",
                        Args.of(
                                new VarVariable(storeInTempVar, VarVariable.Scope.LINE),
                                new VarNumber("%var(memory/%var(refVar).x)"),
                                new VarNumber("%var(memory/%var(refVar).y)"),
                                new VarNumber("%var(memory/%var(refVar).z)")
                        )
                )
        );
    }

    public static List<TemplateBlock> stringToComponent(VarItem string, String storeInTempVar) {
        return List.of(
                new SetVarAction(
                        "ParseMiniMessageExpr",
                        Args.of(new VarVariable(storeInTempVar, VarVariable.Scope.LINE), string)
                            .set(new VarBlockTag("True", "Parse Legacy Color Codes", "ParseMiniMessageExpr","set_var"), 25)
                            .set(new VarBlockTag("Full","Allowed Tags","ParseMiniMessageExpr","set_var"), 26)
                )
        );
    }
}
