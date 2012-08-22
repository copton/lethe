JAR = comm.jar client.jar icepatch.jar

JAVA_PACKAGES = client dialog event gui plugins util xml

JAVA_FILES = $(shell find $(JAVA_PACKAGES) -name "*.java")

USED_LIBS = Ice.jar db.jar jdom.jar saxon8-jdom.jar saxon8.jar xercesImpl.jar xml-apis.jar

JAVA_CLASSPATH = .:$(subst $(SPACE),:,$(addprefix $(PATH_TO_LIBS)/,$(USED_LIBS)))

PROGRAMS = lethe userManagement resultExporter packageGenerator moduleFinder

CWD = $(shell pwd)

SUBSTITUTIONS = NAMING_HOST NAMING_PORT SLICE_COMPILER PATH_TO_XML PATH_TO_MODULES PATH_TO_TYPES PATH_TO_JOBS PLUGIN_DIR TEMP_DIR CWD

SUBSTITUTION_MATCH = $(foreach subst,$(SUBSTITUTIONS),-e "s|__$(subst)__|$($(subst))|")

all:  configuration $(JAR) tmp $(PROGRAMS)

%: %.tmpl
	sed $(SUBSTITUTION_MATCH) <$< > $@

Comm: $(SLICE_FILES)
	slice2java $(SLICE_FLAGS) $(SLICE_FILES)
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.Manager.PersistantJobs,string,::Comm::Job::Persistence ../slices/persistentJob.ice
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.SourceService.PersistentSources,string,::Comm::SourceService::PatchServerWrapper ../slices/sourceService.ice
	
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.Manager.PersistentSimulations,string,::Comm::Manager::SimulationObject ../slices/persistentSimulation.ice
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.Manager.JobIds,string,int
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.Manager.SuffixMap,string,::Comm::Simulation::\Interface* ../slices/simulation.ice

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

client.jar: $(JAVA_FILES:%.java=%.class)
	jar -cf $@ $(JAVA_FILES) $^  

userManagement packageGenerator moduleFinder: $(shell find client -iname "$@.class")
	echo "#!/bin/sh" > $@
	echo >> $@
	echo 'java -classpath $(JAVA_CLASSPATH):comm.jar:client.jar $(basename $(shell find client -iname $@.class)) $$*' >> $@
	chmod +x $@

resultExporter:
	echo "#!/bin/sh" > $@
	echo >> $@
	echo 'java -classpath $(JAVA_CLASSPATH):tmp:comm.jar:client.jar $(basename $(shell find client -iname $@.class)) $$*' >> $@
	chmod +x $@

lethe:
	echo "#!/bin/sh" > $@
	echo >> $@
	echo "java -classpath $(JAVA_CLASSPATH):$(TEMP_DIR):comm.jar:client.jar gui.LetheWindow $$*" >> $@
	chmod +x $@

clean:
	rm -fr $(JAR)
	rm $(JAVA_FILES:%.java=%.class)
	rm -f configuration
	rm -fr tmp
	rm -fr Comm
