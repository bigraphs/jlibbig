package jlibbig.udlang;

import beaver.Symbol;
import beaver.Scanner;

import jlibbig.udlang.BRSParser.Terminals;

%%

%class BRSLexer
%extends Scanner
%function nextToken
%type Symbol
%yylexthrow Scanner.Exception
%unicode
%line
%column
%eofval{
	return new Symbol( Terminals.EOF, yyline + 1, yycolumn + 1, yylength() );
%eofval}
%{
	private Symbol newToken( short id )
	{
		return new Symbol( id, yyline + 1, yycolumn + 1, yylength() );
	}

	private Symbol newToken( short id, Object value )
	{
		return new Symbol( id, yyline + 1, yycolumn + 1, yylength(), value );
	}
%}

Letter = [a-zA-Z]
Whites = [ \n\t\f\r]
Digit = [0-9]

%%

<YYINITIAL> {
	
	"%active"	{ return newToken(Terminals.MODE , true ); }
	"%passive"	{ return newToken(Terminals.MODE , false ); }

	"%" 	{ return newToken(Terminals.PERCENT); }
	";"	{ return newToken(Terminals.SEMICOLON); }
	":"	{ return newToken(Terminals.COLON); }
	"->"	{ return newToken(Terminals.REACT); }
	
	"nil"	{ return newToken(Terminals.NIL); }
	
	"0"			{ return newToken( Terminals.ZERO ); }
	"1"			{ return newToken( Terminals.ONE ); }

	{Digit}+			{ return newToken( Terminals.NUM, Integer.parseInt( yytext() ) ); }
	{Letter}({Letter}|{Digit}|"_")*	{ return newToken( Terminals.VARID, new String( yytext() ) ); }
	
	"."	{ return newToken(Terminals.POINT); }
	"|"	{ return newToken(Terminals.PIPE); }
	"||"	{ return newToken(Terminals.DPIPE); }
	"-"	{ return newToken(Terminals.DASH); }
	"+"	{ return newToken(Terminals.PLUS); }
	","	{ return newToken(Terminals.COMMA); }
	
	"["	{ return newToken(Terminals.SQOPEN); }
	"]"	{ return newToken(Terminals.SQCLOSE); }
	
	"("	{ return newToken(Terminals.PAROPEN); }
	")"	{ return newToken(Terminals.PARCLOSE); }

	"<"	{ return newToken(Terminals.TAGOPEN); }
	">"	{ return newToken(Terminals.TAGCLOSE); }
	"/"	{ return newToken(Terminals.SLASH); }
		
	"$"	{ return newToken(Terminals.SIGIL); }
	
	#.* | {Whites}	{}
	.	{ throw new Scanner.Exception("Illegal character <" + yytext() + ">"); }
}
