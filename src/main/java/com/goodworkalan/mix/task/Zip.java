package com.goodworkalan.mix.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.goodworkalan.glob.Find;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.mix.FindList;
import com.goodworkalan.mix.MixException;
import com.goodworkalan.mix.Project;
import com.goodworkalan.mix.builder.Executable;
import com.goodworkalan.mix.builder.RecipeElement;

public class Zip {
    private final RecipeElement recipeElement;

    private final byte[] buffer = new byte[4098];

    private ZipOutputStream out;
    
    /** Set of files added to the zip file. */
    private final Set<String> seen = new HashSet<String>();

    /** The files to add to the zip file. */
    private final FindList findList = new FindList();
    
    /** The compression level. */
    private int level = 0;

    /** The output file name. */
    private File output;

    public Zip(RecipeElement recipeElement) {
        this.recipeElement = recipeElement;
    }

    /**
     * Set the output file name.
     * 
     * @param output
     *            The output file name.
     */
    public Zip output(File output) {
        this.output = output;
        return this;
    }

    /**
     * Set the compression level.
     * 
     * @param level
     *            The compression level.
     */
    public Zip level(int level) {
        this.level = level;
        return this;
    }

    /**
     * Add a source directory.
     * 
     * @param directory The source directory.
     */
    public FindElement<Zip> source(File directory) {
        return new FindElement<Zip>(this, findList, directory);
    }

    protected void addDirectory(String entryName) throws IOException {
        if (!entryName.endsWith("/")) {
            entryName = entryName + "/";
        }
        if (!seen.contains(entryName)) {
            seen.add(entryName);
            ZipEntry zipEntry = new ZipEntry(entryName);
            out.putNextEntry(zipEntry);
            out.closeEntry();
        }
    }
    
    protected void addFile(File source, String entryName) throws IOException {
        if (seen.contains(entryName)) {
            throw new MixException(0);
        }
        seen.add(entryName);
        byte[] buffer = this.buffer;
        FileInputStream in = new FileInputStream(source);
        ZipEntry zipEntry = new ZipEntry(entryName);
        out.putNextEntry(zipEntry);
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.closeEntry();
        in.close();
    }
    
    protected void addAdditionalEntries(Environment env) throws IOException {
    }

    protected void addFind(Find find, File directory) throws IOException {
        for (String fileName : find.find(directory)) {
            File source = new File(directory, fileName);
            if (source.isDirectory()) {
                addDirectory(fileName);
            } else {
                addFile(source, fileName);
            }
        }
    }
    
    public RecipeElement end() {
        recipeElement.addExecutable(new Executable() {
            public void execute(Environment env, Project project, String recipeName) {
                try {
                    out = new ZipOutputStream(new FileOutputStream(output));
                    out.setLevel(level);
                    for (FindList.Entry entry : findList) {
                        addFind(entry.getFind(), entry.getDirectory());
                    }
                    addAdditionalEntries(env);
                    out.close();
                } catch (IOException e) {
                    throw new MixException(0, e);
                }
            }

        });
        return recipeElement;
    }
}