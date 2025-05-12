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
import java.util.function.Function;

public class ClassCompiler {
    ClassData data;

    public static ClassCompiler forData(ClassData data) {
        var cc = new ClassCompiler();
        cc.data = data;
        return cc;
    }

    public List<CodeTemplateData> templatesForClassModel(ClassModel classModel) {
        var templates = new ArrayList<CodeTemplateData>();
        classModel.methods().forEach(methodModel -> templates.add(templateForMethodModel(methodModel, classModel)));
        return templates;
    }

    public CodeTemplateData templateForMethodModel(MethodModel methodModel, ClassModel classModel) {
        var codeBlocks = new ArrayList<TemplateBlock>();

        var func = new FunctionAction(classModel.thisClass().asInternalName() + "#" + methodModel.methodName() + methodModel.methodTypeSymbol().descriptorString(), new Args(List.of()));
        codeBlocks.add(func);

        methodModel.code().ifPresent(codeModel -> codeBlocks.addAll(compileCodeModel(codeModel, methodModel, classModel)));

        return new CodeTemplateData("x", "x", "1", new CodeTemplate(codeBlocks));
    }

    public List<TemplateBlock> compileCodeModel(CodeModel codeModel, MethodModel methodModel, ClassModel classModel) {
        var blocks = new ArrayList<TemplateBlock>();
        var point = CompilerPoint.create(methodModel, codeModel, classModel);
        codeModel.forEach(codeElement -> blocks.addAll(compileCodeElement(codeElement, point)));
        return blocks;
    }

