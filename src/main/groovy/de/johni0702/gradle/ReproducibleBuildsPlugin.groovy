package de.johni0702.gradle;

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

class ReproducibleBuildsPlugin implements Plugin<Project> {
    // Why can we not just use 0?
    // This is why: https://github.com/gradle/gradle/blob/e411cc70f9645138232b427ed63159d7cbc00523/subprojects/core/src/main/java/org/gradle/api/internal/file/archive/ZipCopyAction.java#L42-L56
    private static final long CONSTANT_TIME_FOR_ZIP_ENTRIES = new GregorianCalendar(1980, Calendar.FEBRUARY, 1, 0, 0, 0).timeInMillis

    void apply(Project project) {
        project.extensions.create("reproducibleBuilds", ReproducibleBuildsExtension)

        project.tasks.all { if (it.path == ':shadowJar') project.reproducibleBuilds.repackAfter it }

        project.afterEvaluate {
            project.tasks.withType(AbstractArchiveTask) {
                preserveFileTimestamps = false
                reproducibleFileOrder = true
                dirMode = 0775
                fileMode = 0664
            }

            project.reproducibleBuilds.repackAfter.each { task ->
                task.doLast {
                    task.outputs.files.filter { it.name.endsWith '.jar' }.files.each { stripJar it }
                }
            }
        }
    }

    void stripJar(File file) {
        File newFile = new File(file.parent, 'tmp-' + file.name)
        newFile.withOutputStream { fout ->
            JarOutputStream out = new JarOutputStream(fout)
            JarFile jf = new JarFile(file)
            jf.entries().unique {it.name}.sort {it.name}.each {
                def copy = new JarEntry(it.name)
                copy.time = CONSTANT_TIME_FOR_ZIP_ENTRIES
                out.putNextEntry(copy)
                out << jf.getInputStream(it)
            }
            out.finish()
        }
        newFile.renameTo file
    }
}
