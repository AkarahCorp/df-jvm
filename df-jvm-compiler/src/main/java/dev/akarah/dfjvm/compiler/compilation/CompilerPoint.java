package dev.akarah.dfjvm.compiler.compilation;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;

public class CompilerPoint {
    MethodModel associatedMethod;
    CodeModel associatedCode;
    ClassModel associatedClass;

    public static CompilerPoint create(MethodModel methodModel, CodeModel codeModel, ClassModel classModel) {
        var cc = new CompilerPoint();
        cc.associatedMethod = methodModel;
        cc.associatedCode = codeModel;
        cc.associatedClass = classModel;
        return cc;
    }

    public MethodModel method() {
        return this.associatedMethod;
    }

    public CodeModel code() {
        return this.associatedCode;
    }

    public ClassModel classModel() {
        return this.associatedClass;
    }

    public String functionName() {
        return this.associatedClass.thisClass().asInternalName()
                + "#" + this.associatedMethod.methodName()
                + this.associatedMethod.methodTypeSymbol().descriptorString();
    }

    public int labelToBci(Label label) {
        if(this.code() instanceof CodeAttribute codeAttribute) {
            return codeAttribute.labelToBci(label);
        }
        throw new UnsupportedOperationException("Label is not associated with this compiler point");
    }

    public String functionName(int label) {
        return this.functionName() + "@" + label;
    }
}
