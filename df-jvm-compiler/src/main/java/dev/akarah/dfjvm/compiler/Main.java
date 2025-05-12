package dev.akarah.dfjvm.compiler;

import dev.akarah.codetemplate.codeclient.CodeClientSend;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.template.GzippedCodeTemplateData;
import dev.akarah.dfjvm.compiler.compilation.ActionRegistry;
import dev.akarah.dfjvm.compiler.compilation.ClassCompiler;
import dev.akarah.dfjvm.compiler.compilation.ClassData;
import dev.akarah.dfjvm.compiler.compilation.GenerateEvents;
import dev.akarah.dfjvm.compiler.io.ClassFileFinder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length == 0) {
            System.out.println("Provide a jar file to compile!");
            return;
        }

        var jarPath = Paths.get(args[0]);
        var models = ClassFileFinder.findAllClassFiles(jarPath);

        System.out.println(models);

        var data = ClassData.forClassModels(models);

        var actionRegistry = ActionRegistry.create().withPlayerActions();

        var classCompiler = ClassCompiler.forData(data).withActionRegistry(actionRegistry);

        var templates = new ArrayList<CodeTemplateData>();
        models.forEach(model -> templates.addAll(classCompiler.templatesForClassModel(model)));

        System.out.println(templates);

        var cc = CodeClientSend.of();
        for(var template : templates) {
            System.out.println(template.toFlatJson());
            cc.push(template.gzip());
        }

        for(var event : GenerateEvents.allPlayerEvents()) {
            cc.push(event);
        }
        cc.finish();
    }


}