include Makefile.git

export CLASSPATH=/usr/local/lib/antlr-*-complete.jar

DOMAINNAME = 47.122.3.40:3000
ANTLR = java -jar /usr/local/lib/antlr-*-complete.jar -listener -visitor -long-messages
JAVAC = javac -g
JAVA = java


PFILE = $(shell find . -name "SysYParser.g4")
LFILE = $(shell find . -name "SysYLexer.g4")
JAVAFILE = $(shell find . -name "*.java")
ANTLRPATH = $(shell find /usr/local/lib -name "antlr-*-complete.jar")


antlr: $(LFILE) $(PFILE) 
	$(ANTLR) $(PFILE) $(LFILE)



clean:
	rm -f src/antlr/*.tokens
	rm -f src/antlr/*.interp
	rm -f src/antlr/SysYLexer.java src/antlr/SysYParser.java src/antlr/SysYParserBaseListener.java src/antlr/SysYParserBaseVisitor.java src/antlr/SysYParserListener.java src/antlr/SysYParserVisitor.java
	rm -rf classes
	rm -f src/*.tokens
	rm -f src/*.interp
	rm -f src/SysYLexer.java src/SysYParser.java src/SysYParserBaseListener.java src/SysYParserBaseVisitor.java src/SysYParserListener.java src/SysYParserVisitor.java
	rm -rf classes


submit: clean
	git gc
	git commit -am "submit"
	bash submit.sh



.PHONY:  antlr clean submit


