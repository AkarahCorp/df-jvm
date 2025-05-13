package dev.akarah.dfjvm.compiler.compilation;

import java.lang.classfile.ClassModel;
import java.lang.classfile.CodeModel;
import java.lang.classfile.MethodModel;
import java.util.concurrent.atomic.AtomicInteger;

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
}
