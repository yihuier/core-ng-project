package core.ext.mongo.migration.util;

import core.framework.util.Lists;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;

/**
 * @author Neal
 */
public class ClassUtils {

    public static List<Class<?>> getClassesWithAnnotation(String packageName, Class<? extends Annotation> annotation) throws ClassNotFoundException {
        List<Class<?>> classes = Lists.newArrayList();
        URL root = Thread.currentThread().getContextClassLoader().getResource(packageName.replace('.', '/'));
        File[] files = new File(root.getFile()).listFiles((dir, name) -> name.endsWith(".class"));
        if (files == null) return List.of();
        for (File file : files) {
            String className = file.getName().replaceAll(".class$", "");
            Class<?> cls = Class.forName(packageName + "." + className);
            if (cls.isAnnotationPresent(annotation)) {
                classes.add(cls);
            }
        }
        return classes;
    }
}
