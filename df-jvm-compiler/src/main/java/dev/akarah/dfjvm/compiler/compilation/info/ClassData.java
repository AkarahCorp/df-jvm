package dev.akarah.dfjvm.compiler.compilation.info;


import java.lang.classfile.ClassModel;
import java.lang.classfile.CustomAttribute;
import java.lang.classfile.Instruction;
import java.lang.classfile.PseudoInstruction;
import java.lang.classfile.attribute.CodeAttribute;
import java.lang.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import java.lang.classfile.attribute.StackMapTableAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.util.*;

public class ClassData {
    Map<String, ClassModel> classModels = new HashMap<>();

    List<String> classLoadingOrder = new ArrayList<>();

    public static ClassData forClassModels(List<ClassModel> classModelsList) {
        var cc = new ClassData();
        classModelsList.forEach(model -> cc.classModels.put(model.thisClass().asInternalName(), model));
        return cc;
    }

    public List<String> getClassLoadingOrder() {
        if(this.classLoadingOrder.isEmpty()) {
            findClassLoadingOrder();
        }

        return this.classLoadingOrder;
    }

    public void findClassLoadingOrder() {
        if(!classModels.containsKey("df/Events")) {
            throw new UnsupportedOperationException("no df/Events class present, plz fix!");
        }

        this.classLoadingOrder.addAll(findAssociatedClasses("df/Events"));
    }

    public Set<String> findAssociatedClasses(String clazz) {
        var associatedClasses = new HashSet<String>();
        associatedClasses.add(clazz);
        var classModel = this.classModels.get(clazz);
        if(classModel == null) {
            return Set.of();
        }

        classModel.constantPool()
                .forEach(entry -> {
                    if(entry instanceof ClassEntry classEntry) {
                        var str = classEntry.asInternalName();
                        if(!associatedClasses.contains(str)) {
                            associatedClasses.addAll(findAssociatedClasses(str));
                        }
                    }
                });

        classModel.fields()
                .forEach(field -> {
                    var str = field.fieldTypeSymbol().displayName().replace(".", "/");
                    if(field.fieldTypeSymbol().isClassOrInterface()) {
                        if(!associatedClasses.contains(str)) {
                            associatedClasses.addAll(findAssociatedClasses(str));
                        }
                    }
                });

        classModel.interfaces().forEach(itf -> {
            var str = itf.asInternalName();
            if(!associatedClasses.contains(str)) {
                associatedClasses.addAll(findAssociatedClasses(str));
            }
        });

        classModel.attributes().stream()
                .filter(a -> a instanceof CodeAttribute)
                .flatMap(a -> ((CodeAttribute)a).elementStream())
                .forEach(elem -> {
                    switch (elem) {
                        case CustomAttribute customAttribute -> {
                        }
                        case Instruction instruction -> {
                            switch (instruction) {
                                default -> {}
                            }
                        }
                        case PseudoInstruction pseudoInstruction -> {
                        }
                        case RuntimeInvisibleTypeAnnotationsAttribute runtimeInvisibleTypeAnnotationsAttribute -> {
                        }
                        case RuntimeVisibleTypeAnnotationsAttribute runtimeVisibleTypeAnnotationsAttribute -> {
                        }
                        case StackMapTableAttribute stackMapTableAttribute -> {
                        }
                    }
                });


        return associatedClasses;
    }

    public Map<String, ClassModel> classModels() {
        return this.classModels;
    }
}
