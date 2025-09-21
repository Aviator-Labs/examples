package org.aviatorlabs.ci.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PipelineConfigDTO {
    private List<GitHubReleaseDTO> githubReleases;
}
