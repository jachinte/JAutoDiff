package com.rigiresearch.dt.expression.tokenizer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import net.objecthunter.exp4j.function.Functions;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.shuntingyard.ShuntingYard;
import net.objecthunter.exp4j.tokenizer.Token;
import nilgiri.math.Field;
import nilgiri.math.RealNumber;
import nilgiri.math.autodiff.DifferentialRealFunctionFactory;

/**
 * Factory class for {@link Expression} instances.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @version $Id$
 * @since 0.1.0
 */
public final class ExpressionParser<X extends Field<X>> {

    /**
     * The expression being parsed.
     */
    private final String expression;

    /**
     * Functions defined by the user.
     */
    private final Map<String, Function> functions;

    /**
     * Operators defined by the user.
     */
    private final Map<String, Operator> operators;

    /**
     * Variable names.
     */
    private final Set<String> names;

    /**
     * Whether implicit multiplication is enabled.
     */
    private boolean implicit = true;

    /**
     * Create a new {@link ExpressionBuilder} instance and initialize it with a
     * given expression string.
     * @param expression The expression to be parsed
     */
    public ExpressionParser(final String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression can not be empty");
        }
        this.expression = expression;
        this.operators = new HashMap<>(4);
        this.functions = new HashMap<>(4);
        this.names = new HashSet<>(4);
    }

    /**
     * Add a {@link Function} implementation
     * available for use in the expression
     * @param function The custom {@link Function} implementation that should be
     * available for use in the expression.
     * @return The {@link ExpressionBuilder} instance
     */
    public ExpressionParser function(final Function function) {
        this.functions.put(function.getName(), function);
        return this;
    }

    /**
     * Add multiple {@link Function} implementations available for use in the
     * expression
     * @param functions The custom {@link Function} implementations
     * @return The {@link ExpressionBuilder} instance
     */
    public ExpressionParser functions(final Function... functions) {
        for (final Function function : functions) {
            this.functions.put(function.getName(), function);
        }
        return this;
    }

    /**
     * Add multiple {@link Function} implementations available for use in the
     * expression
     * @param functions A {@link List} of custom {@link Function} implementations
     * @return The {@link ExpressionBuilder} instance
     */
    public ExpressionParser functions(final Iterable<Function> functions) {
        for (final Function function : functions) {
            this.functions.put(function.getName(), function);
        }
        return this;
    }

    /**
     * Declare variable names used in the expression
     * @param names The variables used in the expression
     * @return The {@link ExpressionBuilder} instance
     */
    public ExpressionParser variables(final Set<String> names) {
        this.names.addAll(names);
        return this;
    }

    /**
     * Declare variable names used in the expression
     * @param names The variables used in the expression
     * @return The {@link ExpressionBuilder} instance
     */
    public ExpressionParser variables(final String... names) {
        Collections.addAll(this.names, names);
        return this;
    }

    /**
     * Declare a variable used in the expression
     * @param name The variable used in the expression
     * @return The {@link ExpressionBuilder} instance
     */
    public ExpressionParser variable(final String name) {
        this.names.add(name);
        return this;
    }

    /**
     * Enables or disables implicit multiplication.
     * @param enabled Whether implicit multiplication must be enabled
     * @return This
     */
    public ExpressionParser implicitMultiplication(final boolean enabled) {
        this.implicit = enabled;
        return this;
    }

    /**
     * Add an {@link Operator} which should be available for use in the expression
     * @param operator The custom {@link Operator} to add
     * @return The {@link ExpressionBuilder} instance
     */
    public ExpressionParser operator(final Operator operator) {
        ExpressionParser.checkOperatorSymbol(operator);
        this.operators.put(operator.getSymbol(), operator);
        return this;
    }

    /**
     * Checks the operator symbol.
     * @param operator The operator
     */
    private static void checkOperatorSymbol(final Operator operator) {
        final String name = operator.getSymbol();
        for (final char character : name.toCharArray()) {
            if (!Operator.isAllowedOperatorChar(character)) {
                throw new IllegalArgumentException(
                    String.format(
                        "The operator symbol '%s' is invalid",
                        name
                    )
                );
            }
        }
    }

    /**
     * Add multiple {@link Operator} implementations which should be available
     * for use in the expression
     * @param operators The set of custom {@link Operator} implementations to add
     * @return The {@link ExpressionBuilder} instance
     */
    public ExpressionParser operator(final Operator... operators) {
        for (final Operator operator : operators) {
            this.operator(operator);
        }
        return this;
    }

    /**
     * Add multiple {@link Operator} implementations which should be available
     * for use in the expression
     * @param operators The {@link List} of custom {@link Operator}
     *  implementations to add
     * @return The {@link ExpressionBuilder} instance
     */
    public ExpressionParser operator(final List<Operator> operators) {
        for (final Operator operator : operators) {
            this.operator(operator);
        }
        return this;
    }

    /**
     * Build the {@link Expression} instance using the custom operators and
     * functions set.
     * @return An {@link Expression} instance which can be used to evaluate the
     *  result of the expression
     */
    public Token[] build() {
        if (this.expression.isEmpty()) {
            throw new IllegalArgumentException("The expression can not be empty");
        }
        // set the constants' variable names
        this.names.add("pi");
        this.names.add("π");
        this.names.add("e");
        this.names.add("φ");
        // Check if there are duplicate vars/functions
        for (final String variable : this.names) {
            if (Functions.getBuiltinFunction(variable) != null ||
                this.functions.containsKey(variable)) {
                throw new IllegalArgumentException(
                    String.format(
                        "A variable can not have the same name as a function [%s]",
                        variable
                    )
                );
            }
        }
        return ShuntingYard.convertToRPN(
            this.expression,
            this.functions,
            this.operators,
            this.names,
            this.implicit
        );
    }

}
