package com.goodworkalan.cafe.builder;

import java.util.List;

import com.goodworkalan.cafe.Rebuild;

/**
 * An element in the domain-specific language that specifies a rebuild if dirty
 * test that compares a set of source files to a set of output files and
 * skips running the recipe if all the source is older than all the output. 
 * 
 * @author Alan Gutierrez
 */
public class RebuildClause {
    /** The parent recipe element in the domain-specific language. */
    private final RecipeStatement parent;
    
    /** The list of rebuilds for the recipe. */
    private final List<Rebuild> rebuilds;
    
    /**
     * Create a new when element.
     * 
     * @param parent
     *            The parent recipe element in the domain-specific language.
     * @param rebuilds
     *            The list of rebuilds for the recipe.
     */
    public RebuildClause(RecipeStatement parent, List<Rebuild> rebuilds) {
        this.parent = parent;
        this.rebuilds = rebuilds;
    }

    /**
     * Return a when element that is used to specify the source files of the
     * rebuild if dirty test.
     * 
     * @return A when element to specify the source files of the rebuild if
     *         dirty test.
     */
    public WhenClause when() {
        return new WhenClause(parent, rebuilds);
    }
}
