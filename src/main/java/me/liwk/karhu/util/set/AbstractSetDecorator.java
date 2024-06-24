/*
 * Decompiled with CFR 0.152.
 */
package me.liwk.karhu.util.set;

import java.util.Set;

public abstract class AbstractSetDecorator<E>
extends AbstractCollectionDecorator<E>
implements Set<E> {
    private static final long serialVersionUID = -4678668309576958546L;

    protected AbstractSetDecorator() {
    }

    protected AbstractSetDecorator(Set<E> set) {
        super(set);
    }

    @Override
    public boolean equals(Object object) {
        return object == this || this.decorated().equals(object);
    }

    @Override
    public int hashCode() {
        return this.decorated().hashCode();
    }

    @Override
    protected Set<E> decorated() {
        return (Set)super.decorated();
    }
}

