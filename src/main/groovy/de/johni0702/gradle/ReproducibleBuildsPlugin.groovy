package de.johni0702.gradle;

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

class ReproducibleBuildsPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("reproducibleBuilds", ReproducibleBuildsExtension)

        def shadowJar = project.tasks.findByPath ':shadowJar'
        if (shadowJar != null) project.reproducibleBuilds.repackAfter shadowJar

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
                copy.time = 0
                out.putNextEntry(copy)
                out << jf.getInputStream(it)
            }
            out.finish()
        }
        newFile.renameTo file
    }
}
