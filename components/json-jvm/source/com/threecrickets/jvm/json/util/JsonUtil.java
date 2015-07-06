/**
 * Copyright 2010-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the Mozilla Public
 * License version 1.1: http://www.mozilla.org/MPL/MPL-1.1.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.jvm.json.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.threecrickets.jvm.json.internal.DtoA;
import com.threecrickets.jvm.json.internal.FastDtoA;

/**
 * JavaScript formatting utilities.
 * 
 * @author Tal Liron
 */
public class JsonUtil
{
	//
	// Static operations
	//

	/**
	 * Escape literal strings (assumes they are surrounded by double quotes).
	 * 
	 * @param string
	 *        The string
	 * @return The escaped string
	 */
	public static CharSequence escapeCharSequence( CharSequence string )
	{
		for( int i = 0, length = ESCAPE_PATTERNS.length; i < length; i++ )
			string = ESCAPE_PATTERNS[i].matcher( string ).replaceAll( ESCAPE_REPLACEMENTS[i] );
		return string;
	}

	/**
	 * Encodes a number as a JSON-compatible string.
	 * 
	 * @param number
	 *        The number
	 * @return The number encoded as a string
	 */
	public static String numberToString( Number number )
	{
		return numberToString( number.doubleValue(), 10 );
	}

	/**
	 * Encodes a number as a JSON-compatible string.
	 *
	 * @param number
	 *        The number
	 * @param radix
	 *        The radix
	 * @return The number encoded as a string
	 */
	public static String numberToString( double number, int radix )
	{
		// Code taken from: org.mozilla.javascript.ScriptRuntime

		if( ( radix < 2 ) || ( radix > 36 ) )
			throw new NumberFormatException( "Unsupported radix: " + radix );

		if( number != number )
			return "NaN";
		if( number == Double.POSITIVE_INFINITY )
			return "Infinity";
		if( number == Double.NEGATIVE_INFINITY )
			return "-Infinity";
		if( number == 0.0 )
			return "0";

		if( radix != 10 )
			return DtoA.JS_dtobasestr( radix, number );
		else
		{
			// V8 FastDtoA can't convert all numbers, so try it first but fall
			// back to old DtoA in case it fails
			String result = FastDtoA.numberToString( number );
			if( result != null )
				return result;
			StringBuilder buffer = new StringBuilder();
			DtoA.JS_dtostr( buffer, DtoA.DTOSTR_STANDARD, 0, number );
			return buffer.toString();
		}
	}

