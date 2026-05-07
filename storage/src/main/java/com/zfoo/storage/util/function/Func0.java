package com.zfoo.storage.util.function;

import java.io.Serializable;

/**
 * No-parameter Function object<br>
 * Inspired by ActFramework<a href="http://actframework.org/">ActFramework</a><br>
 * A functional interface wrapping a function as an object<br>
 * Before JDK 8, functions could not be passed/returned; this wraps a function as an object
 *
 * @author Looly
 *
 * @param <R> return type
 * @since 4.5.2
 */
@FunctionalInterface
public interface Func0<R> extends Serializable {
	/**
	 * Execute the function
	 *
	 * @return function result
	 * @throws Exception custom exception
	 */
	R apply() throws Exception;

}
