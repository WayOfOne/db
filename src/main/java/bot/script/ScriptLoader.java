package bot.script;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Finds script jars in a directory and loads them, one {@link URLClassLoader}
 * per jar. The scripts directory itself is the catalog: every {@code *.jar}
 * carrying exactly one {@link ScriptManifest}-annotated {@link Script}
 * implementation becomes runnable. Jars that fail load are skipped with the
 * failure recorded, never aborting the scan. */
public final class ScriptLoader implements AutoCloseable {

    /** One successfully loaded script jar. */
    public static final class LoadedScript {
        private final File jar;
        private final URLClassLoader loader;
        private final Class<? extends Script> type;
        private final ScriptManifest manifest;

        LoadedScript(File jar, URLClassLoader loader, Class<? extends Script> type) {
            this.jar = jar;
            this.loader = loader;
            this.type = type;
            this.manifest = type.getAnnotation(ScriptManifest.class);
        }

        public File jar() {
            return jar;
        }

        public ScriptManifest manifest() {
            return manifest;
        }

        public Script newInstance() throws ReflectiveOperationException {
            return type.getDeclaredConstructor().newInstance();
        }

        void close() throws IOException {
            loader.close();
        }
    }

    private final List<LoadedScript> loaded = new ArrayList<>();
    private final List<String> failures = new ArrayList<>();

    /** Scan {@code dir} for {@code *.jar} and load every valid script. */
    public List<LoadedScript> loadAll(File dir) {
        File[] jars = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (jars == null) {
            failures.add(dir + ": not a readable directory");
            return List.copyOf(loaded);
        }
        for (File jar : jars) {
            try {
                loadOne(jar);
            } catch (Exception e) {
                failures.add(jar.getName() + ": " + e);
            }
        }
        return List.copyOf(loaded);
    }

    private void loadOne(File jar) throws IOException {
        URLClassLoader ucl = new URLClassLoader(
            new URL[] { jar.toURI().toURL() }, ScriptLoader.class.getClassLoader());
        boolean claimed = false;
        try (JarFile jf = new JarFile(jar)) {
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                String name = e.getName();
                if (!name.endsWith(".class") || name.contains("$")) {
                    continue;
                }
                String binary = name.substring(0, name.length() - 6).replace('/', '.');
                Class<?> c;
                try {
                    // No initialization: manifest inspection must be side-effect free.
                    c = Class.forName(binary, false, ucl);
                } catch (ClassNotFoundException | NoClassDefFoundError | ExceptionInInitializerError ex) {
                    continue;
                }
                if (Script.class.isAssignableFrom(c)
                    && c.isAnnotationPresent(ScriptManifest.class)) {
                    if (claimed) {
                        throw new IllegalStateException("jar holds more than one @ScriptManifest script");
                    }
                    loaded.add(new LoadedScript(jar, ucl, c.asSubclass(Script.class)));
                    claimed = true;
                }
            }
        }
        if (!claimed) {
            ucl.close();
            throw new IllegalStateException("jar holds no @ScriptManifest script");
        }
    }

    public List<String> failures() {
        return List.copyOf(failures);
    }

    @Override
    public void close() throws IOException {
        IOException first = null;
        for (LoadedScript s : loaded) {
            try {
                s.close();
            } catch (IOException e) {
                if (first == null) {
                    first = e;
                }
            }
        }
        if (first != null) {
            throw first;
        }
    }
}
