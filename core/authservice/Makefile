JAR = comm.jar icepatch.jar authentication.jar

JAVA_FILES = $(shell find authentication -name "*.java")

USED_LIBS = Ice.jar jdom.jar

JAVA_CLASSPATH = .:$(subst $(SPACE),:,$(addprefix $(PATH_TO_LIBS)/,$(USED_LIBS)))

all: $(JAR)

%.class: %.java
	javac $(JAVA_FLAGS) -classpath $(JAVA_CLASSPATH) $^

Comm:
	slice2java $(SLICE_FLAGS) $(SLICE_FILES)
	javac $(JAVA_FLAGS) -classpath $(JAVA_CLASSPATH) Comm/*/*.java Comm/*/*/*.java

comm.jar: Comm
	jar -cf $@ Comm

icepatch.jar: IcePatch2
	jar -cf $@ IcePatch2

authentication.jar: $(JAVA_FILES:%.java=%.class)
	jar -cf $@ $(JAVA_FILES) $^

clean:
	rm -fr $(JAR)
	rm -fr *.class
	rm -fr Comm
