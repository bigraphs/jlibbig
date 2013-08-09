#Makefile per generare il codice java di lexer e parser

all: parser lexer

parser:
	java -jar ../../../libs/beaver-cc-0.9.11.jar brs_parser.grammar
lexer:
	jflex brs_lexer.l
