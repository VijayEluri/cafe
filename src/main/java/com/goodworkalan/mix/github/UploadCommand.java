package com.goodworkalan.mix.github;

import java.io.File;

import com.goodworkalan.comfort.io.Find;
import com.goodworkalan.github4j.downloads.Download;
import com.goodworkalan.github4j.downloads.GitHubDownloadException;
import com.goodworkalan.github4j.downloads.GitHubDownloads;
import com.goodworkalan.github4j.uploader.GitHubUploadException;
import com.goodworkalan.github4j.uploader.GitHubUploader;
import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.mix.Production;
import com.goodworkalan.mix.Project;

@Command(parent = GitHubCommand.class)
public class UploadCommand implements Commandable {
    @Argument
    public String projectName;
    
    @Argument
    public String contentType = "application/octet-stream";
    
    /** Whether or not to replace an existing file with the same name. */
    @Argument
    public boolean replace;

    public String getDescription(File file) {
        if (file.getName().endsWith("-javadoc.jar")) {
            return "Javadoc documentation.";
        } else if (file.getName().endsWith("-sources.jar")) {
            return "Source code.";
        } else if (file.getName().endsWith(".dep")) {
            return "Jav-a-Go-Go dependencies file.";
        } else if (file.getName().endsWith(".jar")) {
            return "Java classes.";
        }
        return "";
    }
    
    public void execute(Environment env) {
        Project project = env.get(Project.class, 0);
        GitHubConfig github = env.get(GitHubConfig.class, 1);
        for (Production source : project.getProductions()) {
            env.executor.run(env.io, "mix", env.arguments.get(0), "make", source.getRecipeName());
            Artifact artifact = source.getArtifact();
            Find find = new Find();
            find.include(artifact.getName() + "-" + artifact.getVersion() + "*.*");
            File sourceDirectory = new File(source.getDirectory(), source.getArtifact().getDirectoryPath());
            String[] split = source.getArtifact().getGroup().split("\\.");
            String projectName = split[split.length - 1];
            for (String fileName : find.find(sourceDirectory)) {
                try {
                    for (Download download : GitHubDownloads.getDownloads(github.login, projectName)) {
                        if (download.getFileName().equals(fileName)) {
                            if (!replace) {
                                throw new GitHubError(UploadCommand.class, "exists", fileName);
                            }
                            download.delete(github.token);
                        }
                    }
                } catch (GitHubDownloadException e) {
                    throw new GitHubError(UploadCommand.class, "delete", e);
                }
                File sourceFile = new File(sourceDirectory, fileName);
                String description = getDescription(sourceFile);
                env.debug("upload", project, sourceFile, description, contentType, fileName);
                GitHubUploader uploader = new GitHubUploader(github.login, github.token);
                try {
                    uploader.upload(projectName, sourceFile, description, contentType, fileName);
                } catch (GitHubUploadException e) {
                    throw new GitHubError(UploadCommand.class, "io", e);
                }
            }
        }
    }
}
