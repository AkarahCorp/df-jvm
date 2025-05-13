package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.*;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.blocks.types.SelectionTarget;
import dev.akarah.codetemplate.template.CodeTemplate;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.*;

import java.lang.classfile.*;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.instruction.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
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
            case NewObjectInstruction _ -> {
                var blocks = new ArrayList<>(CompilerPoint.allocateMemory("tmp"));
                blocks.add(point.pushStack(new VarString("%var(tmp)")));
                yield blocks;
            }
            case FieldInstruction fieldInstruction -> switch (fieldInstruction.opcode()) {
                case GETFIELD -> {
                    var objectRef = point.popStack();
                    yield List.of(
                            point.pushStack(
                                    new VarVariable(
                                            "memory/%var(" + objectRef.name() + ")." + fieldInstruction.field().name().stringValue(),
                                            VarVariable.Scope.GAME
                                    )
                            )
                    );
                }
                case PUTFIELD -> {
                    var newValue = point.popStack();
                    var objectRef = point.popStack();
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
                    instructions.addAll(CompilerPoint.pushParameter(point.popStack(), slot));

                    if(parameterSymbol.descriptorString().equals("D") || parameterSymbol.descriptorString().equals("L")) {
                        slot += 2;
                    } else {
                        slot += 1;
                    }
                }
                if(invokeInstruction.opcode() != Opcode.INVOKESTATIC) {
                    instructions.addAll(CompilerPoint.pushParameter(point.popStack(), 0));
                }
                instructions.add(CompilerPoint.recurseDeeper());

                if(this.actionRegistry.actionSuppliers.containsKey(functionName)) {
                    instructions.addAll(this.actionRegistry.actionSuppliers.get(functionName).apply(point));
                } else {
                    instructions.add(new CallFunctionAction(
                            functionName,
                            new Args(List.of())
                    ));
                }
                instructions.add(CompilerPoint.recurseHigher());


                if(!invokeInstruction.typeSymbol().returnType().descriptorString().equals("V")) {
                    instructions.addAll(point.pushReturnValue());
                }
                yield instructions;
            }
            case LoadInstruction loadInstruction -> List.of(
                    point.pushStack(CompilerPoint.getLocal(loadInstruction.slot()))
            );
            case ReturnInstruction _ -> List.of(
                    new SetVarAction(
                            "=",
                            new Args(List.of(
                                    new Args.Slot(new VarVariable("rt.%var(r_depth)", VarVariable.Scope.LOCAL), 0),
                                    new Args.Slot(point.popStack(), 1)
                            ))
                    ),
                    new ControlAction("Return", new Args(List.of()))
            );
            case StoreInstruction storeInstruction -> List.of(
                    CompilerPoint.setLocal(storeInstruction.slot(), point.popStack())
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
            case NewPrimitiveArrayInstruction newPrimitiveArrayInstruction -> {
                var count = point.popStack();

                var blocks = new ArrayList<>(CompilerPoint.allocateMemory("array_ptr_tmp"));
                blocks.add(point.pushStack(new VarString("%var(array_ptr_tmp)")));
                yield blocks;
            }
            case ArrayLoadInstruction arrayLoadInstruction -> {
                var index = point.popStack();
                var arrayRef = point.popStack();

                yield List.of(
                        point.pushStack(new VarVariable("memory/%var(" + arrayRef.name() + ")[%var(" + index.name() + ")]", VarVariable.Scope.GAME))
                );
            }
            case ArrayStoreInstruction arrayStoreInstruction -> {
                var value = point.popStack();
                var index = point.popStack();
                var arrayRef = point.popStack();

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
                                    new VarVariable("stack." + this.stackPointer().incrementAndGet(), VarVariable.Scope.LINE),
                                    0
                            ),
                            new Args.Slot(varItem, 1)
                    ))
            );
        }

        public static SetVarAction recurseDeeper() {
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

        public static SetVarAction recurseHigher() {
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

        public static List<TemplateBlock> pushParameter(VarItem varItem, int index) {
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
                    this.pushStack(new VarVariable("rt.%var(r1_depth)", VarVariable.Scope.LOCAL))
            );
        }

        public VarVariable popStack() {
            return new VarVariable("stack." + this.stackPointer().getAndDecrement(), VarVariable.Scope.LINE);
        }

        public static SetVarAction setLocal(int local, VarItem varItem) {
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

        public static VarVariable getLocal(int local) {
            return new VarVariable("local[%var(r_depth)][" + local + "]", VarVariable.Scope.LOCAL);
        }

        public static List<TemplateBlock> allocateMemory(String placeholderVariable) {
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

        public static TemplateBlock setReturnValue(VarItem varItem) {
            return new SetVarAction(
                    "=",
                    new Args(List.of(
                            new Args.Slot(new VarVariable("rt.%var(r_depth)", VarVariable.Scope.LOCAL), 0),
                            new Args.Slot(varItem, 1)
                    ))
            );
        }
    }
}
