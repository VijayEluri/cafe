package com.goodworkalan.mix;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.goodworkalan.go.go.PathPart;

public class RecipeElement {
    private final Builder builder;
    
    private final String name;
    
    private final Map<String, Recipe> recipes;
    
    private final Map<String, Command> commands = new LinkedHashMap<String, Command>();
    
    private final Map<String, Dependency> dependencies = new LinkedHashMap<String, Dependency>();
    
    private final List<PathPart> produces = new ArrayList<PathPart>();
    
    public RecipeElement(Builder builder, Map<String, Recipe> recipes, String name) {
        this.builder = builder;
        this.recipes = recipes;
        this.name = name;
    }
    
    /** Here's an idea on reuse and extension. */
    public RecipeElement reset() {
        return this;
    }

    /**
     * Call the configure method of the given recipe module passing this recipe
     * element.
     * 
     * @param recipeModule
     *            A pre-defined recipe.
     * @return This recipe language element in order to continue to specify
     *         recipe properties.
     */
    public RecipeElement apply(RecipeModule recipeModule) {
        recipeModule.configure(this);
        return this;
    }

    public CommandElement<RecipeElement> command(String name) {
        Command command = new Command(name);
        commands.put(name, command);
        return new CommandElement<RecipeElement>(this, command);
    }

    /**
     * Specify the dependencies for this recipe.
     * 
     * @return A depends language element to specify project dependencies.
     */
    public DependsElement depends() {
        return new DependsElement(this, dependencies);
    }
    
    public ProducesElement produces() {
        return new ProducesElement(this, produces);
    }

    public Builder end() {
        recipes.put(name, new Recipe(commands, dependencies, produces));
        return builder;
    }
}
