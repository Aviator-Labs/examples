package org.aviatorlabs.ci;


import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.resource.registry.RegistryImageResourceType;
import org.aviatorlabs.ci.sdk.Pipeline;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.*;

public class Main {
    public static void main(String[] args) {
        Pipeline pipeline = new Pipeline();

        Job createAndConsume = new Job("create-and-consume").markPublic();

        Command makeFileCommand = Command.createCommand("sh")
                .addArg("-exc")
                .addArg("ls -la; echo \"Created a file on $(date)\" > ./files/created_file");

        AnonymousResource<RegistryImageConfig> busyBox = AnonymousResource.create(
                RegistryImageResourceType.create(),
                RegistryImageConfig.create("busybox")
        );

        Output filesOutput = Output.create("files");

        TaskConfig makeFileConfig = TaskConfig.create(Platform.LINUX, busyBox, makeFileCommand).addOutput(filesOutput);

        createAndConsume.addStep(Task.create("make-a-file", makeFileConfig));

        Command consumeFileCommand = Command.createCommand("cat").addArg("./files/created_file");

        TaskConfig consumeFileConfig = TaskConfig.create(Platform.LINUX, busyBox, consumeFileCommand).addInput(Input.create(filesOutput));

        createAndConsume.addStep(Task.create("consume-the-file", consumeFileConfig));

        pipeline.addJob(createAndConsume);

        System.out.println(pipeline.render());
    }
}