    public List<TemplateBlock> compileCodeElement(CodeElement codeElement, CompilerPoint point) {
        return switch (codeElement) {
            case Instruction instruction -> compileInstruction(instruction, point, true);
            case PseudoInstruction pseudoInstruction -> switch (pseudoInstruction) {
                case LabelTarget labelTarget -> {
                    var name = point.classModel().thisClass().asInternalName() + "#" + point.method().methodName() + point.method().methodTypeSymbol().descriptorString();
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

    public List<TemplateBlock> compileInstruction(Instruction instruction, CompilerPoint point, boolean debug) {
        var out = compileInstruction(instruction, point);
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

    public List<TemplateBlock> compileInstruction(Instruction instruction, CompilerPoint point) {
        return switch (instruction) {
            case NewObjectInstruction newObjectInstruction -> {
                var blocks = new ArrayList<>(point.allocateMemory("tmp"));
                blocks.add(point.pushStack(new VarString("%var(tmp)")));
                blocks.add(new SetVarAction(
                    "CreateDict",
                    new Args(List.of(
                            new Args.Slot(new VarVariable("memory/%var(tmp)", VarVariable.Scope.GAME), 0)
                    ))
                ));
                yield blocks;
            }
            case FieldInstruction fieldInstruction -> switch (fieldInstruction.opcode()) {
                case GETFIELD -> List.of(
                        new SetVarAction(
                                "=",
                                new Args(List.of(
                                        new Args.Slot(new VarVariable("addr", VarVariable.Scope.LINE), 0),
                                        new Args.Slot(point.popStack(), 1)
                                ))
                        ),
                        new SetVarAction(
                                "GetDictValue",
                                new Args(List.of(
                                        new Args.Slot(new VarVariable("stack_var", VarVariable.Scope.LINE), 0),
                                        new Args.Slot(new VarVariable("memory/%var(addr)", VarVariable.Scope.GAME), 1),
                                        new Args.Slot(new VarString(fieldInstruction.field().name().stringValue()), 2)
                                ))
                        ),
                        point.pushStack(new VarVariable("stack_var", VarVariable.Scope.LINE))
                );
                case PUTFIELD -> {
                    var newValue = point.popStack();
                    var objectRef = point.popStack();
                    yield List.of(
                            new SetVarAction(
                                    "=",
                                    new Args(List.of(
                                            new Args.Slot(new VarVariable("addr", VarVariable.Scope.LINE), 0),
                                            new Args.Slot(objectRef, 1)
                                    ))
                            ),
                            new SetVarAction(
                                    "SetDictValue",
                                    new Args(List.of(
                                            new Args.Slot(new VarVariable("memory/%var(addr)", VarVariable.Scope.GAME), 0),
                                            new Args.Slot(new VarString(fieldInstruction.field().name().stringValue()), 1),
                                            new Args.Slot(newValue, 2)
                                    ))
                            )
                    );
                }
                default -> throw new RuntimeException("uh not supported " + fieldInstruction.opcode() + " byebye");
            };
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
            case InvokeInstruction invokeInstruction -> {
                var functionName = invokeInstruction.method().owner().asInternalName()
                        + "#" + invokeInstruction.method().name()
                        + invokeInstruction.typeSymbol().descriptorString();

                var instructions = new ArrayList<TemplateBlock>();

                int si = 0;
                if(invokeInstruction.opcode() != Opcode.INVOKESTATIC) {
                    si = 1;
                }

                for(var idx = 0; idx < invokeInstruction.typeSymbol().parameterCount(); idx++) {
                    instructions.addAll(point.pushParameter(point.popStack(), idx + si));
                }
                if(invokeInstruction.opcode() != Opcode.INVOKESTATIC) {
                    instructions.addAll(point.pushParameter(point.popStack(), 0));
                }
                instructions.add(point.recurseDeeper());
                instructions.add(new CallFunctionAction(
                        functionName,
                        new Args(List.of())
                ));
                instructions.add(point.recurseHigher());
                if(!invokeInstruction.typeSymbol().returnType().descriptorString().equals("V")) {
                    instructions.addAll(point.pushReturnValue());
                }
                yield instructions;
            }
            case LoadInstruction loadInstruction -> List.of(
                    point.pushStack(point.getLocal(loadInstruction.slot()))
            );
            case ReturnInstruction _ -> List.of(
                    new SetVarAction(
                            "=",
                            new Args(List.of(
                                    new Args.Slot(new VarVariable("stack[%var(r_depth)][returned]", VarVariable.Scope.LOCAL), 0),
                                    new Args.Slot(point.popStack(), 1)
                            ))
                    ),
                    new ControlAction("Return", new Args(List.of()))
            );
            case StoreInstruction storeInstruction -> List.of(
                    point.setLocal(storeInstruction.slot(), point.popStack())
            );
            case OperatorInstruction operatorInstruction -> {
                Function<String, List<TemplateBlock>> binaryOp = action -> List.of(
                        new SetVarAction(
                                action,
                                new Args(List.of(
                                        new Args.Slot(new VarVariable("tmp", VarVariable.Scope.LINE), 0),
                                        new Args.Slot(point.popStack(), 1),
                                        new Args.Slot(point.popStack(), 2)
                                ))
                        ),
                        point.pushStack(new VarVariable("tmp", VarVariable.Scope.LINE))
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
                                        new Args.Slot(point.popStack(), 1)
                                ))
                        ),
                        point.pushStack(new VarVariable("tmp", VarVariable.Scope.LINE)),
                        point.pushStack(new VarVariable("tmp", VarVariable.Scope.LINE))
                );
                default -> throw new RuntimeException("idk " + stackInstruction);
            };
            default -> List.of();
        };
    }

    public static class CompilerPoint {
        AtomicInteger stackPointer = new AtomicInteger(0);
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

        public SetVarAction recurseDeeper() {
            return new SetVarAction(
                    "+=",
                    new Args(List.of(
                            new Args.Slot(
                                    new VarVariable("r_depth", VarVariable.Scope.LOCAL),
                                    0
                            ),
                            new Args.Slot(new VarNumber("1"), 1)
                    ))
            );
        }

        public SetVarAction recurseHigher() {
            return new SetVarAction(
                    "-=",
                    new Args(List.of(
                            new Args.Slot(
                                    new VarVariable("r_depth", VarVariable.Scope.LOCAL),
                                    0
                            ),
                            new Args.Slot(new VarNumber("1"), 1)
                    ))
            );
        }

        public List<TemplateBlock> pushParameter(VarItem varItem, int index) {
            return List.of(
                    new SetVarAction(
                            "+",
                            new Args(List.of(
                                    new Args.Slot(new VarVariable("r1_depth", VarVariable.Scope.LINE), 0),
                                    new Args.Slot(new VarVariable("r_depth", VarVariable.Scope.LOCAL), 1),
                                    new Args.Slot(new VarNumber("1"), 2)
                            ))
                    ),
                    new SetVarAction(
                            "=",
                            new Args(List.of(
                                    new Args.Slot(
                                            new VarVariable("local[%var(r1_depth)][" + index + "]", VarVariable.Scope.LOCAL),
                                            0
                                    ),
                                    new Args.Slot(varItem, 1)
                            ))
                    )
            );
        }

        public List<TemplateBlock> pushReturnValue() {
            return List.of(
                    new SetVarAction(
                            "+",
                            new Args(List.of(
                                    new Args.Slot(new VarVariable("r1_depth", VarVariable.Scope.LINE), 0),
                                    new Args.Slot(new VarVariable("r_depth", VarVariable.Scope.LOCAL), 1),
                                    new Args.Slot(new VarNumber("1"), 2)
                            ))
                    ),
                    this.pushStack(new VarVariable("stack[%var(r1_depth)][returned]", VarVariable.Scope.LOCAL))
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

        public List<TemplateBlock> allocateMemory(String placeholderVariable) {
            return List.of(
                    new SetVarAction(
                            "+=",
                            new Args(List.of(
                                    new Args.Slot(new VarVariable("memory/idx", VarVariable.Scope.GAME), 0)
                            ))
                    ),
                    new SetVarAction(
                            "=",
                            new Args(List.of(
                                    new Args.Slot(new VarVariable(placeholderVariable, VarVariable.Scope.LINE), 0),
                                    new Args.Slot(new VarString("ref@%var(memory/idx)"), 1)
                            ))
                    )
            );
        }
    }
}
