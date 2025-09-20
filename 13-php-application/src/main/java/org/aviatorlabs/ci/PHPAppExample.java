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

public class PHPAppExample {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        GitResourceConfig repoConfig = GitResourceConfig.create("https://github.com/laravel/laravel.git");
        Resource repo = GitResource.create("laravel-git", repoConfig).setIcon("github");
        pipeline.addResource(repo);

        // Task Config
        String phpTest = """
                cd laravel-git

                composer install

                cp .env.example .env
                php artisan key:generate

                vendor/bin/phpunit
                """;
        Command command = Command.createCommand("/bin/sh")
                .addArg("-ce")
                .addArg(phpTest);
        AnonymousResource<RegistryImageConfig> resource = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("composer")
        );
        TaskConfig config = TaskConfig.create(Platform.LINUX, resource, command)
                .addInput(Input.create(repo.createGetDefinition()));
        Task task = Task.create("run-tests", config);

        // Job Config
        Job job = new Job("test").markPublic()
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
            Path parent = Paths.get(Objects.requireNonNull(PHPAppExample.class.getResource("/")).toURI()).getParent();

            return "%s/generated-pipelines/%s.json".formatted(parent, pipelineName);
        } catch (Exception e) {
            throw new IllegalArgumentException("");
        }
    }
}