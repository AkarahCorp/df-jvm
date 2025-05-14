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
