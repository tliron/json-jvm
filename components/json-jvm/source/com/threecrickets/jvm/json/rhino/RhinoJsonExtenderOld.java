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

package com.threecrickets.jvm.json.rhino;

import org.mozilla.javascript.ScriptableObject;

public interface RhinoJsonExtenderOld
{
	/**
	 * @param scriptableObject
	 *        The JavaScript object
	 * @param rhino
	 *        True to prefer conversion to Rhino native objects (Date, RegExp,
	 *        etc.)
	 * @return A new object or null if not converted
	 */
	public Object from( ScriptableObject scriptableObject, boolean rhino );

	/**
	 * @param object
	 *        The object
	 * @param rhino
	 *        True to create Rhino native objects, otherwise a java.util.HashMap
	 *        will be used
	 * @param allowCode
	 *        True to allow language code (this will break JSON compatibility!)
	 * @return A JavaScript value or null if not converted
	 */
	public Object to( Object object, boolean rhino, boolean allowCode );
}
