package org.aviatorlabs.ci;


import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.GitResourceConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResource;
import org.aviatorlabs.ci.resource.registry.RegistryImageResourceType;
import org.aviatorlabs.ci.resource.registry.get.RegistryFormat;
import org.aviatorlabs.ci.resource.registry.put.RegistryPutConfig;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.*;
import org.aviatorlabs.ci.sdk.variable.Variable;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        GitResourceConfig repoConfig = GitResourceConfig.create("https://github.com/concourse/examples.git", "master");
        Resource repo = GitResource.create("concourse-examples", repoConfig).setIcon("github");
        pipeline.addResource(repo);

        RegistryImageConfig imageConfig = RegistryImageConfig.create(String.format("%s/simple-image", Variable.referenceVariable("image-repo-name")))
                .setCredentials(Variable.referenceVariable("registry-username"), Variable.referenceVariable("registry-password"));
        Resource image = RegistryImageResource.create("simple-image", imageConfig).setIcon("docker");
        pipeline.addResource(image);

        Job job = new Job("build-and-push").addStep(repo.createGetDefinition());

        AnonymousResource<RegistryImageConfig> oci = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("concourse/oci-build-task")
        );

        Output output = Output.create("image");
        TaskConfig buildConfig = TaskConfig.create(Platform.LINUX, oci, Command.createCommand("build"))
                .addInput(Input.create(repo.createGetDefinition()))
                .addOutput(output)
                .addParam("CONTEXT", "concourse-examples/Dockerfiles/simple")
                .addParam("UNPACK_ROOTFS", "true");

        Task build = Task.create("build-task-image", buildConfig).markPrivileged();

        job.addStep(build)
                .addStep(image.createPutDefinition().setParams(RegistryPutConfig.create(output, RegistryFormat.OCI)));

        pipeline.addJob(job);

        System.out.println(pipeline.render());
    }
}