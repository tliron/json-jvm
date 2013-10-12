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

package com.threecrickets.jvm.json.nashorn;

import jdk.nashorn.internal.runtime.ScriptObject;

public interface NashornJsonExtender
{
	/**
	 * @param script
	 *        The JavaScript object
	 * @param nashorn
	 *        True to prefer conversion to Nashorn native objects (Date, RegExp,
	 *        etc.)
	 * @return A new object or null if not converted
	 */
	public Object from( ScriptObject script, boolean nashorn );

	/**
	 * @param object
	 *        The object
	 * @param nashorn
	 *        True to create Nashorn native objects, otherwise a
	 *        java.util.HashMap will be used
	 * @param allowCode
	 *        True to allow language code (this will break JSON compatibility!)
	 * @return A JavaScript value or null if not converted
	 */
	public Object to( Object object, boolean nashorn, boolean allowCode );
}
