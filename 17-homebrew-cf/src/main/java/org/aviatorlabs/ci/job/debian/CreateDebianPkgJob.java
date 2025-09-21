package org.aviatorlabs.ci.job.debian;

import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.githubrelease.GitHubReleaseConfig;
import org.aviatorlabs.ci.resource.githubrelease.GitHubReleaseResource;
import org.aviatorlabs.ci.resource.registry.AnonymousRegistryResource;
import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.Command;
import org.aviatorlabs.ci.sdk.step.task.config.Input;
import org.aviatorlabs.ci.sdk.step.task.config.TaskConfig;
import org.aviatorlabs.ci.sdk.variable.Variable;

public class CreateDebianPkgJob extends Job {
    private CreateDebianPkgJob(Resource resource) {
        super("%s-debian".formatted(resource.getName()));
    }

    public static CreateDebianPkgJob create(GitResource homebrewRepo, GitHubReleaseResource release) {
        CreateDebianPkgJob job = new CreateDebianPkgJob(release);
        job.markPublic().addSerialGroup("apt");

        GitHubReleaseConfig config = (GitHubReleaseConfig) release.getConfig();

        job.addStep(homebrewRepo.createGetDefinition())
                .addStep(release.createGetDefinition().enableTrigger());

        RegistryImageConfig genesisCommunityConfig = RegistryImageConfig.create(
                "registry.ops.scalecf.net/genesis-community/concourse",
                "latest"
        ).markInsecure();

        AnonymousResource<RegistryImageConfig> genesisCommunity = AnonymousRegistryResource.create(genesisCommunityConfig);

        TaskConfig createDebianConfig = TaskConfig.create(
                genesisCommunity,
                Command.createCommand(
                        homebrewRepo.createGetDefinition(),
                        "ci/scripts/create-debian-pkg-from-binary.sh")
        );
        createDebianConfig.addInput(Input.create(homebrewRepo.createGetDefinition()))
                .addInput(Input.create(release.createGetDefinition()).setPath("recipe"));

        createDebianConfig.addParam("DEBUG", "1")
                .addParam("REPO_ROOT", "homebrew")
                .addParam("REPO_OUT", "pushme")
                .addParam("IN_BINARY", "%s-linux-amd64".formatted(release.getName()))
                .addParam("OUT_BINARY", release.getName())
                .addParam("NAME", release.getName())
                .addParam("LICENSE", "MIT")
                .addParam("DESCRIPTION", "Install a directory of *.deb in the correct order")
                .addParam("URL", "https://github.com/%s/%s".formatted(config.getOwner(), config.getRepository()))
                .addParam("MAINTAINERS", "https://github.com/%s/%s/graphs/contributors".formatted(config.getOwner(), config.getRepository()))

                .addParam("VENDOR", "")

                .addParam("RELEASE_BUCKET", Variable.referenceVariable("debian_s3_bucket"))
                .addParam("AWS_ACCESS_KEY", Variable.referenceVariable("aws_access_key"))
                .addParam("AWS_SECRET_KEY", Variable.referenceVariable("aws_secret_key"))
                .addParam("S3_REGION", "us-east-1")
                .addParam("GPG_ID", Variable.referenceVariable("gpg_id"))
                .addParam("GPG_PUBLIC_KEY", Variable.referenceVariable("gpg", "public_key"))
                .addParam("GPG_PRIVATE_KEY", Variable.referenceVariable("gpg", "private_key"))
        ;

        Task createDebian = Task.create("create-debian", createDebianConfig);

        job.addStep(createDebian);

        return job;
    }
}
