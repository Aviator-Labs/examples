package org.aviatorlabs.ci.job.homebrew;

import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.git.get.GitGet;
import org.aviatorlabs.ci.resource.git.put.GitPutConfig;
import org.aviatorlabs.ci.resource.githubrelease.GitHubReleaseResource;
import org.aviatorlabs.ci.resource.githubrelease.put.GitHubReleasePutConfig;
import org.aviatorlabs.ci.resource.registry.AnonymousRegistryResource;
import org.aviatorlabs.ci.resource.registry.RegistryImageConfig;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.AnonymousResource;
import org.aviatorlabs.ci.sdk.resource.Resource;
import org.aviatorlabs.ci.sdk.resource.get.Get;
import org.aviatorlabs.ci.sdk.step.task.Task;
import org.aviatorlabs.ci.sdk.step.task.config.Command;
import org.aviatorlabs.ci.sdk.step.task.config.Input;
import org.aviatorlabs.ci.sdk.step.task.config.Output;
import org.aviatorlabs.ci.sdk.step.task.config.TaskConfig;

public class CreateHomebrewReleaseJob extends Job {
    private CreateHomebrewReleaseJob(Resource resource) {
        super("%s-homebrew".formatted(resource.getName()));
    }

    public static CreateHomebrewReleaseJob create(GitResource homebrewRepo, GitHubReleaseResource release) {
        CreateHomebrewReleaseJob job = new CreateHomebrewReleaseJob(release);
        job.markPublic().markSerial();

        GitGet hbRepoGet = homebrewRepo.createGetDefinition(homebrewRepo.getName());
        Get releaseGet = release.createGetDefinition(release.getName()).enableTrigger();
        job.addStep(hbRepoGet)
                .addStep(releaseGet);

        RegistryImageConfig genesisCommunityConfig = RegistryImageConfig.create(
                "registry.ops.scalecf.net/genesis-community/concourse",
                "latest"
        ).markInsecure();

        AnonymousResource<RegistryImageConfig> genesisCommunity = AnonymousRegistryResource.create(genesisCommunityConfig);

        TaskConfig updateHomebrewConfig = TaskConfig.create(
                genesisCommunity,
                Command.createCommand(
                                hbRepoGet,
                                "ci/scripts/update-homebrew.sh"
                        )
                        .addArg("%s.rb".formatted(release.getName()))
        );

        Output pushme = Output.create("pushme");

        updateHomebrewConfig.addInput(Input.create(hbRepoGet))
                .addInput(Input.create(releaseGet).setPath("recipe"))
                .addParam("REPO_ROOT", "homebrew")
                .addParam("REPO_OUT", "pushme")
                .addParam("BINARY", "%s-darwin-amd64".formatted(release.getName()))
                .addOutput(pushme);

        job.addStep(Task.create("update-homebrew", updateHomebrewConfig));

//        job.addStep(homebrewRepo.createPutDefinition());

        return job;
    }
}
