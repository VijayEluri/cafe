package com.goodworkalan.cafe.task;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.goodworkalan.cafe.Build;
import com.goodworkalan.cafe.Dependency;
import com.goodworkalan.cafe.Make;
import com.goodworkalan.cafe.Project;
import com.goodworkalan.cafe.builder.FindList;
import com.goodworkalan.cafe.builder.FindStatement;
import com.goodworkalan.cafe.builder.RecipeStatement;
import com.goodworkalan.comfort.io.ComfortIOException;
import com.goodworkalan.comfort.io.Files;
import com.goodworkalan.comfort.io.Find;
import com.goodworkalan.go.go.Commandable;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.PathPart;
import com.goodworkalan.go.go.library.PathParts;
import com.goodworkalan.go.go.library.ResolutionPart;
import com.goodworkalan.spawn.Exit;
import com.goodworkalan.spawn.Spawn;

// TODO Document.
public class Javadoc extends JavadocOptionsElement<RecipeStatement, Javadoc> {
    // TODO Document.
    private File output;
    
    /** The directory where package lists for offline linking are kept. */
    private File packageLists;
    
    // TODO Document.
    private FindList findList = new FindList();
    
    /** Artifacts. */
    private final List<Artifact> artifacts = new ArrayList<Artifact>();
    
    // TODO Document.
    public Javadoc(RecipeStatement recipeElement) {
        super(recipeElement, new SelfServer<Javadoc>(), null);
        self.setSelf(this);
        this.ending = new JavadocEnd() {
            public void end(JavadocConfiguration configuration) {
                configure(configuration);
                parent.executable(new Commandable() {
                    public void execute(Environment env) {
                        Build mix = env.get(Build.class, 0);
                        Project project = env.get(Project.class, 0);
                        List<String> arguments = new ArrayList<String>();
                        
                        arguments.add("-d");
                        arguments.add(mix.relativize(output).getPath());
                        
                        if (visibility != null) {
                            arguments.add("-" + visibility);
                        }
                        
                        Collection<PathPart> parts = new ArrayList<PathPart>();
                        for (Artifact artifact : artifacts) {
                            parts.add(new ResolutionPart(artifact));
                        }
                        Make make = env.get(Make.class, 1);
                        for (Dependency dependency : project.getRecipe(make.recipeName).getDependencies()) {
                            parts.addAll(dependency.getPathParts(project));
                        }
                        for (FindList.Entry entry : findList) {
                            Find find = entry.getFind();
                            if (!find.hasFilters()) {
                                find.include("**/*.java");
                            }
                            File directory = mix.relativize(entry.getDirectory());
                            for (String fileName : find.find(directory)) {
                                arguments.add(new File(directory, fileName).toString());
                            }
                        }
                        arguments.add("-quiet");
                        for (Map.Entry<URI, File> entry : offlineLinks.entrySet()) {
                            arguments.add("-linkoffline");
                            arguments.add(entry.getKey().toASCIIString());
                            File packages = entry.getValue();
                            if (!packages.isAbsolute()) {
                                packages = new File(mix.getWorkingDirectory(), packages.getPath());
                            }
                            arguments.add(packages.getAbsoluteFile().toURI().toString());
                        }
                        if (packageLists != null) {
                            File workingPackageLists = mix.relativize(packageLists);
                            if (workingPackageLists.isDirectory()) {
                                for (File group : workingPackageLists.listFiles()) {
                                    if (group.isDirectory() && group.canRead()) {
                                        for (File dir : group.listFiles()) {
                                            if (dir.isDirectory() && dir.canRead()) {
                                                try {
                                                    for (String line : Files.slurp(new File(dir, "url"))) {
                                                        line = line.trim();
                                                        if (line.length() != 0) {
                                                            arguments.add("-linkoffline");
                                                            arguments.add(line);
                                                            arguments.add(dir.getAbsoluteFile().toURI().toString());
                                                            break;
                                                        }
                                                    }
                                                } catch (ComfortIOException e) {
                                                    // If we can't read the URL file, then it doesn't get linked.
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
//                        if (!mixArguments.isOffline()) {
//                            for (URI link : links) {
//                                arguments.add("-link");
//                                arguments.add(link.toASCIIString());
//                            }
//                        }
                        Set<File> classpath = PathParts.fileSet(env.library.resolve(parts));
                        if (!classpath.isEmpty()) {
                            arguments.add("-classpath");
                            arguments.add(Files.path(classpath));
                        }
                        env.debug(Javadoc.class, "arguments", arguments);
                        arguments.add(0, "javadoc");
                        
                        ProcessBuilder newProcess = new ProcessBuilder();
                        newProcess.command().addAll(arguments);
                        
                        Exit exit = new Spawn().$(arguments).out(env.io.out).err(env.io.err).run();
                        
                        if (!exit.isSuccess()) {
                            throw new RuntimeException();
                        }
                    }
                });
                
            }
        };
    }

    // TODO Document.
    public Javadoc configure(JavadocConfiguration...configurations) {
        return configure(Arrays.asList(configurations));
    }
    
    // TODO Document.
    public Javadoc configure(List<JavadocConfiguration> configurations){
        for (JavadocConfiguration configuration : configurations) {
            configuration.configure(this);
        }
        return this;
    }

    // TODO Document.
    public Javadoc artifact(Artifact artifact) {
        artifacts.add(artifact);
        return this;
    }

    // TODO Document.
    public Javadoc link(URI uri) {
        links.add(uri);
        return this;
    }

    // TODO Document.
    public Javadoc offlineLink(URI uri, File file) {
        offlineLinks.put(uri, file);
        return this;
    }
    
    // TODO Document.
    public FindStatement<Javadoc> source(File directory) {
        return new FindStatement<Javadoc>(this, findList, directory);
    }

    // TODO Document.
    public Javadoc output(File directory) {
        this.output = directory;
        return this;
    }

    /**
     * Set the directory where package lists for offline linking are stored. The
     * directory will contain a directory for each library that will be linked
     * offline. In each library directory will be the package list and a file
     * named <code>url</code> that contains the URL where the documentation is
     * hosted.
     * 
     * @param directory
     *            The package lists.
     * @return This Javadoc object.
     */
    public Javadoc packageLists(File directory) {
        this.packageLists = directory;
        return this;
    }
}
