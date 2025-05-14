package dev.akarah.dfjvm.compiler.compilation.info;

import dev.akarah.codetemplate.varitem.VarParameter;
import dev.akarah.codetemplate.varitem.VarVariable;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    public Stream<VarVariable> getAllLocals() {
        if(this.code() instanceof CodeAttribute codeAttribute) {
            if(codeAttribute.maxLocals() > 27) {
                throw new UnsupportedOperationException("please make function" + this.method().methodName() + " have less variables tyty");
            }
            return IntStream.range(0, codeAttribute.maxLocals())
                    .mapToObj(x -> new VarVariable("local." + x, VarVariable.Scope.LINE));
        }
        throw new UnsupportedOperationException("not instance of CodeAttribute");
    }

    public Stream<VarParameter> getCopiedLocalParameters() {
        return this.getAllLocals()
                .map(x -> new VarParameter(x.name(), "any", false, false));
    }

    public Stream<VarParameter> getReferencedLocalParameters() {
        return this.getAllLocals()
                .map(x -> new VarParameter(x.name(), "var", false, false));
    }
}
