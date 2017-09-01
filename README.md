## Gradle Reproducible Builds Plugin
This plugin configures Gradle to produce byte-for-byte reproducible jar files.
Its main purpose is to be used as a quick plug'n'play way of getting simple Gradle builds to produce deterministic results.

This is achieved by configuring any archive tasks (e.g. the jar task) in such a way that they:
- remove all file timestamps by setting [preserveFileTimestamps](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.bundling.AbstractArchiveTask.html#org.gradle.api.tasks.bundling.AbstractArchiveTask:preserveFileTimestamps) = false
- package all files in a reproducible order by setting [reproducibleFileOrder](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.bundling.AbstractArchiveTask.html#org.gradle.api.tasks.bundling.AbstractArchiveTask:reproducibleFileOrder) = true
- setting [dirMode](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.bundling.AbstractArchiveTask.html#org.gradle.api.tasks.bundling.AbstractArchiveTask:dirMode) = 0775 and [fileMode](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.bundling.AbstractArchiveTask.html#org.gradle.api.tasks.bundling.AbstractArchiveTask:fileMode) = 0664

and manually re-packaging the jar after tasks which don't support the above.

### Usage
```groovy
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath 'com.github.johni0702:gradle-reproducible-builds-plugin:master-SNAPSHOT'
        // "master-SNAPSHOT" is the latest version and should be replaced with a specific commit hash
    }
}

apply plugin: 'java' // or anything else that produces archives
...
apply plugin: 'de.johni0702.reproducible-builds'

// Optionally configure tasks whose output should be re-packaged manually
reproducibleBuilds {
    repackAfter task(':shadowJar')
    // Note that the shadowJar task is included by default and doesn't have to be specified explicitly
    repackAfter task(':myCustomTask')
    repackAfter task(':myOtherTask')
    // Also note that re-packaging happens after every specified task, so adding the last one might be sufficient
}
