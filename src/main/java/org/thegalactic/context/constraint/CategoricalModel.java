package org.thegalactic.context.constraint;

/*
 * CategoricalModel.java
 *
 * Copyright: 2016 The Galactic Organization, France
 *
 * License: http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html CeCILL-B license
 *
 * This file is part of java-lattices.
 * You can redistribute it and/or modify it under the terms of the CeCILL-B license.
 */
import java.util.ArrayList;

/**
 * Categorical Model.
 */
public final class CategoricalModel {

    /**
     * Factory method to construct a categorical model.
     *
     * @return a new CategoricalModel object
     */
    static CategoricalModel create() {
        return new CategoricalModel();
    }

    /**
     * Values.
     */
    private final ArrayList<CategoricalAttribute> values;

    /**
     * This class is not designed to be publicly instantiated.
     */
    private CategoricalModel() {
        values = new ArrayList<CategoricalAttribute>();
    }

    /**
     * Add a new categorical attribute.
     *
     * @param attribute an attribute to add
     *
     * @return this for chaining
     */
    CategoricalModel add(CategoricalAttribute attribute) {
        values.add(attribute);
        return this;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return values.toString();
    }
}
