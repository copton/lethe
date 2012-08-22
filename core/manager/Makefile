JAR = comm.jar icepatch.jar manager.jar

JAVA_PACKAGES = algorithm callbacks manager scheduler server

JAVA_FILES = $(shell find $(JAVA_PACKAGES) -name "*.java")

USED_LIBS = Ice.jar

JAVA_CLASSPATH=.:$(subst $(SPACE),:,$(addprefix $(PATH_TO_LIBS)/,$(USED_LIBS)))

all: $(JAR)

%.class: %.java
	javac $(JAVA_FLAGS) -classpath $(JAVA_CLASSPATH) $^

Comm:
	slice2java $(SLICE_FLAGS) $(SLICE_FILES)
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.Manager.PersistantJobs,string,::Comm::Job::Persistence ../slices/persistentJob.ice
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.Manager.PersistentSimulations,string,::Comm::Manager::SimulationObject ../slices/persistentSimulation.ice
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.Manager.JobIds,string,int
	$(FREEZE_J) $(SLICE_FLAGS) --dict Comm.Manager.SuffixMap,string,::Comm::Simulation::\Interface* ../slices/simulation.ice  
	javac $(JAVA_FLAGS) -classpath $(JAVA_CLASSPATH) Comm/*/*.java Comm/*/*/*.java

comm.jar: Comm
	jar -cf $@ Comm

icepatch.jar: IcePatch2
	jar -cf $@ IcePatch2

manager.jar: $(JAVA_FILES:%.java=%.class) $(JAVA_FILES)
	jar -cf $@ $(JAVA_FILES) $^

clean:
	rm -fr $(JAR)
	find . -iname '*.class' | xargs rm -fr
	rm -fr db/*    
	rm -fr Comm

