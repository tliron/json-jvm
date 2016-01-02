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

import java.io.IOException;

/**
 * Encodes objects into JSON text.
 * 
 * @author Tal Liron
 */
public interface JsonEncoder
{
	/**
	 * Whether this encoder can encode the object into JSON.
	 * 
	 * @param object
	 *        The object to test
	 * @param context
	 *        The context
	 * @return True if we can encode the object
	 */
	public boolean canEncode( Object object, JsonContext context );

	/**
	 * Encode the object into JSON.
	 * 
	 * @param object
	 *        The object
	 * @param context
	 *        The context
	 * @throws IOException
	 *         In case of a write error
	 */
	public void encode( Object object, JsonContext context ) throws IOException;
}
