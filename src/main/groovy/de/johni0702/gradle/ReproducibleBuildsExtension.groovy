package de.johni0702.gradle;

import java.util.List;

import org.gradle.api.Task;

class ReproducibleBuildsExtension {
    List<Task> repackAfter = []

    void repackAfter(Task task) {
        repackAfter << task
    }
}
