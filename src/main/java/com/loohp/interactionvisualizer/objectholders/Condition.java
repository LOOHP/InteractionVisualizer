package com.loohp.interactionvisualizer.objectholders;

/**
 * Represents a condition.
 *
 * <p>There is no requirement that a new or distinct result be returned each
 * time the supplier is invoked.
 *
 * <p>This is a functional interface
 * whose functional method is {@link #check()}.
 */
@FunctionalInterface
public interface Condition {

    boolean check();

}
