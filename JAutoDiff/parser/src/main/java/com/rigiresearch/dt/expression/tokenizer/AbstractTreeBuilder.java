package com.rigiresearch.dt.expression.tokenizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.objecthunter.exp4j.operator.Operator;
import net.objecthunter.exp4j.tokenizer.FunctionToken;
import net.objecthunter.exp4j.tokenizer.NumberToken;
import net.objecthunter.exp4j.tokenizer.OperatorToken;
import net.objecthunter.exp4j.tokenizer.Token;
import net.objecthunter.exp4j.tokenizer.VariableToken;
import nilgiri.math.DoubleReal;
import nilgiri.math.RealNumber;
import nilgiri.math.autodiff.Constant;
import nilgiri.math.autodiff.DifferentialFunction;
import nilgiri.math.autodiff.DifferentialRealFunctionFactory;
import nilgiri.math.autodiff.Variable;

/**
 * An abstract tree builder.
 * @author Miguel Jimenez (miguel@uvic.ca)
 * @version $Id$
 * @since 0.1.0
 */
public final class AbstractTreeBuilder<X extends RealNumber<X>> {

	/**
	 * The expression to build.
	 */
	private final String expression;

	/**
	 * A real number factory to use when building the function objects.
	 */
	private final DifferentialRealFunctionFactory<X> factory;

	/**
	 * The variables used in the expression.
	 */
	private final Variable<X>[] variables;

	/**
	 * The token iterator.
	 */
	private final Iterator<Token> iterator;

	/**
	 * Default constructor.
	 * @param expression The expression to build
	 * @param variables The variables used in the expression
	 */
	public AbstractTreeBuilder(final String expression,
		final DifferentialRealFunctionFactory<X> factory,
		final Variable<X>... variables){
		if (expression == null) {
			throw new IllegalArgumentException("Invalid expression");
		}
		this.expression = expression;
		this.factory = factory;
		this.variables = variables.clone();
		this.iterator = this.init();
	}

	/**
	 * Initializes the expression's token iterator.
	 * @return A possibly empty iterator
	 */
	private Iterator<Token> init(){
		final ExpressionParser<X> builder = new ExpressionParser<>(this.expression);
		Arrays.stream(this.variables)
			.map(Variable::getName)
			.forEach(builder::variable);
		final List<Token> list = Arrays.asList(builder.build());
		Collections.reverse(list);
		return list.iterator();
	}

	/**
	 * Returns the tree for the given expression.
	 * @return An operation
	 * @throws TokenizerException If the next token in the expression is unknown
	 */
	public DifferentialFunction<X> getTree() throws TokenizerException {
		final Token token = this.iterator.next();
		final DifferentialFunction<X> result;
		switch (token.getType()){
			case Token.TOKEN_FUNCTION: result = this.getFunction(token);
				break;
			case Token.TOKEN_NUMBER: result = this.getConstant(token);
				break;
			case Token.TOKEN_OPERATOR: result = this.getOperator(token);
				break;
			case Token.TOKEN_PARENTHESES_OPEN:
			case Token.TOKEN_PARENTHESES_CLOSE:
				result = this.getTree();
				break;
			case Token.TOKEN_VARIABLE: result = this.getVariable(token);
				break;
			default: throw new TokenizerException("Invalid expression");
		}
		return result;
	}

	/**
	 * Returns a function.
	 * @param token The function token
	 * @return A non-null instance of {@link DifferentialFunction}
	 * @throws TokenizerException If there is an unsupported function
	 */
	private DifferentialFunction<X> getFunction(final Token token)
		throws TokenizerException {
		final FunctionToken function = (FunctionToken) token;
		final DifferentialFunction<X> result;
		switch (function.getFunction().getName()) {
			case "acos": result = this.factory.acos(this.getTree());
			    break;
			case "asin": result = this.factory.asin(this.getTree());
			    break;
			case "atan": result = this.factory.atan(this.getTree());
			    break;
			case "log": result = this.factory.log(this.getTree());
				break;
			case "cos": result = this.factory.cos(this.getTree());
				break;
			case "sin": result = this.factory.sin(this.getTree());
				break;
			case "sqrt": result = this.factory.sqrt(this.getTree());
				break;
			case "tan": result = this.factory.tan(this.getTree());
				break;
			case "exp": result = this.factory.exp(this.getTree());
				break;
			default: throw new TokenizerException(
				String.format(
					"Unsupported function: %s",
					function.getFunction().getName()
				)
			);
		}
		return result;
	}

	/**
	 * Instantiates an operation based on a given token.
	 * @param token The token
	 * @return A non-null instance of {@link DifferentialFunction}
	 * @throws TokenizerException If there is an unsupported operator
	 */
	private DifferentialFunction<X> getOperator(final Token token)
		throws TokenizerException {
		final DifferentialFunction<X> right = this.getTree();
		final DifferentialFunction<X> left = this.getTree();
		final DifferentialFunction<X> result;
		final Operator operator = ((OperatorToken) token).getOperator();
		switch (operator.getSymbol()) {
			case "+": result = left.plus(right);
				break;
			case "-": result = left.minus(right);
				break;
			case "*": result = left.mul(right);
				break;
			case "/": result = left.div(right);
				break;
			case "^": result = this.powOperator(left, right);
				break;
			default: throw new TokenizerException(
				String.format("Unsupported operator '%s'", operator.getSymbol())
			);
		}
		return result;
	}

	/**
	 * Instantiates a constant based on the token's value.
	 * @param token The number token
	 * @return The constant element
	 */
	private DifferentialFunction<X> getConstant(final Token token) {
		final DoubleReal value = new DoubleReal(((NumberToken) token).getValue());
		// X extends RealNumber<X>
		// DoubleReal implements RealNumber<DoubleReal>
		// Will trow error if the expressions are used with another type of real number
		return this.factory.val((X) value);
	}

	/**
	 * Finds a variable based on its name.
	 * @param token The variable token
	 * @return The variable element
	 */
	private DifferentialFunction<X> getVariable(final Token token) {
		final VariableToken variable = (VariableToken) token;
		final Optional<Variable<X>> optional = Arrays.stream(this.variables)
			.filter(tmp -> tmp.getName().equals(variable.getName()))
			.findFirst();
		if (optional.isEmpty()) {
			throw new IllegalStateException(
				String.format("Unknown variable '%s'", variable.getName())
			);
		}
		return optional.get();
	}

	/**
	 * Builds the pow operator.
	 * @param left The left side of the pow operator
	 * @param right The right side of the pow operator
	 * @return A differential function
	 */
	private DifferentialFunction<X> powOperator(final DifferentialFunction<X> left,
		final DifferentialFunction<X> right) {
		if (!right.isConstant()) {
			throw new UnsupportedOperationException(
				"Pow argument was expected to be a constant"
			);
		}
		final Constant<X> constant = this.factory.val(right.getValue());
		return this.factory.pow(left, constant);
	}

}
