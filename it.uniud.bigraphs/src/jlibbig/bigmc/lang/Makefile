#Makefile per generare il codice java di lexer e parser

all: parser lexer

parser:
	java -jar ../../../../libs/beaver-cc-0.9.11.jar parser.grammar 
#	mv bigmc/BigMCParser.java ../jlibbig/bigmc/lang/BigMCParser.java
lexer:
	jflex lexer.l
#	mv bigmc/BigMCLexer.java ../jlibbig/bigmc/lang/BigMCLexer.java
