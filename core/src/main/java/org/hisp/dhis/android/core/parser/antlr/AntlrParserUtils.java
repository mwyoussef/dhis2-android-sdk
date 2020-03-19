package org.hisp.dhis.android.core.parser.antlr;

/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.hisp.dhis.android.core.parser.antlr.operator.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hisp.dhis.android.core.parser.antlr.DateUtils.parseDate;
import static org.hisp.dhis.android.core.parser.expression.antlr.ExpressionParser.*;

/**
 * Utilities for ANTLR parsing
 *
 * @author Jim Grace
 */
public class AntlrParserUtils
{
    public final static double DOUBLE_VALUE_IF_NULL = 0.0;

    public final static boolean BOOLEAN_VALUE_IF_NULL = false;

    public final static Map<Integer, AntlrExprItem> ANTLR_EXPRESSION_ITEMS;

    static {
        Map<Integer, AntlrExprItem> m = new HashMap<>();
        m.put( PAREN, new AntlrOperatorGroupingParentheses() );
        m.put( PLUS, new AntlrOperatorMathPlus() );
        m.put( MINUS, new AntlrOperatorMathMinus() );
        m.put( POWER, new AntlrOperatorMathPower() );
        m.put( MUL, new AntlrOperatorMathMultiply() );
        m.put( DIV, new AntlrOperatorMathDivide() );
        m.put( MOD, new AntlrOperatorMathModulus() );
        m.put( NOT, new AntlrOperatorLogicalNot() );
        m.put( EXCLAMATION_POINT, new AntlrOperatorLogicalNot() );
        m.put( AND, new AntlrOperatorLogicalAnd() );
        m.put( AMPERSAND_2, new AntlrOperatorLogicalAnd() );
        m.put( OR, new AntlrOperatorLogicalOr() );
        m.put( VERTICAL_BAR_2, new AntlrOperatorLogicalOr() );

        // Comparison operators

        m.put( EQ, new AntlrOperatorCompareEqual() );
        m.put( NE, new AntlrOperatorCompareNotEqual() );
        m.put( GT, new AntlrOperatorCompareGreaterThan() );
        m.put( LT, new AntlrOperatorCompareLessThan() );
        m.put( GEQ, new AntlrOperatorCompareGreaterThanOrEqual() );
        m.put( LEQ, new AntlrOperatorCompareLessThanOrEqual() );

        ANTLR_EXPRESSION_ITEMS = m;
    }

    /**
     * Trim quotes from the first and last characters of a string.
     * The string must be at least two characters long.
     * The first character must be either a single or double quote.
     * The last character must be the same as the first character.
     *
     * @param str the quoted string
     * @return the unquoted string
     */
    public static String trimQuotes( String str )
    {
        if ( str.length() < 2
            || ! "'\"".contains( str.substring( 0, 1 ) )
            || str.charAt( 0 ) != str.charAt( str.length() - 1 ) )
        {
            throw new ParserExceptionWithoutContext(
                "Internal parsing error: unquoted string '" + str + "'" );
        }

        return str.substring(1, str.length() - 1);
    }

    /**
     * Casts object as Double, or throws exception.
     * <p/>
     * If the object is null, return null.
     *
     * @param object the value to cast as a Double.
     * @return Double value.
     */
    public static Double castDouble( Object object )
    {
        return (Double) castClass( Double.class, object );
    }

    /**
     * Casts object as Double, or throws exception.
     * <p/>
     * If the object is null, returns a default.
     *
     * @param object the value to cast as a Double.
     * @return Double value.
     */
    public static Double castDoubleDefault( Object object )
    {
        return object == null
            ? DOUBLE_VALUE_IF_NULL
            : (Double) castClass( Double.class, object );
    }

    /**
     * Casts object as Boolean, or throws exception.
     * <p/>
     * If the object is null, return null.
     *
     * @param object the value to cast as a Boolean.
     * @return Boolean value.
     */
    public static Boolean castBoolean( Object object )
    {
        return (Boolean) castClass( Boolean.class, object );
    }

    /**
     * Casts object as Boolean, or throws exception.
     * <p/>
     * If the object is null, returns a default.
     *
     * @param object the value to cast as a Boolean.
     * @return Boolean value.
     */
    public static Boolean castBooleanDefault( Object object )
    {
        return object == null
            ? BOOLEAN_VALUE_IF_NULL
            : (Boolean) castClass( Boolean.class, object );
    }

    /**
     * Casts object as String, or throws exception.
     *
     * @param object the value to cast as a String.
     * @return String value.
     */
    public static String castString( Object object )
    {
        return (String) castClass( String.class, object );
    }

    /**
     * Parses an object from String to Date, or throws exception.
     *
     * @param object the value to cast as a String.
     * @return String value.
     */
    public static Date castDate( Object object )
    {
        return (Date) castClass( Date.class, object );
    }

    /**
     * Checks to see whether object can be cast to the class specified,
     * or throws exception if it can't.
     *
     * @param clazz the class: Double, Boolean, or String
     * @param object the value to cast
     * @return object (if it can be cast to that class.)
     */
    public static Object castClass( Class<?> clazz, Object object )
    {
        if ( object instanceof Double )
        {
            if ( clazz == String.class )
            {
                return object.toString();
            }
            else if ( clazz != Double.class )
            {
                throw new ParserExceptionWithoutContext( "Found number when expecting " + clazz.getSimpleName() );
            }
        }

        if ( object instanceof String )
        {
            if ( clazz == Date.class )
            {
                try
                {
                    return parseDate( (String) object );
                }
                catch ( Exception e )
                {
                    throw new ParserExceptionWithoutContext( "Found '" + object + "' when expecting a date" );
                }
            }
            else if ( clazz == Double.class )
            {
                try
                {
                    return Double.parseDouble( (String) object );
                }
                catch ( Exception e )
                {
                    throw new ParserExceptionWithoutContext( "Found '" + object + "' when expecting a number" );
                }
            }
            else if ( clazz == String.class )
            {
                return object;
            }
            else if ( clazz != String.class )
            {
                throw new ParserExceptionWithoutContext( "Found string when expecting " + clazz.getSimpleName() );
            }
        }

        if ( object instanceof Boolean && clazz != Boolean.class )
        {
            throw new ParserExceptionWithoutContext( "Found boolean value when expecting " + clazz.getSimpleName() );
        }

        try
        {
            return clazz.cast( object );
        }
        catch ( Exception e )
        {
            throw new ParserExceptionWithoutContext( "Could not cast value to " + clazz.getSimpleName() );
        }
    }
}