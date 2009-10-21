package com.goodworkalan.mix.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.mix.task.Copy;
import com.goodworkalan.mix.task.Delete;
import com.goodworkalan.mix.task.Dependencies;
import com.goodworkalan.mix.task.Javac;
import com.goodworkalan.mix.task.JavacConfiguration;
import com.goodworkalan.mix.task.JavacEnd;
import com.goodworkalan.mix.task.JavacOptionsElement;
import com.goodworkalan.mix.task.Javadoc;
import com.goodworkalan.mix.task.JavadocConfiguration;
import com.goodworkalan.mix.task.JavadocEnd;
import com.goodworkalan.mix.task.JavadocOptionsElement;
import com.goodworkalan.mix.task.Mkdirs;
import com.goodworkalan.mix.task.OutputConfiguration;
import com.goodworkalan.mix.task.TestNG;
import com.goodworkalan.mix.task.Zip;


public class JavaProject extends JavaSpecificsElement {
    private Artifact produces;
    
    private final Builder builder;
    
    private final List<JavacConfiguration> javacConfigurations = new ArrayList<JavacConfiguration>();
    
    private final List<JavadocConfiguration> apidocConfigurations = new ArrayList<JavadocConfiguration>();

    private final List<JavadocConfiguration> devdocConfigurations = new ArrayList<JavadocConfiguration>();
    
    public JavaProject(Builder builder) {
        this.builder = builder;
        this
            .main()
                .javac()
                     .output(new File("target/classes"))
                     .end()
                .end();
    }
    
    public JavacOptionsElement<JavaProject, JavacOptionsElement<JavaProject, ?>> javac() {
        return JavacOptionsElement.<JavaProject>newJavacOptionsElement(this, new JavacEnd() {
            public void end(JavacConfiguration configuration) {
                javacConfigurations.add(configuration);
            }
        });
    }
    
    public JavadocOptionsElement<JavaProject, JavadocOptionsElement<JavaProject, ?>> apidocs() {
        return JavadocOptionsElement.newJavadocOptionsElement(this, new JavadocEnd() {
            public void end(JavadocConfiguration configuration) {
                apidocConfigurations.add(configuration);
            }
        });
    }
    
    public JavadocOptionsElement<JavaProject, JavadocOptionsElement<JavaProject, ?>> devdocs() {
        return JavadocOptionsElement.newJavadocOptionsElement(this, new JavadocEnd() {
            public void end(JavadocConfiguration configuration) {
                devdocConfigurations.add(configuration);
            }
        });
    }
    
    public Builder end() {
        builder
            .recipe("javac")
                .depends()
                    .dependencies(mainDependencies)
                    .end()
                .task(Delete.class)
                    .file(getOutput(javacConfigurations))
                    .recurse(true)
                    .end()
                .task(Javac.class)
                    .source(new File("src/main/java")).end()
                    .configure(mainJavacConfigurations)
                    .output(new File("target/classes"))
                    .end()
                .task(Copy.class)
                    .source(new File("src/main/resources")).end()
                    .output(new File("target/classes"))
                    .end()
                 .produces()
                     .classes(new File("target/classes"))
                     .end()
                 .end()
            .recipe("javac-test")
                .depends()
                    .dependencies(testDependencies)
                    .end()
                .task(Delete.class)
                    .file(new File("target/test-classes"))
                    .recurse(true)
                    .end()
                .task(Javac.class)
                    .source(new File("src/test/java")).end()
                    .configure(testJavacConfigurations)
                    .output(new File("target/test-classes"))
                    .end()
                .task(Copy.class)
                    .source(new File("src/main/java")).end()
                    .output(new File("target/test-classes"))
                    .end()
                .produces()
                    .classes(new File("target/test-classes"))
                    .end()
                .end()
            .recipe("apidoc")
                .depends()
                    .classes("javac")
                    .end()
                .task(Delete.class)
                    .file(new File("target/apidocs")).recurse(true)
                    .end()
                .task(Mkdirs.class)
                    .directory(new File("target/apidocs"))
                    .end()
                .task(Javadoc.class)
                    .configure(apidocConfigurations)
                    .end()
                .end()
            .recipe("devdoc")
                .depends()
                    .classes("javac")
                    .end()
                .task(Delete.class)
                    .file(new File("target/devdocs")).recurse(true)
                    .end()
                .task(Mkdirs.class)
                    .directory(new File("target/devdocs"))
                    .end()
                .task(Javadoc.class)
                    .configure(devdocConfigurations)
                    .end()
                .end()
            .recipe("test")
                .depends()
                    .classes("javac")
                    .end()
                .task(TestNG.class)
                    .classes(getOutput(javacConfigurations))
                    .end()
                .end()
            .recipe("clean")
                .task(Delete.class)
                    .file(new File("target"))
                    .recurse(true)
                    .end()
                .end()
            .recipe("distribute")
                .depends()
                    .classes("javac").classes("apidocs")
                    .end()
                .task(Delete.class)
                    .file(new File("target/distribution"))
                    .recurse(true)
                    .end()
                .task(Mkdirs.class)
                    .directory(new File("target/distribution/" + "target/distribution/" + produces.getDirectoryPath()))
                    .end()
                .task(Dependencies.class)
                    .recipe("javac")
                    .output(new File("target/distribution/" + produces.getPath("dep")))
                    .end()
                .task(Zip.class)
                    .source(new File("target/classes")).end()
                    .level(0)
                    .output(new File("target/distribution/" + produces.getPath("jar")))
                    .end()
                .task(Zip.class)
                    .source(new File("src/main/java")).end()
                    .source(new File("src/main/resources")).end()
                    .level(0)
                    .output(new File("target/distribution/" + produces.getPath("sources/jar")))
                    .end()
                .task(Zip.class)
                    .source(new File("target/apidocs")).end()
                    .level(0)
                    .output(new File("target/distribution/" + produces.getPath("javadoc/jar")))
                .end();
        return builder;
    }

    private File getOutput(List<? extends OutputConfiguration> configurations) {
        File mainClassOutput = null;
        for (OutputConfiguration configuration : configurations) {
            if (configuration.getOuptut() != null) {
                mainClassOutput = configuration.getOuptut();
            }
        }
        return mainClassOutput;
    }
}
