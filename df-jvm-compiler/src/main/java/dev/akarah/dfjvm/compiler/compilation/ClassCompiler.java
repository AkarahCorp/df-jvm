package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.CallFunctionAction;
import dev.akarah.codetemplate.blocks.ControlAction;
import dev.akarah.codetemplate.blocks.FunctionAction;
import dev.akarah.codetemplate.blocks.SetVarAction;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.CodeTemplate;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.VarItem;
import dev.akarah.codetemplate.varitem.VarNumber;
import dev.akarah.codetemplate.varitem.VarString;
import dev.akarah.codetemplate.varitem.VarVariable;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassCompiler {
    ClassData data;

    public static ClassCompiler forData(ClassData data) {
        var cc = new ClassCompiler();
        cc.data = data;
        return cc;
    }

    public List<CodeTemplateData> templatesForClassModel(ClassModel classModel) {
        var templates = new ArrayList<CodeTemplateData>();
        classModel.methods().forEach(methodModel -> templates.add(templateForMethodModel(methodModel)));
        return templates;
    }

    public CodeTemplateData templateForMethodModel(MethodModel methodModel) {
        var codeBlocks = new ArrayList<TemplateBlock>();

        var func = new FunctionAction(methodModel.methodName() + methodModel.methodTypeSymbol().descriptorString(), new Args(List.of()));
        codeBlocks.add(func);

        methodModel.code().ifPresent(codeModel -> codeBlocks.addAll(compileCodeModel(codeModel, methodModel)));

        return new CodeTemplateData("x", "x", "1", new CodeTemplate(codeBlocks));
    }

    public List<TemplateBlock> compileCodeModel(CodeModel codeModel, MethodModel methodModel) {
        var blocks = new ArrayList<TemplateBlock>();
        var point = CompilerPoint.create(methodModel, codeModel);
        codeModel.forEach(codeElement -> blocks.addAll(compileCodeElement(codeElement, point)));
        return blocks;
    }

    public List<TemplateBlock> compileCodeElement(CodeElement codeElement, CompilerPoint point) {
        return switch (codeElement) {
            case Instruction instruction -> compileInstruction(instruction, point);
            case PseudoInstruction pseudoInstruction -> switch (pseudoInstruction) {
                case LabelTarget labelTarget -> {
                    var name = point.method().methodName() + point.method().methodTypeSymbol().descriptorString();
                    var bci = 0;
                    if(point.code() instanceof CodeAttribute codeAttribute) {
                        bci = codeAttribute.labelToBci(labelTarget.label());
                    } else {
                        bci = -1;
                    }
                    yield List.of(
                        new CallFunctionAction(
                                name + "@" + bci,
                                new Args(List.of())
                        ),
                        new FunctionAction(
                                name + "@" + bci,
                                new Args(List.of())
                        )
                    );
                }
                default -> List.of();
            };
            default -> List.of();
        };
    }

    public List<TemplateBlock> compileInstruction(Instruction instruction, CompilerPoint point) {
        return switch (instruction) {
            case ConstantInstruction constantInstruction -> switch (constantInstruction.constantValue()) {
                case Double aDouble -> List.of(
                        point.pushStack(new VarNumber(aDouble.toString()))
                );
                case Float aFloat -> List.of(
                        point.pushStack(new VarNumber(aFloat.toString()))
                );
                case Integer integer -> List.of(
                        point.pushStack(new VarNumber(integer.toString()))
                );
                case Long aLong -> List.of(
                        point.pushStack(new VarNumber(aLong.toString()))
                );
                case String string -> List.of(
                        point.pushStack(new VarString(string))
                );
                default -> List.of();
            };
            case LoadInstruction loadInstruction -> List.of(
                    point.pushStack(point.getLocal(loadInstruction.slot()))
            );
            case ReturnInstruction returnInstruction -> List.of(
                    new ControlAction("Return", new Args(List.of()))
            );
            case StoreInstruction storeInstruction -> List.of(
                    point.setLocal(storeInstruction.slot(), point.popStack())
            );
            default -> List.of();
        };
    }

    public static class CompilerPoint {
        AtomicInteger stackPointer = new AtomicInteger(0);
        MethodModel associatedMethod;
        CodeModel associatedCode;

        public static CompilerPoint create(MethodModel methodModel, CodeModel codeModel) {
            var cc = new CompilerPoint();
            cc.associatedMethod = methodModel;
            cc.associatedCode = codeModel;
            return cc;
        }

        public MethodModel method() {
            return this.associatedMethod;
        }

        public CodeModel code() {
            return this.associatedCode;
        }

        public AtomicInteger stackPointer() {
            return this.stackPointer;
        }

        public SetVarAction pushStack(VarItem varItem) {
            return new SetVarAction(
                    "=",
                    new Args(List.of(
                            new Args.Slot(
                                    new VarVariable("stack[%var(r_depth)][" + this.stackPointer().incrementAndGet() + "]", VarVariable.Scope.LOCAL),
                                    0
                            ),
                            new Args.Slot(varItem, 1)
                    ))
            );
        }

        public VarItem popStack() {
            return new VarVariable("stack[%var(r_depth)][" + this.stackPointer().getAndDecrement() + "]", VarVariable.Scope.LOCAL);
        }

        public SetVarAction setLocal(int local, VarItem varItem) {
            return new SetVarAction(
                    "=",
                    new Args(List.of(
                            new Args.Slot(
                                    new VarVariable("local[%var(r_depth)][" + local + "]", VarVariable.Scope.LOCAL),
                                    0
                            ),
                            new Args.Slot(varItem, 1)
                    ))
            );
        }

        public VarItem getLocal(int local) {
            return new VarVariable("local[%var(r_depth)][" + local + "]", VarVariable.Scope.LOCAL);
        }
    }
}
