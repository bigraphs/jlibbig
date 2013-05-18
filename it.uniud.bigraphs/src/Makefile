#Makefile per generare il codice java di lexer e parser

all: parser lexer

parser:
	java -jar ../libs/beaver-cc-0.9.11.jar jlibbig/parser/parser.grammar 
	mv jlibbig/parser/BigraphParser.java jlibbig/BigraphParser.java
lexer:
	jflex jlibbig/parser/lexer.l
	mv jlibbig/parser/BigraphLexer.java jlibbig/BigraphLexer.java
