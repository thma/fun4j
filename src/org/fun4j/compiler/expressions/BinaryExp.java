package org.fun4j.compiler.expressions;

import org.fun4j.compiler.Expression;


/**
 * abstract binary expression.
 */
public abstract class BinaryExp
        extends Expression {

    protected Expression e1;

    protected Expression e2;

    public BinaryExp(final Expression e1, final Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }
    
}
