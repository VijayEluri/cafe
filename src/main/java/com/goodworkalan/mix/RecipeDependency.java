package com.goodworkalan.mix;

import java.util.ArrayList;
import java.util.Collection;

import com.goodworkalan.go.go.PathPart;

// FIXME OutputDepdenency and RecipeDependency
public class RecipeDependency implements Dependency {
    private final String recipeName;
    
    public RecipeDependency(String recipeName) {
        this.recipeName = recipeName;
    }

    public Collection<PathPart> getPathParts(Project project) {
        Collection<PathPart> parts = new ArrayList<PathPart>();
        Recipe recipe = project.getRecipe(recipeName);
        parts.addAll(recipe.getProduce());
        for (Dependency dependency : recipe.getDependencies()) {
            parts.addAll(dependency.getPathParts(project));
        }
        return parts;
    }
}
