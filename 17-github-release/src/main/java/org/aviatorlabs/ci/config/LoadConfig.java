package org.aviatorlabs.ci.config;

import org.aviatorlabs.ci.GitHubReleaseExample;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;

public class LoadConfig {

    public static PipelineConfigDTO load(String file) {
        Yaml yaml = new Yaml(new Constructor(PipelineConfigDTO.class, new LoaderOptions()));
        InputStream inputStream = GitHubReleaseExample.class
                .getClassLoader()
                .getResourceAsStream(file);
        return yaml.load(inputStream);
    }
}
