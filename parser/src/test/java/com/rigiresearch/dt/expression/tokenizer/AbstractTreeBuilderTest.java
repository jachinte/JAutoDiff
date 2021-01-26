package com.rigiresearch.dt.expression.tokenizer;

import nilgiri.math.DoubleReal;
import nilgiri.math.DoubleRealFactory;
import nilgiri.math.autodiff.Constant;
import nilgiri.math.autodiff.DifferentialFunction;
import nilgiri.math.autodiff.DifferentialRealFunctionFactory;
import nilgiri.math.autodiff.Variable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests {@link AbstractTreeBuilder}.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @version $Id$
 * @since 0.1.0
 */
class AbstractTreeBuilderTest {

    /**
     * The logger.
     */
    private static Logger LOGGER =
        LoggerFactory.getLogger(AbstractTreeBuilderTest.class);

    /**
     * A real numbers' factory.
     */
    private static final DoubleRealFactory RF = DoubleRealFactory.instance();

    /**
     * A differential functions' factory.
     */
    private static final DifferentialRealFunctionFactory<DoubleReal> DF =
        new DifferentialRealFunctionFactory<>(AbstractTreeBuilderTest.RF);

    /**
     * A constant to compare double values.
     */
    private static final double EPSILON = 0.00001;

    @SuppressWarnings("unchecked")
    @Test
    void simpleTest() throws TokenizerException {
        // Define the variables
        final Variable<DoubleReal> x = AbstractTreeBuilderTest.DF.var("x", new DoubleReal(10.0));
        final Variable<DoubleReal> y = AbstractTreeBuilderTest.DF.var("y", new DoubleReal(5.5));

        // Define a function manually
        final Constant<DoubleReal> constant = AbstractTreeBuilderTest.DF.val(new DoubleReal(2.0));
        final DifferentialFunction<DoubleReal> manual = constant.mul(x.pow(2)).plus(y);

        // Define the same function by parsing a expression
        final String expression = "2x^2 + y";
        final DifferentialFunction<DoubleReal> function = new AbstractTreeBuilder<>(
            expression,
            AbstractTreeBuilderTest.DF,
            x,
            y
        ).getTree();

        Assertions.assertTrue(
            manual.getValue().doubleValue() - function.getValue().doubleValue() <
                AbstractTreeBuilderTest.EPSILON,
            "The manual and parsed functions are different"
        );
        Assertions.assertNotNull(
            function.diff(x),
            "The derivative with respect to x should not be null"
        );
        AbstractTreeBuilderTest.LOGGER.info("f(x, y): {}", function);
        AbstractTreeBuilderTest.LOGGER.info("df/dx: {}", function.diff(x));
        AbstractTreeBuilderTest.LOGGER.info("df/dy: {}", function.diff(y));
    }

}
