package org.aviatorlabs.ci.job.homebrew;

import org.aviatorlabs.ci.resource.git.GitResource;
import org.aviatorlabs.ci.resource.githubrelease.GitHubReleaseResource;
import org.aviatorlabs.ci.sdk.job.Job;
import org.aviatorlabs.ci.sdk.resource.Resource;

public class CreateHomebrewReleaseJob extends Job {
    private CreateHomebrewReleaseJob(Resource resource) {
        super("%s-homebrew".formatted(resource.getName()));
    }

    public static CreateHomebrewReleaseJob create(GitResource homebrewRepo, GitHubReleaseResource release) {
        CreateHomebrewReleaseJob job = new CreateHomebrewReleaseJob(release);
        job.markPublic().markSerial();

        job.addStep(homebrewRepo.createGetDefinition()).addStep(release.createGetDefinition().enableTrigger());

        return job;
    }
}
