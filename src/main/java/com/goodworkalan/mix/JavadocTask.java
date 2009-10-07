package com.goodworkalan.mix;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.goodworkalan.glob.Files;
import com.goodworkalan.glob.Find;
import com.goodworkalan.go.go.Argument;
import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.go.go.Command;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.Library;
import com.goodworkalan.go.go.PathPart;
import com.goodworkalan.go.go.ResolutionPart;
import com.goodworkalan.go.go.Task;

@Command(parent = MixTask.class)
public class JavadocTask extends Task {
    private String recipe;
    
    private String visibility;
    
    private File outputDirectory;
    
    private FindList findList = new FindList();
    
    private LinkedList<OfflineLink> offlineLinks = new LinkedList<OfflineLink>();
    
    /** Artifacts. */
    private final List<Artifact> artifacts = new ArrayList<Artifact>();
   
    @Argument
    public void addArtifact(Artifact artifact) {
        artifacts.add(artifact);
    }

    /** The mix wide configuration. */
    private MixTask.Configuration configuration;
    
    @Argument
    public void addOfflineLink(File file) {
        offlineLinks.addLast(new OfflineLink(file));
    }
    
    @Argument
    public void addOfflineUri(URI uri) {
        if (offlineLinks.isEmpty()) {
            throw new MixError(0);
        }
        offlineLinks.getLast().setUri(uri);
    }
    
    @Argument
    public void addRecipe(String recipe) {
        this.recipe = recipe;
    }
 
    @Argument
    public void addInclude(String string) {
        findList.addInclude(string);
    }
    
    @Argument
    public void addExclude(String string) {
        findList.addExclude(string);
    }
    
    @Argument
    public void addVisibility(String visibility) {
        if ("|public|protected|package|private|".indexOf("|" + visibility + "|") == -1) {
            throw new MixError(0);
        }
        this.visibility = visibility;
    }
    
    @Argument
    public void addSourceDirectory(File sourceDirectory) {
        findList.addDirectory(sourceDirectory);
    }
    
    /**
     * The the arguments common to all Mix commands which are specified after
     * the mix command on the command line.
     * 
     * @param configuration
     *            The arguments common to all Mix commands.
     */
    public void setConfiguration(MixTask.Configuration configuration) {
        this.configuration = configuration;
    }
    
    @Argument
    public void addOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void execute(Environment env) {
        Project project = configuration.getProject();

        List<String> arguments = new ArrayList<String>();
        
        arguments.add("-d");
        arguments.add(outputDirectory.toString());
        
        if (visibility != null) {
            arguments.add("-" + visibility);
        }
     
        Collection<PathPart> parts = new ArrayList<PathPart>();
        for (Artifact artifact : artifacts) {
            parts.add(new ResolutionPart(artifact));
        }
        if (recipe != null) {
            for (Dependency dependency : project.getRecipe(recipe).getDependencies()) {
                parts.addAll(dependency.getPathParts(project));
            }
        }
        for (FindList.Entry entry : findList) {
            Find find = entry.getFind();
            if (!find.hasFilters()) {
                find.include("**/*.java");
            }
            for (String fileName : find.find(entry.getDirectory())) {
                arguments.add(new File(entry.getDirectory(), fileName).toString());
            }
        }
        arguments.add("-quiet");
        for (OfflineLink offlineLink : offlineLinks) {
            if (offlineLink.getUri() != null) {
                arguments.add("-linkoffline");
                arguments.add(offlineLink.getUri().toString());
                arguments.add(offlineLink.getPackages().getAbsoluteFile().toURI().toString());
            }
        }
        Set<File> classpath = new LinkedHashSet<File>();
        Library library = env.commandPart.getCommandInterpreter().getLibrary();
        classpath.addAll(library.resolve(parts).getFiles());
        if (!classpath.isEmpty()) {
            arguments.add("-classpath");
            arguments.add(Files.path(classpath));
        }
        
        try {
            com.sun.tools.javadoc.Main.execute(arguments.toArray(new String[arguments.size()]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}