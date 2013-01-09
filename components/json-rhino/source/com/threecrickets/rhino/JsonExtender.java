/**
 * Copyright 2010-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the Mozilla Public
 * License version 1.1: http://www.mozilla.org/MPL/MPL-1.1.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.rhino;

import org.mozilla.javascript.ScriptableObject;

public interface JsonExtender
{
	/**
	 * @param scriptable
	 *        The JavaScript object
	 * @param javaScript
	 *        True to prefer conversion to Rhino native objects (Date, RegEx,
	 *        etc.)
	 * @return A BSON object, a java.util.Date, a JavaScript Date or null
	 */
	public Object from( ScriptableObject scriptable, boolean javaScript );

	/**
	 * @param object
	 * @param rhino
	 *        True to create Rhino native objects, otherwise a java.util.HashMap
	 *        will be used
	 * @param javaScript
	 *        True to allow JavaScript literals (these will break JSON
	 *        compatibility!)
	 * @return A JavaScript object, a java.util.HashMap or null if not converted
	 */
	public Object to( Object object, boolean rhino, boolean javaScript );
}
