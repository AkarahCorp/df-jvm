package dev.akarah.codetemplate.template;

import dev.akarah.codetemplate.blocks.FunctionAction;

import java.util.ArrayList;
import java.util.List;

public class TemplateSplitter {
    public static List<GzippedCodeTemplateData> split(CodeTemplateData codeTemplateData) {
        var lists = new ArrayList<GzippedCodeTemplateData>();

        var currentList = new ArrayList<TemplateBlock>();
        for(var block : codeTemplateData.code().blocks()) {
            if(block instanceof FunctionAction functionAction) {
                if(!currentList.isEmpty()) {
                    lists.add(new CodeTemplateData(
                            "x",
                            "x",
                            "1",
                            new CodeTemplate(currentList)
                    ).gzip());
                }
                currentList = new ArrayList<>();
                currentList.add(functionAction);
            } else {
                currentList.add(block);
            }
        }

        if(!currentList.isEmpty()) {
            lists.add(new CodeTemplateData(
                    "x",
                    "x",
                    "1",
                    new CodeTemplate(currentList)
            ).gzip());
        }


        return lists;
    }
}
