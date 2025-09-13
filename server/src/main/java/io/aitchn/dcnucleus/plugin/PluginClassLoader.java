package io.aitchn.dcnucleus.plugin;

import java.net.URL;
import java.net.URLClassLoader;

public class PluginClassLoader extends URLClassLoader {

    private static final String[] PARENT_FIRST_PACKAGES = {
            "java.",
            "javax.",
            "sun.",
            "jdk.",
            "com.sun.",
            "io.aitchn.dcnucleus.api.",  // API 包必須共享
            "org.slf4j.",               // SLF4J 必須共享
            "ch.qos.logback.",          // Logback 必須共享
    };

    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass != null) {
                if (resolve) resolveClass(loadedClass);
                return loadedClass;
            }

            if (isParentFirstPackage(name)) {
                Class<?> parentClass = getParent().loadClass(name);
                if (resolve) resolveClass(parentClass);
                return parentClass;
            }

            try {
                Class<?> pluginClass = findClass(name);
                if (resolve) resolveClass(pluginClass);
                return pluginClass;
            } catch (ClassNotFoundException e) {
                return super.loadClass(name, resolve);
            }
        }
    }

    private boolean isParentFirstPackage(String className) {
        for (String prefix : PARENT_FIRST_PACKAGES) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}
