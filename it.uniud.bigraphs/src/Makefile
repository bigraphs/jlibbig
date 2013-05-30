#Makefile per generare il codice java di lexer e parser

all: parser lexer

parser:
	java -jar ../libs/beaver-cc-0.9.11.jar parser_src/parser.grammar 
	mv parser_src/BigraphParser.java jlibbig/udlang/BigraphParser.java
lexer:
	jflex parser_src/lexer.l
	mv parser_src/BigraphLexer.java jlibbig/udlang/BigraphLexer.java