	/**
	 * Decodes a JSON-compatible number from string.
	 * 
	 * @param string
	 *        The string
	 * @return The number
	 */
	public static double stringToNumber( CharSequence string )
	{
		// Code taken from: org.mozilla.javascript.ScriptRuntime

		int len = string.length();
		int start = 0;
		char startChar;
		for( ;; )
		{
			if( start == len )
			{
				// Empty or contains only whitespace
				return +0.0;
			}
			startChar = string.charAt( start );
			if( !Character.isWhitespace( startChar ) )
				break;
			start++;
		}

		if( startChar == '0' )
		{
			if( start + 2 < len )
			{
				int c1 = string.charAt( start + 1 );
				if( c1 == 'x' || c1 == 'X' )
					// A hexadecimal number
					return stringToNumber( string, start + 2, 16 );
			}
		}
		else if( startChar == '+' || startChar == '-' )
		{
			if( start + 3 < len && string.charAt( start + 1 ) == '0' )
			{
				int c2 = string.charAt( start + 2 );
				if( c2 == 'x' || c2 == 'X' )
				{
					// A hexadecimal number with sign
					double val = stringToNumber( string, start + 3, 16 );
					return startChar == '-' ? -val : val;
				}
			}
		}

		int end = len - 1;
		char endChar;
		while( Character.isWhitespace( endChar = string.charAt( end ) ) )
			end--;
		if( endChar == 'y' )
		{
			// check for "Infinity"
			if( startChar == '+' || startChar == '-' )
				start++;
			if( start + 7 == end && string.toString().regionMatches( start, "Infinity", 0, 8 ) )
				return startChar == '-' ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
			return Double.NaN;
		}
		// A non-hexadecimal, non-infinity number:
		// just try a normal floating point conversion
		CharSequence sub = string.subSequence( start, end + 1 );
		// Quick test to check string contains only valid characters because
		// Double.parseDouble() can be slow and accept input we want to reject
		for( int i = sub.length() - 1; i >= 0; i-- )
		{
			char c = sub.charAt( i );
			if( ( '0' <= c && c <= '9' ) || c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-' )
				continue;
			return Double.NaN;
		}
		try
		{
			return Double.parseDouble( sub.toString() );
		}
		catch( NumberFormatException x )
		{
			return Double.NaN;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static Pattern[] ESCAPE_PATTERNS = new Pattern[]
	{
		Pattern.compile( "\\\\" ), Pattern.compile( "\\n" ), Pattern.compile( "\\r" ), Pattern.compile( "\\t" ), Pattern.compile( "\\f" ), Pattern.compile( "\\\"" )
	};

	private static String[] ESCAPE_REPLACEMENTS = new String[]
	{
		Matcher.quoteReplacement( "\\\\" ), Matcher.quoteReplacement( "\\n" ), Matcher.quoteReplacement( "\\r" ), Matcher.quoteReplacement( "\\t" ), Matcher.quoteReplacement( "\\f" ), Matcher.quoteReplacement( "\\\"" )
	};

	private static double stringToNumber( CharSequence string, int start, int radix )
	{
		// Code taken from: org.mozilla.javascript.ScriptRuntime

		char digitMax = '9';
		char lowerCaseBound = 'a';
		char upperCaseBound = 'A';
		int len = string.length();
		if( radix < 10 )
			digitMax = (char) ( '0' + radix - 1 );
		if( radix > 10 )
		{
			lowerCaseBound = (char) ( 'a' + radix - 10 );
			upperCaseBound = (char) ( 'A' + radix - 10 );
		}
		int end;
		double sum = 0.0;
		for( end = start; end < len; end++ )
		{
			char c = string.charAt( end );
			int newDigit;
			if( '0' <= c && c <= digitMax )
				newDigit = c - '0';
			else if( 'a' <= c && c < lowerCaseBound )
				newDigit = c - 'a' + 10;
			else if( 'A' <= c && c < upperCaseBound )
				newDigit = c - 'A' + 10;
			else
				break;
			sum = sum * radix + newDigit;
		}
		if( start == end )
			return Double.NaN;
		if( sum >= 9007199254740992.0 )
		{
			if( radix == 10 )
			{
				/*
				 * If we're accumulating a decimal number and the number is >=
				 * 2^53, then the result from the repeated multiply-add above
				 * may be inaccurate. Call Java to get the correct answer.
				 */
				try
				{
					return Double.parseDouble( string.subSequence( start, end ).toString() );
				}
				catch( NumberFormatException x )
				{
					return Double.NaN;
				}
			}
			else if( radix == 2 || radix == 4 || radix == 8 || radix == 16 || radix == 32 )
			{
				/*
				 * The number may also be inaccurate for one of these bases.
				 * This happens if the addition in value*radix + digit causes a
				 * round-down to an even least significant mantissa bit when the
				 * first dropped bit is a one. If any of the following digits in
				 * the number (which haven't been added in yet) are nonzero then
				 * the correct action would have been to round up instead of
				 * down. An example of this occurs when reading the number
				 * 0x1000000000000081, which rounds to 0x1000000000000000
				 * instead of 0x1000000000000100.
				 */
				int bitShiftInChar = 1;
				int digit = 0;

				final int SKIP_LEADING_ZEROS = 0;
				final int FIRST_EXACT_53_BITS = 1;
				final int AFTER_BIT_53 = 2;
				final int ZEROS_AFTER_54 = 3;
				final int MIXED_AFTER_54 = 4;

				int state = SKIP_LEADING_ZEROS;
				int exactBitsLimit = 53;
				double factor = 0.0;
				boolean bit53 = false;
				// bit54 is the 54th bit (the first dropped from the mantissa)
				boolean bit54 = false;

				for( ;; )
				{
					if( bitShiftInChar == 1 )
					{
						if( start == end )
							break;
						digit = string.charAt( start++ );
						if( '0' <= digit && digit <= '9' )
							digit -= '0';
						else if( 'a' <= digit && digit <= 'z' )
							digit -= 'a' - 10;
						else
							digit -= 'A' - 10;
						bitShiftInChar = radix;
					}
					bitShiftInChar >>= 1;
					boolean bit = ( digit & bitShiftInChar ) != 0;

					switch( state )
					{
						case SKIP_LEADING_ZEROS:
							if( bit )
							{
								--exactBitsLimit;
								sum = 1.0;
								state = FIRST_EXACT_53_BITS;
							}
							break;
						case FIRST_EXACT_53_BITS:
							sum *= 2.0;
							if( bit )
								sum += 1.0;
							--exactBitsLimit;
							if( exactBitsLimit == 0 )
							{
								bit53 = bit;
								state = AFTER_BIT_53;
							}
							break;
						case AFTER_BIT_53:
							bit54 = bit;
							factor = 2.0;
							state = ZEROS_AFTER_54;
							break;
						case ZEROS_AFTER_54:
							if( bit )
							{
								state = MIXED_AFTER_54;
							}
							// fallthrough
						case MIXED_AFTER_54:
							factor *= 2;
							break;
					}
				}
				switch( state )
				{
					case SKIP_LEADING_ZEROS:
						sum = 0.0;
						break;
					case FIRST_EXACT_53_BITS:
					case AFTER_BIT_53:
						// do nothing
						break;
					case ZEROS_AFTER_54:
						// x1.1 -> x1 + 1 (round up)
						// x0.1 -> x0 (round down)
						if( bit54 & bit53 )
							sum += 1.0;
						sum *= factor;
						break;
					case MIXED_AFTER_54:
						// x.100...1.. -> x + 1 (round up)
						// x.0anything -> x (round down)
						if( bit54 )
							sum += 1.0;
						sum *= factor;
						break;
				}
			}
			/* We don't worry about inaccurate numbers for any other base. */
		}
		return sum;
	}
}
