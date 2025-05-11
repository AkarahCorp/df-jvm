package dev.akarah.dfjvm.compiler.io;

import dev.akarah.codetemplate.template.GzippedCodeTemplateData;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassFileFinder {
    public static List<ClassModel> findAllClassFiles(Path jarPath) throws IOException {
        var classModels = new ArrayList<ClassModel>();

        try(var fs = FileSystems.newFileSystem(jarPath, Collections.emptyMap())) {
            fs.getRootDirectories()
                    .forEach(root -> {
                        try(var walk = Files.walk(root)) {
                            walk.forEach(path -> {
                                try {
                                    var classModel = ClassFile.of().parse(path);
                                    classModels.add(classModel);
                                } catch (Exception ignored) {

                                }
                            });
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        return classModels;
    }
}
