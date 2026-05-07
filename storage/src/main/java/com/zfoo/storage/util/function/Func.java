package com.zfoo.storage.util.function;

import java.io.Serializable;

/**
 * Function object<br>
 * Inspired by ActFramework<a href="http://actframework.org/">ActFramework</a><br>
 * A functional interface wrapping a function as an object<br>
 * Before JDK 8, functions could not be passed/returned; this wraps a function as an object
 *
 * @author Looly
 *
 * @param <P> parameter type
 * @param <R> return type
 * @since 3.1.0
 */
@FunctionalInterface
public interface Func<P, R> extends Serializable {
	/**
	 * Execute the function
	 *
	 * @param parameters parameter list
	 * @return function result
	 * @throws Exception custom exception
	 */
	@SuppressWarnings("unchecked")
	R apply(P... parameters) throws Exception;

}
