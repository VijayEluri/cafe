package com.goodworkalan.cafe.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.goodworkalan.cafe.Dependency;
import com.goodworkalan.cafe.Project;
import com.goodworkalan.cafe.Target;
import com.goodworkalan.cafe.TargetPathPart;
import com.goodworkalan.go.go.library.Include;
import com.goodworkalan.go.go.library.PathPart;

/**
 * A recipe dependency that includes the recipe output and also includes the
 * dependencies of any recipes the given recipe depends upon.
 * <p>
 * FIXME OutputDepdenency and RecipeDependency - Eh? What's that about?
 * 
 * @author Alan Gutierrez
 */
class RecipeDependency implements Dependency {
    /** The recipe name. */
    private final String name;

    /**
     * Create a recipe dependency for the recipe with the given name.
     * 
     * @param name
     *            The recipe name.
     */
    public RecipeDependency(String name) {
        this.name = name;
    }

    /**
     * Create a collection of unexpanded path parts that includes the path parts
     * of recipe productions and the path parts of all of the recipe
     * dependencies.
     * 
     * @param project
     *            The project.
     */
    public Collection<PathPart> getPathParts(Project project) {
        return Collections.<PathPart>singletonList(new TargetPathPart(project, name));
    }

    /**
     * Create a collection of the unexpanded artifacts including all the
     * artifact dependencies of any recipes this recipe depends upon.
     * 
     * @param project
     *            The project.
     */
    public Collection<Include> getIncludes(Project project) {
        Collection<Include> includes = new ArrayList<Include>();
        Target recipe = project.getRecipe(name);
        for (Dependency dependency : recipe.getDependencies()) {
            includes.addAll(dependency.getIncludes(project));
        }
        return includes;
    }

    /**
     * Get a singleton collection containing the recipe name of this recipe
     * dependency.
     * 
     * @return A singleton collection with the recipe name.
     */
    public Collection<String> getRecipeNames() {
        return Collections.singleton(name);
    }
}
