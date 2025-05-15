package dev.akarah.dfjvm.compiler.compilation;

import dev.akarah.codetemplate.blocks.*;
import dev.akarah.codetemplate.blocks.types.Args;
import dev.akarah.codetemplate.template.CodeTemplate;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.template.TemplateBlock;
import dev.akarah.codetemplate.varitem.*;
import dev.akarah.dfjvm.compiler.compilation.info.ClassData;
import dev.akarah.dfjvm.compiler.compilation.info.CompilerPoint;
import dev.akarah.dfjvm.compiler.compilation.info.StackInfo;
import dev.akarah.dfjvm.compiler.compilation.util.CodeHelper;

import java.lang.classfile.*;
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
        blocks.add(CodeHelper.beginFunction(point.functionName(), point.getCopiedLocalParameters().toList(), "False"));
        codeModel.forEach(codeElement -> blocks.addAll(compileCodeElement(codeElement, point, si)));
        return blocks;
    }

    public List<TemplateBlock> compileCodeElement(CodeElement codeElement, CompilerPoint point, StackInfo stackInfo) {
        return switch (codeElement) {
            case Instruction instruction -> compileInstruction(instruction, point, stackInfo, false);
            case PseudoInstruction pseudoInstruction -> switch (pseudoInstruction) {
                case LabelTarget labelTarget -> {
                    var bci = point.labelToBci(labelTarget.label());
                    yield List.of(
                            CodeHelper.callFunction(
                                    point.functionName(bci),
                                    point.getAllLocals().map(x -> (VarItem) x).toList()
                            ),
                            CodeHelper.beginFunction(
                                    point.functionName(bci),
                                    point.getReferencedLocalParameters().toList(),
                                    "True"
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
                    Args.of(
                        new VarString(instruction.toString())
                    )
            ));
            return list;
        } else {
            return out;
        }
    }

    public List<TemplateBlock> compileInstruction(Instruction instruction, CompilerPoint point, StackInfo stackInfo) {
        return switch (instruction) {
            case NewObjectInstruction newObjectInstruction -> generateBranch(newObjectInstruction, point, stackInfo);
            case BranchInstruction branchInstruction -> generateBranch(branchInstruction, point, stackInfo);
            case FieldInstruction fieldInstruction -> generateField(fieldInstruction, point, stackInfo);
            case ConstantInstruction constantInstruction -> generateConstant(constantInstruction, point, stackInfo);
            case InvokeInstruction invokeInstruction -> generateInvoke(invokeInstruction, point, stackInfo);
            case LoadInstruction loadInstruction -> List.of(stackInfo.pushStack(CodeHelper.getLocal(loadInstruction.slot())));
            case ReturnInstruction returnInstruction -> generateReturn(returnInstruction, point, stackInfo);
            case StoreInstruction storeInstruction -> generateStore(storeInstruction, point, stackInfo);
            case IncrementInstruction incrementInstruction -> generateIncrement(incrementInstruction, point, stackInfo);
            case OperatorInstruction operatorInstruction -> generateOperator(operatorInstruction, point, stackInfo);
            case StackInstruction stackInstruction -> generateStack(stackInstruction, point, stackInfo);
            case NewPrimitiveArrayInstruction newPrimitiveArrayInstruction ->
                    generatePrimitiveArray(newPrimitiveArrayInstruction, point, stackInfo);
            case ArrayLoadInstruction arrayLoadInstruction -> generateArrayLoad(arrayLoadInstruction, point, stackInfo);
            case ArrayStoreInstruction arrayStoreInstruction -> generateArrayStore(arrayStoreInstruction, point, stackInfo);
            case ConvertInstruction convertInstruction -> List.of();
            case InvokeDynamicInstruction invokeDynamicInstruction -> {
                System.out.println("warning: InvokeDynamic is being skipped. only continue with build if you are confident it is OK");
                yield List.of();
            }
            default -> throw new RuntimeException("unknwon instruction " + instruction);
        };
    }


    public List<TemplateBlock> generateBranch(NewObjectInstruction newObjectInstruction, CompilerPoint point, StackInfo stackInfo) {
        var blocks = new ArrayList<>(CodeHelper.allocateMemory("tmp"));
        blocks.add(stackInfo.pushStack(new VarString("%var(tmp)")));
        return blocks;
    }

    public List<TemplateBlock> generateBranch(BranchInstruction branchInstruction, CompilerPoint point, StackInfo stackInfo) {
        var blocks = new ArrayList<TemplateBlock>();

        VarItem value1 = null;
        VarItem value2 = null;

        switch (branchInstruction.opcode()) {
            case IF_ICMPEQ, IF_ICMPNE, IF_ICMPGT, IF_ICMPLT, IF_ICMPGE, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE -> {
                value2 = stackInfo.popStack();
                value1 = stackInfo.popStack();
            }
            case IFEQ, IFNE, IFLE, IFLT, IFGE, IFGT -> {
                value2 = new VarNumber("0");
                value1 = stackInfo.popStack();
            }
            case IFNULL, IFNONNULL -> {
                value1 = stackInfo.popStack();
                value2 = new VarString("ref@null");
            }
        }

        var blockAdded = true;
        switch (branchInstruction.opcode()) {
            case IF_ICMPEQ, IF_ACMPEQ, IFNULL, IFEQ -> blocks.add(new IfVarAction("=", Args.of(value1, value2)));
            case IF_ICMPNE, IF_ACMPNE, IFNONNULL, IFNE -> blocks.add(new IfVarAction("!=", Args.of(value1, value2)));
            case IF_ICMPLT, IFLT -> blocks.add(new IfVarAction("<", Args.of(value1, value2)));
            case IF_ICMPGT, IFGT -> blocks.add(new IfVarAction(">", Args.of(value1, value2)));
            case IF_ICMPLE, IFLE -> blocks.add(new IfVarAction("<=", Args.of(value1, value2)));
            case IF_ICMPGE, IFGE -> blocks.add(new IfVarAction(">=", Args.of(value1, value2)));
            case GOTO, GOTO_W -> blockAdded = false;
            default -> throw new RuntimeException("unknown opcode " + branchInstruction.opcode());
        }

        if(blockAdded)
            blocks.add(new Bracket(Bracket.Direction.OPEN, Bracket.Type.NORMAL));

        var targetLabel = branchInstruction.target();
        var name = point.functionName(point.labelToBci(targetLabel));
        blocks.add(CodeHelper.callFunction(name, point.getAllLocals().map(x -> (VarItem) x).toList()));
        blocks.add(new ControlAction("Return", Args.empty()));

        if(blockAdded)
            blocks.add(new Bracket(Bracket.Direction.CLOSE, Bracket.Type.NORMAL));

        return blocks;
    }

    public List<TemplateBlock> generateField(FieldInstruction fieldInstruction, CompilerPoint point, StackInfo stackInfo) {
        return switch (fieldInstruction.opcode()) {
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
                                Args.of(
                                        new VarVariable("memory/%var(" + objectRef.name() + ")." + fieldInstruction.field().name().stringValue(), VarVariable.Scope.GAME),
                                        newValue
                                )
                        )
                );
            }
            case GETSTATIC -> List.of(
                    stackInfo.pushStack(new VarVariable(
                            "static/"
                                    + fieldInstruction.field().owner().asInternalName()
                                    + "#" + fieldInstruction.field().name().stringValue(), VarVariable.Scope.GAME))
            );
            case PUTSTATIC -> List.of(
                    new SetVarAction(
                            "=",
                            Args.of(
                                    new VarVariable(
                                            "static/"
                                                    + fieldInstruction.field().owner().asInternalName()
                                                    + "#" + fieldInstruction.field().name().stringValue(), VarVariable.Scope.GAME
                                    ),
                                    stackInfo.popStack()
                            )
                    )
            );
            default -> throw new RuntimeException("uh not supported " + fieldInstruction.opcode() + " byebye");
        };
    }

    public List<TemplateBlock> generateConstant(ConstantInstruction constantInstruction, CompilerPoint point, StackInfo stackInfo) {
        return switch (constantInstruction.constantValue()) {
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
            default -> {
                if (constantInstruction.opcode() == Opcode.ACONST_NULL) {
                    yield List.of(stackInfo.pushStack(new VarString("ref@null")));
                }
                throw new RuntimeException("unsupported constant " + constantInstruction.constantValue());
            }
        };
    }


    public List<TemplateBlock> generateInvoke(InvokeInstruction invokeInstruction, CompilerPoint point, StackInfo stackInfo) {


        var functionName = invokeInstruction.method().owner().asInternalName()
                + "#" + invokeInstruction.method().name()
                + invokeInstruction.typeSymbol().descriptorString();

        var isCustomFunction = this.actionRegistry.actionSuppliers.containsKey(functionName);

        if(functionName.equals("java/lang/Object#<init>()V")) {
            return List.of();
        }

        var instructions = new ArrayList<TemplateBlock>();

        // stackParameters is needed since StackInfo#popStack pops values off the stack in the wrong order
        // this array stores the values so we can reference them in the correct order through List#removeLast
        List<VarItem> stackParameters = new ArrayList<>();
        for(var idx = 0; idx < invokeInstruction.typeSymbol().parameterCount(); idx++) {
            stackParameters.add(stackInfo.popStack());
        }

        List<VarItem> parameters = new ArrayList<>();
        for(var idx = 0; idx < invokeInstruction.typeSymbol().parameterCount(); idx++) {
            var parameterSymbol = invokeInstruction.typeSymbol().parameterList().get(idx);
            parameters.add(stackParameters.removeLast());
            if(parameterSymbol.descriptorString().equals("D") || parameterSymbol.descriptorString().equals("L")) {
                parameters.add(new VarString("unused"));
            }
        }

        if(invokeInstruction.opcode() != Opcode.INVOKESTATIC) {
            parameters.addFirst(stackInfo.popStack());
        }

        System.out.println("fn: " + functionName + ", params: " + parameters);
        if(isCustomFunction) {
            instructions.addAll(this.actionRegistry.actionSuppliers.get(functionName).apply(parameters));
        } else {
            instructions.add(CodeHelper.callFunction(functionName, parameters));
        }


        if(!invokeInstruction.typeSymbol().returnType().descriptorString().equals("V")) {
            instructions.addAll(stackInfo.pushReturnValue());
        }
        return instructions;
    }

    public List<TemplateBlock> generateReturn(ReturnInstruction returnInstruction, CompilerPoint point, StackInfo stackInfo) {
        return List.of(
                new SetVarAction(
                        "=",
                        Args.of(
                                new VarVariable("returned", VarVariable.Scope.LINE),
                                stackInfo.popStack()
                        )
                ),
                new ControlAction("Return", Args.empty())
        );
    }

    public List<TemplateBlock> generateStore(StoreInstruction storeInstruction, CompilerPoint point, StackInfo stackInfo) {
        return List.of(
                CodeHelper.setLocal(storeInstruction.slot(), stackInfo.popStack())
        );
    }

    public List<TemplateBlock> generateIncrement(IncrementInstruction incrementInstruction, CompilerPoint point, StackInfo stackInfo) {
        return List.of(
                new SetVarAction(
                        "+=",
                        Args.of(
                                new VarVariable("local." + incrementInstruction.slot(), VarVariable.Scope.LINE),
                                new VarNumber(String.valueOf(incrementInstruction.constant()))
                        )
                )
        );
    }

    public List<TemplateBlock> generateOperator(OperatorInstruction operatorInstruction, CompilerPoint point, StackInfo stackInfo) {
        Function<String, List<TemplateBlock>> binaryOp = action -> List.of(
                new SetVarAction(
                        action,
                        Args.of(
                                new VarVariable("tmp", VarVariable.Scope.LINE),
                                stackInfo.popStack(),
                                stackInfo.popStack()
                        )
                ),
                stackInfo.pushStack(new VarVariable("tmp", VarVariable.Scope.LINE))
        );

        return switch (operatorInstruction.opcode()) {
            case Opcode.IADD, Opcode.LADD, Opcode.FADD, Opcode.DADD -> binaryOp.apply("+");
            case Opcode.ISUB, Opcode.LSUB, Opcode.FSUB, Opcode.DSUB -> binaryOp.apply("-");
            case Opcode.IMUL, Opcode.LMUL, Opcode.FMUL, Opcode.DMUL -> binaryOp.apply("*");
            case Opcode.IDIV, Opcode.LDIV, Opcode.FDIV, Opcode.DDIV -> binaryOp.apply("/");
            case Opcode.IREM, Opcode.LREM, Opcode.FREM, Opcode.DREM -> binaryOp.apply("%");
            default -> List.of();
        };
    }

    public List<TemplateBlock> generateStack(StackInstruction stackInstruction, CompilerPoint point, StackInfo stackInfo) {
        return switch (stackInstruction.opcode()) {
            case DUP -> List.of(
                    new SetVarAction(
                            "=",
                            Args.of(
                                    new VarVariable("tmp", VarVariable.Scope.LINE),
                                    stackInfo.popStack()
                            )
                    ),
                    stackInfo.pushStack(new VarVariable("tmp", VarVariable.Scope.LINE)),
                    stackInfo.pushStack(new VarVariable("tmp", VarVariable.Scope.LINE))
            );
            default -> throw new RuntimeException("idk " + stackInstruction);
        };
    }

    public List<TemplateBlock> generatePrimitiveArray(NewPrimitiveArrayInstruction newPrimitiveArrayInstruction, CompilerPoint point, StackInfo stackInfo) {
        var count = stackInfo.popStack();
        var blocks = new ArrayList<>(CodeHelper.allocateMemory("array_ptr_tmp"));
        blocks.add(stackInfo.pushStack(new VarString("%var(array_ptr_tmp)")));
        return blocks;
    }

    public List<TemplateBlock> generateArrayLoad(ArrayLoadInstruction arrayLoadInstruction, CompilerPoint point, StackInfo stackInfo) {
        var index = stackInfo.popStack();
        var arrayRef = stackInfo.popStack();

        return List.of(
                stackInfo.pushStack(new VarVariable("memory/%var(" + arrayRef.name() + ")[%var(" + index.name() + ")]", VarVariable.Scope.GAME))
        );
    }

    public List<TemplateBlock> generateArrayStore(ArrayStoreInstruction arrayStoreInstruction, CompilerPoint point, StackInfo stackInfo) {
        var value = stackInfo.popStack();
        var index = stackInfo.popStack();
        var arrayRef = stackInfo.popStack();

        return List.of(
                new SetVarAction(
                        "=",
                        Args.of(
                                new VarVariable("memory/%var(" +  arrayRef.name() + ")[%var(" + index.name() + ")]", VarVariable.Scope.GAME),
                                value
                        )
                )
        );
    }
}
