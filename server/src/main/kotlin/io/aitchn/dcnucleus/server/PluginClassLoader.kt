package io.aitchn.dcnucleus.server

import java.net.URLClassLoader
import java.nio.file.Path

class PluginClassLoader(
    jar: Path,
    parent: ClassLoader
) : URLClassLoader(arrayOf(jar.toUri().toURL()), parent) {

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        synchronized(getClassLoadingLock(name)) {
            findLoadedClass(name)?.let { return it }
            try {
                val c = findClass(name)
                if (resolve) resolveClass(c)
                return c
            } catch (_: ClassNotFoundException) {
                // fallback to parent
            }
            return super.loadClass(name, resolve)
        }
    }
}