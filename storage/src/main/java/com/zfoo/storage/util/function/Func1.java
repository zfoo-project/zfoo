package com.zfoo.storage.util.function;

import java.io.Serializable;

/**
 * Single-parameter Function object<br>
 * Inspired by ActFramework<a href="http://actframework.org/">ActFramework</a><br>
 * A functional interface wrapping a function as an object<br>
 * Before JDK 8, functions could not be passed/returned; this wraps a function as an object
 *
 * @author Looly
 *
 * @param <P> parameter type
 * @param <R> return type
 * @since 4.2.2
 */
@FunctionalInterface
public interface Func1<P, R> extends Serializable {

	/**
	 * Execute the function
	 *
	 * @param parameter the parameter
	 * @return function result
	 * @throws Exception custom exception
	 */
	R apply(P parameter) throws Exception;

}
