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

public class Main {
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

        System.out.println(pipeline.render());
    }
}