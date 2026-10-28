package com.example.web.aop;

import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Resolves annotation keys written as SpEL expressions.
 *
 * <p>Method parameter names are available as SpEL variables, so {@code "#request.orderNo"}
 * works on a method argument named {@code request}.</p>
 *
 * @author nanak
 */
public final class SpelKeys {

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private SpelKeys() {
    }

    /**
     * Resolves an annotation key and prefixes it for the Redis namespace.
     *
     * @param expression SpEL expression, or an empty string for a generated key
     * @param signature  intercepted method signature
     * @param args       method arguments
     * @param prefix     Redis key prefix
     * @return namespaced key
     */
    public static String resolve(String expression, MethodSignature signature, Object[] args, String prefix) {
        String suffix = StringUtils.hasText(expression)
                ? evaluate(expression, signature.getMethod(), args)
                : signature.toShortString() + ':' + Arrays.deepHashCode(args);
        return prefix + suffix;
    }

    private static String evaluate(String expression, Method method, Object[] args) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] parameterNames = NAME_DISCOVERER.getParameterNames(method);
        for (int i = 0; i < args.length; i++) {
            context.setVariable("p" + i, args[i]);
            context.setVariable("a" + i, args[i]);
            if (parameterNames != null && i < parameterNames.length) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }
        String value = PARSER.parseExpression(expression).getValue(context, String.class);
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("SpEL key expression resolved to an empty value: " + expression);
        }
        return value;
    }
}
