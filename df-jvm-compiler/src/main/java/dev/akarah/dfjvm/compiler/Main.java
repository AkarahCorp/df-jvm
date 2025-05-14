package dev.akarah.dfjvm.compiler;

import dev.akarah.codetemplate.codeclient.CodeClientSend;
import dev.akarah.codetemplate.template.CodeTemplateData;
import dev.akarah.codetemplate.template.TemplateSplitter;
import dev.akarah.dfjvm.compiler.compilation.*;
import dev.akarah.dfjvm.compiler.io.ClassFileFinder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;

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

        var actionRegistry = ActionRegistry.create()
                .withPlayerActions()
                .withJavaMethods();

        var classCompiler = ClassCompiler.forData(data).withActionRegistry(actionRegistry);

        var templates = models
                .stream()
                .map(classCompiler::templatesForClassModel)
                .flatMap(Collection::stream)
                .map(TemplateSplitter::split)
                .flatMap(Collection::stream)
                .filter(template -> CodeHelper.templateIsLoaded(template, data))
                .map(CodeTemplateData::gzip)
                .toList();


        var cc = CodeClientSend.of();
        for(var template : templates) {
            cc.push(template);
        }

        for(var event : GenerateEvents.allPlayerEvents(data)) {
            cc.push(event);
        }

        System.out.println(data.getClassLoadingOrder());
        cc.finish();
    }


}