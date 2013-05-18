package jlibbig;

import beaver.Symbol;
import beaver.Scanner;

import jlibbig.BigraphParser.Terminals;

%%

%class BigraphLexer
%extends Scanner
%function nextToken
%type Symbol
%yylexthrow Scanner.Exception
%unicode
%line
%column
%eofval{
	return new Symbol(Terminals.EOF, yyline + 1, yycolumn + 1, yylength());
%eofval}
%{
	private Symbol newToken(short id)
	{
		return new Symbol(id, yyline + 1, yycolumn + 1, yylength());
	}

	private Symbol newToken(short id, Object value)
	{
		return new Symbol(id, yyline + 1, yycolumn + 1, yylength(), value);
	}
%}

Letter = [a-zA-Z]
Whites = [ \n\t\f\r]
Digit = [0-9]

%%

<YYINITIAL> {
	"%active"		{ return newToken(Terminals.ACTIVE); }
	"%passive"		{ return newToken(Terminals.PASSIVE); }
	"%name"|"%outer"	{ return newToken(Terminals.OUTERNAME); }
	"%inner"		{ return newToken(Terminals.INNERNAME); }
	
	":"{Whites}*"o" { return newToken(Terminals.OUTER); }
	":"{Whites}*"i" { return newToken(Terminals.INNER); }
	":"{Whites}*"e" { return newToken(Terminals.EDGE); }
	
	"<-"	{ return newToken(Terminals.LINKED); }
	
	";"	{ return newToken(Terminals.SEMICOLON); }
	":"	{ return newToken(Terminals.COLON); }
	
	"NIL"	{ return newToken(Terminals.NIL); }
	
	{Digit}+			{ return newToken(Terminals.NUM, new Integer(yytext())); }
	{Letter}({Letter}|{Digit})*	{ return newToken(Terminals.VARID, new String(yytext())); }
	
	"."	{ return newToken(Terminals.POINT); }
	"|"	{ return newToken(Terminals.PIPE); }
	"||"	{ return newToken(Terminals.DPIPE); }
	"-"	{ return newToken(Terminals.UNLINKED); }
	","	{ return newToken(Terminals.COMMA); }
	
	"["	{ return newToken(Terminals.SQOPEN); }
	"]"	{ return newToken(Terminals.SQCLOSE); }
	
	"("	{ return newToken(Terminals.PAROPEN); }
	")"	{ return newToken(Terminals.PARCLOSE); }	
		
	"$"	{ return newToken(Terminals.INNERPLACE); }
	"->"	{ return newToken(Terminals.REACT); }
	
	{Whites} {} 
	#.*	{}
	.	{ throw new Scanner.Exception("Illegal character <" + yytext() + ">"); }
}
