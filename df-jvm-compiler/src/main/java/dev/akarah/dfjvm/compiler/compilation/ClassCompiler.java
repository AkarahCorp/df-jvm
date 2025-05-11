package dev.akarah.dfjvm.compiler.compilation;

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
import java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.instruction.*;
import java.lang.constant.ClassDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
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
        classModel.methods().forEach(methodModel -> {
            templates.add(templateForMethodModel(methodModel));
        });
        return templates;
    }

    public CodeTemplateData templateForMethodModel(MethodModel methodModel) {
        var codeBlocks = new ArrayList<TemplateBlock>();

        var func = new FunctionAction(methodModel.methodName() + methodModel.methodTypeSymbol().descriptorString(), new Args(List.of()));
        codeBlocks.add(func);

        methodModel.code().ifPresent(codeModel -> codeBlocks.addAll(compileCodeModel(codeModel)));

        return new CodeTemplateData("x", "x", "1", new CodeTemplate(codeBlocks));
    }

    public List<TemplateBlock> compileCodeModel(CodeModel codeModel) {
        var blocks = new ArrayList<TemplateBlock>();
        var point = CompilerPoint.create();
        codeModel.forEach(codeElement -> blocks.addAll(compileCodeElement(codeElement, point)));
        return blocks;
    }

    public List<TemplateBlock> compileCodeElement(CodeElement codeElement, CompilerPoint point) {
        return switch (codeElement) {
            case CustomAttribute<?> customAttribute -> List.of();
            case Instruction instruction -> compileInstruction(instruction, point);
            case PseudoInstruction pseudoInstruction -> List.of();
            case RuntimeInvisibleTypeAnnotationsAttribute runtimeInvisibleTypeAnnotationsAttribute -> List.of();
            case RuntimeVisibleTypeAnnotationsAttribute runtimeVisibleTypeAnnotationsAttribute -> List.of();
            case StackMapTableAttribute stackMapTableAttribute -> List.of();
        };
    }

    public List<TemplateBlock> compileInstruction(Instruction instruction, CompilerPoint point) {
        return switch (instruction) {
            case ArrayLoadInstruction arrayLoadInstruction -> List.of();
            case ArrayStoreInstruction arrayStoreInstruction ->  List.of();
            case BranchInstruction branchInstruction ->  List.of();
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
                case ClassDesc classDesc -> List.of();
                case DynamicConstantDesc dynamicConstantDesc -> List.of();
                case MethodHandleDesc methodHandleDesc -> List.of();
                case MethodTypeDesc methodTypeDesc -> List.of();
            };
            case ConvertInstruction convertInstruction ->  List.of();
            case DiscontinuedInstruction discontinuedInstruction ->  List.of();
            case FieldInstruction fieldInstruction -> List.of();
            case IncrementInstruction incrementInstruction -> List.of();
            case InvokeDynamicInstruction invokeDynamicInstruction -> List.of();
            case InvokeInstruction invokeInstruction -> List.of();
            case LoadInstruction loadInstruction -> List.of(
                    point.pushStack(point.getLocal(loadInstruction.slot()))
            );
            case LookupSwitchInstruction lookupSwitchInstruction -> List.of();
            case MonitorInstruction monitorInstruction -> List.of();
            case NewMultiArrayInstruction newMultiArrayInstruction -> List.of();
            case NewObjectInstruction newObjectInstruction -> List.of();
            case NewPrimitiveArrayInstruction newPrimitiveArrayInstruction -> List.of();
            case NewReferenceArrayInstruction newReferenceArrayInstruction -> List.of();
            case NopInstruction nopInstruction -> List.of();
            case OperatorInstruction operatorInstruction -> List.of();
            case ReturnInstruction returnInstruction -> List.of(
                    new ControlAction("Return", new Args(List.of()))
            );
            case StackInstruction stackInstruction -> List.of();
            case StoreInstruction storeInstruction -> List.of(
                    point.setLocal(storeInstruction.slot(), point.popStack())
            );
            case TableSwitchInstruction tableSwitchInstruction -> List.of();
            case ThrowInstruction throwInstruction -> List.of();
            case TypeCheckInstruction typeCheckInstruction -> List.of();
        };
    }

    public static class CompilerPoint {
        AtomicInteger stackPointer = new AtomicInteger(0);

        public static CompilerPoint create() {
            return new CompilerPoint();
        }

        public AtomicInteger stackPointer() {
            return this.stackPointer;
        }

        public SetVarAction pushStack(VarItem varItem) {
            return new SetVarAction(
                    "=",
                    new Args(List.of(
                            new Args.Slot(
                                    new VarVariable("stack[" + this.stackPointer().incrementAndGet() + "]", VarVariable.Scope.LINE),
                                    0
                            ),
                            new Args.Slot(varItem, 1)
                    ))
            );
        }

        public VarItem popStack() {
            return new VarVariable("stack[" + this.stackPointer().getAndDecrement() + "]", VarVariable.Scope.LINE);
        }

        public SetVarAction setLocal(int local, VarItem varItem) {
            return new SetVarAction(
                    "=",
                    new Args(List.of(
                            new Args.Slot(
                                    new VarVariable("local[" + local + "]", VarVariable.Scope.LINE),
                                    0
                            ),
                            new Args.Slot(varItem, 1)
                    ))
            );
        }

        public VarItem getLocal(int local) {
            return new VarVariable("local[" + local + "]", VarVariable.Scope.LINE);
        }
    }
}
