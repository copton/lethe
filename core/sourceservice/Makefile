JAR = comm.jar icepatch.jar sources.jar

JAVA_PACKAGES = controller service server

JAVA_FILES = SourceServer.java $(shell find $(JAVA_PACKAGES) -name "*.java")

USED_LIBS = Ice.jar db.jar

JAVA_CLASSPATH = .:$(subst $(SPACE),:,$(JAR)):$(subst $(SPACE),:,$(addprefix $(PATH_TO_LIBS)/,$(USED_LIBS)))

PROGRAMS = run

SUBSTITUTIONS = ENDPOINTS SOURCE_PORT SOURCE_PORT2 SLICE_PATH SOURCE_TEMP_DIR SVN_CMD SVN_USER SVN_PASSWORD SVN_PATH_TO_REPOSITORY SVN_PATH_IN_REPOSITORY FILE_PATH

SUBSTITUTION_MATCH = $(foreach subst,$(SUBSTITUTIONS),-e "s|__$(subst)__|$($(subst))|")


all:  configuration $(JAR) tmp $(PROGRAMS)

%: %.tmpl
	sed $(SUBSTITUTION_MATCH) < $< > $@

Comm: $(SLICE_FILES)
	slice2java $(SLICE_FLAGS) $(SLICE_FILES) 
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.SourceService.PersistentSources,string,::Comm::SourceService::PatchServerWrapper ../slices/sourceService.ice
	javac $(JAVA_FLAGS) -classpath $(JAVA_CLASSPATH) Comm/*/*.java Comm/*/*/*.java
	touch $@

%.class: %.java
	javac $(JAVA_FLAGS) -classpath $(JAVA_CLASSPATH) $<

tmp:
	mkdir $@

comm.jar: Comm
	jar -cf $@ Comm

icepatch.jar: IcePatch2
	jar -cf $@ IcePatch2

sources.jar: $(JAVA_FILES:%.java=%.class)
	jar -cf $@ $(JAVA_FILES) $^

run:
	echo "#!/bin/sh" > $@
	echo >> $@
	echo 'java -classpath $(JAVA_CLASSPATH):comm.jar:sources.jar SourceServer' >> $@ 

.PHONY: Comm IcePatch2

clean:
	rm -fr $(JAR)
	find . -name "*class" -exec rm {} \;
	rm -fr config.mak
	rm -f configuration
	rm -fr tmp
	rm -fr Comm
