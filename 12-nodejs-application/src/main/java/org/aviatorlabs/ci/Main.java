package org.aviatorlabs.ci;


import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.GitResourceConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResource;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.BuildLogRetentionPolicy;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.*;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        GitResourceConfig repoConfig = GitResourceConfig.create("https://github.com/nodejs/nodejs.org.git");
        Resource repo = GitResource.create("repo", repoConfig).setIcon("github");
        pipeline.addResource(repo);

        RegistryImageConfig registryConfig = RegistryImageConfig.create("node", "22-slim");
        Resource image = RegistryImageResource.create("node-image", registryConfig).setIcon("docker");
        pipeline.addResource(image);

        // Task Config
        Output workspace = Output.create("workspace").setPath("repo");
        Command installCommand = Command.createCommand("npm")
                .addArg("ci")
                .addArg("--no-audit")
                .addArg("--no-fund")
                .setWorkingDirectory("repo");
        TaskConfig installConfig = TaskConfig.create(Platform.LINUX, installCommand)
                .addInput(Input.create(repo.createGetDefinition()))
                .addOutput(workspace);
        Task install = Task.create("install", installConfig);
        install.setImage(image.createGetDefinition());

        Command testCommand = Command.createCommand("npm")
                .addArg("run")
                .addArg("test")
                .setWorkingDirectory("repo");
        TaskConfig testConfig = TaskConfig.create(Platform.LINUX, testCommand)
                .addInput(Input.create(workspace).setPath("repo"));
        Task test = Task.create("test", testConfig);
        test.setImage(image.createGetDefinition());

        // Job Config
        Job job = new Job("test")
                .markPublic()
                .addStep(image.createGetDefinition())
                .addStep(repo.createGetDefinition().enableTrigger())
                .addStep(install)
                .addStep(test)
                .setBuildLogRetention(BuildLogRetentionPolicy.create().setBuilds(50));

        pipeline.addJob(job);

        System.out.println(pipeline.render());
    }
}