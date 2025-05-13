package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.*;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.CodeTemplate;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.*;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ClassCompiler {
    ClassData data;
    ActionRegistry actionRegistry;

    public static ClassCompiler forData(ClassData data) {
        var cc = new ClassCompiler();
        cc.data = data;
        return cc;
    }

    public ClassCompiler withActionRegistry(ActionRegistry actionRegistry) {
        this.actionRegistry = actionRegistry;
        return this;
    }

    public List<CodeTemplateData> templatesForClassModel(ClassModel classModel) {
        var templates = new ArrayList<CodeTemplateData>();
        classModel.methods().forEach(methodModel -> templates.add(templateForMethodModel(methodModel, classModel)));
        return templates;
    }

    public CodeTemplateData templateForMethodModel(MethodModel methodModel, ClassModel classModel) {
        var codeBlocks = new ArrayList<TemplateBlock>();


        methodModel.code().ifPresent(codeModel -> codeBlocks.addAll(compileCodeModel(codeModel, methodModel, classModel)));

        return new CodeTemplateData("x", "x", "1", new CodeTemplate(codeBlocks));
    }

    public List<TemplateBlock> compileCodeModel(CodeModel codeModel, MethodModel methodModel, ClassModel classModel) {
        var blocks = new ArrayList<TemplateBlock>();
        var point = CompilerPoint.create(methodModel, codeModel, classModel);
        var si = new StackInfo();
        blocks.add(new FunctionAction(point.functionName(), new Args(List.of())));
        codeModel.forEach(codeElement -> blocks.addAll(compileCodeElement(codeElement, point, si)));
        return blocks;
    }

    public List<TemplateBlock> compileCodeElement(CodeElement codeElement, CompilerPoint point, StackInfo stackInfo) {
        return switch (codeElement) {
            case Instruction instruction -> compileInstruction(instruction, point, stackInfo, true);
            case PseudoInstruction pseudoInstruction -> switch (pseudoInstruction) {
                case LabelTarget labelTarget -> {
                    var bci = point.labelToBci(labelTarget.label());
                    yield List.of(
                        new CallFunctionAction(
                                point.functionName(bci),
                                new Args(List.of())
                        ),
                        new FunctionAction(
                                point.functionName(bci),
                                new Args(List.of())
                        )
                    );
                }
                default -> List.of();
            };
            default -> List.of();
        };
    }

    public List<TemplateBlock> compileInstruction(Instruction instruction, CompilerPoint point, StackInfo stackInfo, boolean debug) {
        var out = compileInstruction(instruction, point, stackInfo);
        if(debug) {
            var list = new ArrayList<>(out);
            list.add(new ControlAction(
                    "Comment",
                    new Args(List.of(
                        new Args.Slot(new VarString(instruction.toString()), 0)
                    ))
            ));
            return list;
        } else {
            return out;
        }
    }

    public List<TemplateBlock> compileInstruction(Instruction instruction, CompilerPoint point, StackInfo stackInfo) {
        return switch (instruction) {
            case NewObjectInstruction _ -> {
                var blocks = new ArrayList<>(StackInfo.allocateMemory("tmp"));
                blocks.add(stackInfo.pushStack(new VarString("%var(tmp)")));
                yield blocks;
            }
            case FieldInstruction fieldInstruction -> switch (fieldInstruction.opcode()) {
                case GETFIELD -> {
                    var objectRef = stackInfo.popStack();
                    yield List.of(
                            stackInfo.pushStack(
                                    new VarVariable(
                                            "memory/%var(" + objectRef.name() + ")." + fieldInstruction.field().name().stringValue(),
                                            VarVariable.Scope.GAME
                                    )
                            )
                    );
                }
                case PUTFIELD -> {
                    var newValue = stackInfo.popStack();
                    var objectRef = stackInfo.popStack();
                    yield List.of(
                            new SetVarAction(
                                    "=",
                                    new Args(List.of(
                                            new Args.Slot(new VarVariable("memory/%var(" + objectRef.name() + ")." + fieldInstruction.field().name().stringValue(), VarVariable.Scope.GAME), 0),
                                            new Args.Slot(newValue, 2)
                                    ))
                            )
                    );
                }
                default -> throw new RuntimeException("uh not supported " + fieldInstruction.opcode() + " byebye");
            };
            case ConstantInstruction constantInstruction -> switch (constantInstruction.constantValue()) {
                case Double aDouble -> List.of(
                        stackInfo.pushStack(new VarNumber(aDouble.toString()))
                );
                case Float aFloat -> List.of(
                        stackInfo.pushStack(new VarNumber(aFloat.toString()))
                );
                case Integer integer -> List.of(
                        stackInfo.pushStack(new VarNumber(integer.toString()))
                );
                case Long aLong -> List.of(
                        stackInfo.pushStack(new VarNumber(aLong.toString()))
                );
                case String string -> List.of(
                        stackInfo.pushStack(new VarString(string))
                );
                default -> List.of();
            };
            case InvokeInstruction invokeInstruction -> {
                var functionName = invokeInstruction.method().owner().asInternalName()
                        + "#" + invokeInstruction.method().name()
                        + invokeInstruction.typeSymbol().descriptorString();

                if(functionName.equals("java/lang/Object#<init>()V")) {
                    yield List.of();
                }

                var instructions = new ArrayList<TemplateBlock>();

                int si = 0;
                if(invokeInstruction.opcode() != Opcode.INVOKESTATIC) {
                    si = 1;
                }

                var slot = si;
                for(var idx = 0; idx < invokeInstruction.typeSymbol().parameterCount(); idx++) {
                    var parameterSymbol = invokeInstruction.typeSymbol().parameterList().get(idx);
                    instructions.addAll(StackInfo.pushParameter(stackInfo.popStack(), slot));

                    if(parameterSymbol.descriptorString().equals("D") || parameterSymbol.descriptorString().equals("L")) {
                        slot += 2;
                    } else {
                        slot += 1;
                    }
                }
                if(invokeInstruction.opcode() != Opcode.INVOKESTATIC) {
                    instructions.addAll(StackInfo.pushParameter(stackInfo.popStack(), 0));
                }
                instructions.add(StackInfo.recurseDeeper());

                if(this.actionRegistry.actionSuppliers.containsKey(functionName)) {
                    instructions.addAll(this.actionRegistry.actionSuppliers.get(functionName).apply(point));
                } else {
                    instructions.add(new CallFunctionAction(
                            functionName,
                            new Args(List.of())
                    ));
                }
                instructions.add(StackInfo.recurseHigher());


                if(!invokeInstruction.typeSymbol().returnType().descriptorString().equals("V")) {
                    instructions.addAll(stackInfo.pushReturnValue());
                }
                yield instructions;
            }
            case LoadInstruction loadInstruction -> List.of(
                    stackInfo.pushStack(StackInfo.getLocal(loadInstruction.slot()))
            );
            case ReturnInstruction _ -> List.of(
                    new SetVarAction(
                            "=",
                            new Args(List.of(
                                    new Args.Slot(new VarVariable("rt.%var(r_depth)", VarVariable.Scope.LOCAL), 0),
                                    new Args.Slot(stackInfo.popStack(), 1)
                            ))
                    ),
                    new ControlAction("Return", new Args(List.of()))
            );
            case StoreInstruction storeInstruction -> List.of(
                    StackInfo.setLocal(storeInstruction.slot(), stackInfo.popStack())
            );
            case OperatorInstruction operatorInstruction -> {
                Function<String, List<TemplateBlock>> binaryOp = action -> List.of(
                        new SetVarAction(
                                action,
                                new Args(List.of(
                                        new Args.Slot(new VarVariable("tmp", VarVariable.Scope.LINE), 0),
                                        new Args.Slot(stackInfo.popStack(), 1),
                                        new Args.Slot(stackInfo.popStack(), 2)
                                ))
                        ),
                        stackInfo.pushStack(new VarVariable("tmp", VarVariable.Scope.LINE))
                );

                yield switch (operatorInstruction.opcode()) {
                    case Opcode.IADD, Opcode.LADD, Opcode.FADD, Opcode.DADD -> binaryOp.apply("+");
                    case Opcode.ISUB, Opcode.LSUB, Opcode.FSUB, Opcode.DSUB -> binaryOp.apply("-");
                    case Opcode.IMUL, Opcode.LMUL, Opcode.FMUL, Opcode.DMUL -> binaryOp.apply("*");
                    case Opcode.IDIV, Opcode.LDIV, Opcode.FDIV, Opcode.DDIV -> binaryOp.apply("/");
                    case Opcode.IREM, Opcode.LREM, Opcode.FREM, Opcode.DREM -> binaryOp.apply("%");
                    default -> List.of();
                };
            }
            case StackInstruction stackInstruction -> switch (stackInstruction.opcode()) {
                case DUP -> List.of(
                        new SetVarAction(
                                "=",
                                new Args(List.of(
                                        new Args.Slot(new VarVariable("tmp", VarVariable.Scope.LINE), 0),
                                        new Args.Slot(stackInfo.popStack(), 1)
                                ))
                        ),
                        stackInfo.pushStack(new VarVariable("tmp", VarVariable.Scope.LINE)),
                        stackInfo.pushStack(new VarVariable("tmp", VarVariable.Scope.LINE))
                );
                default -> throw new RuntimeException("idk " + stackInstruction);
            };
            case NewPrimitiveArrayInstruction newPrimitiveArrayInstruction -> {
                var count = stackInfo.popStack();
                var blocks = new ArrayList<>(StackInfo.allocateMemory("array_ptr_tmp"));
                blocks.add(stackInfo.pushStack(new VarString("%var(array_ptr_tmp)")));
                yield blocks;
            }
            case ArrayLoadInstruction arrayLoadInstruction -> {
                var index = stackInfo.popStack();
                var arrayRef = stackInfo.popStack();

                yield List.of(
                        stackInfo.pushStack(new VarVariable("memory/%var(" + arrayRef.name() + ")[%var(" + index.name() + ")]", VarVariable.Scope.GAME))
                );
            }
            case ArrayStoreInstruction arrayStoreInstruction -> {
                var value = stackInfo.popStack();
                var index = stackInfo.popStack();
                var arrayRef = stackInfo.popStack();

                yield List.of(
                        new SetVarAction(
                                "=",
                                new Args(List.of(
                                        new Args.Slot(new VarVariable("memory/%var(" +  arrayRef.name() + ")[%var(" + index.name() + ")]", VarVariable.Scope.GAME), 0),
                                        new Args.Slot(value, 1)
                                ))
                        )
                );
            }
            default -> List.of();
        };
    }

}
