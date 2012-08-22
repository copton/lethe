.PHONY: all clean

JAR=util.jar

all: $(JAR)

$(JAR):
	javac -source 1.4 -classpath ".:../../../libs/Ice.jar" *.java
	jar -cf $@ *.class

clean:
	rm -fr $(JAR)
	rm -fr *.class
