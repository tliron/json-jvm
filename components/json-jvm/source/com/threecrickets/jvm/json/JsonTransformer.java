/**
 * Copyright 2010-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the Mozilla Public
 * License version 1.1: http://www.mozilla.org/MPL/MPL-1.1.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.jvm.json;

/**
 * Transforms objects.
 * <p>
 * Used by the {@link JsonDecoder} to transform special JSON data to native
 * objects.
 * 
 * @author Tal Liron
 */
public interface JsonTransformer
{
	/**
	 * Transforms the object.
	 * 
	 * @param object
	 *        The object
	 * @param implementation
	 *        The implementation
	 * @return The transformed object, or null if not transformed
	 */
	public Object transform( Object object, JsonImplementation implementation );
}
