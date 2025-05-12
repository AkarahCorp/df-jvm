package dev.akarah.dfjvm.compiler;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    @SuppressWarnings("unchecked")
    public static <T> List<T> join(List<T>... lists) {
        var list = new ArrayList<T>();
        for(var sublist : lists) {
            list.addAll(sublist);
        }
        return list;
    }
}
