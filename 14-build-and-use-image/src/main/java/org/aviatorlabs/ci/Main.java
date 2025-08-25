package org.aviatorlabs.ci;

import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.GitResourceConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResourceType;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.*;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        GitResourceConfig repoConfig = GitResourceConfig.create("https://github.com/concourse/examples.git", "master");
        Resource repo = GitResource.create("concourse-examples", repoConfig).setIcon("github");
        pipeline.addResource(repo);

        Job job = new Job("build-and-use-image").addStep(repo.createGetDefinition());

        AnonymousResource<RegistryImageConfig> oci = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("concourse/oci-build-task")
        );

        Output buildImageOutput = Output.create("image");
        TaskConfig buildConfig = TaskConfig.create(Platform.LINUX, oci, Command.createCommand("build"))
                .addInput(Input.create(repo.createGetDefinition()))
                .addOutput(buildImageOutput)
                .addParam("CONTEXT", "concourse-examples/Dockerfiles/simple")
                .addParam("UNPACK_ROOTFS", "true");

        Task build = Task.create("build-task-image", buildConfig).markPrivileged();

        TaskConfig useConfig = TaskConfig.create(Platform.LINUX, Command.createCommand("cat").addArg("/stranger"));

        Task use = Task.create("use-built-image-in-task", useConfig).setImage(buildImageOutput);

        job.addStep(build).addStep(use);

        pipeline.addJob(job);

        System.out.println(pipeline);
    }
}