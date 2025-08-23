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

        Resource concourseDocs = GitResource.create("concourse-docs-git", GitResourceConfig.create("https://github.com/concourse/docs")).setIcon("github");
        pipeline.addResource(concourseDocs);

        Job job = new Job("job").markPublic();

        AnonymousResource<RegistryImageConfig> busyBox = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("busybox")
        );

        Command command = Command.createCommand("ls")
                .addArg("-la")
                .addArg("./concourse-docs-git");

        TaskConfig config = TaskConfig.create(Platform.LINUX, busyBox, command);
        Task simpleTask = Task.create("list-files", config);

        simpleTask.getConfig().addInput(Input.create(concourseDocs.createGetDefinition()));

        job.addStep(concourseDocs.createGetDefinition().enableTrigger())
                .addStep(simpleTask)
                .setBuildLogRetention(BuildLogRetentionPolicy.create().setBuilds(50));

        pipeline.addJob(job);

        System.out.println(pipeline.render());
    }
}