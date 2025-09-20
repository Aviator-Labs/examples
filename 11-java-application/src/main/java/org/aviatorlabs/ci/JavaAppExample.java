package org.aviatorlabs.ci;


import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.GitResourceConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResourceType;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.BuildLogRetentionPolicy;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.Command;
import org.aviatorlabs.ci.sdk.step.task.config.Input;
import org.aviatorlabs.ci.sdk.step.task.config.Platform;
import org.aviatorlabs.ci.sdk.step.task.config.TaskConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class JavaAppExample {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        GitResourceConfig repoConfig = GitResourceConfig.create("https://github.com/apache/kafka.git");
        Resource repo = GitResource.create("apache-kafka", repoConfig).setIcon("github");
        pipeline.addResource(repo);

        // Task Config
        AnonymousResource<RegistryImageConfig> gradle = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("gradle", "jdk17")
        );
        String javaTesting = """
                cd apache-kafka
                
                ./gradlew clients:test --tests RequestResponseTest
                """;
        Command testingCommand = Command.createCommand("/bin/sh")
                .addArg("-ce")
                .addArg(javaTesting)
                .setUser("root");

        TaskConfig config = TaskConfig.create(Platform.LINUX, gradle, testingCommand)
                .addInput(Input.create(repo.createGetDefinition()))
                .addCache("$HOME/.m2/repository")
                .addCache("$HOME/.gradle/caches/")
                .addCache("$HOME/.gradle/wrapper/");

        Task task = Task.create("run-tests", config);

        // Job Config
        Job job = new Job("test")
                .markPublic()
                .addStep(repo.createGetDefinition().enableTrigger())
                .addStep(task)
                .setBuildLogRetention(BuildLogRetentionPolicy.create().setBuilds(50));

        pipeline.addJob(job);

        try {
            pipeline.render(setPipelineOutput("generated"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String setPipelineOutput(String pipelineName) {
        try {
            Path parent = Paths.get(Objects.requireNonNull(JavaAppExample.class.getResource("/")).toURI()).getParent();

            return "%s/generated-pipelines/%s.json".formatted(parent, pipelineName);
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
    }
}