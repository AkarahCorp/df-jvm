package dev.akarah.dfjvm.compiler.compilation;

import java.lang.classfile.ClassModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassData {
    Map<String, ClassModel> classModels = new HashMap<>();

    public static ClassData forClassModels(List<ClassModel> classModelsList) {
        var cc = new ClassData();
        classModelsList.forEach(model -> cc.classModels.put(model.thisClass().asInternalName(), model));
        return cc;
    }

    public Map<String, ClassModel> classModels() {
        return this.classModels;
    }
}
