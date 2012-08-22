.PHONY: all clean

PARTS = registry node center
TARGETS = $(PARTS) $(foreach part, $(PARTS), $(part).cfg) lethe.xml

CWD = $(shell pwd)

SUBSTITUTIONS = NAMING_HOST NAMING_PORT ENDPOINTS CWD CONFIGURATION_FILE TIME_SLICE SORTING_ALGORITHM SYSTEM_LOG WORKING_DIR SVN_CMD SVN_USER SVN_PASSWORD SVN_PATH_TO_REPOSITORY SVN_PATH_IN_REPOSITORY FILE_PATH SOURCE_TEMP_DIR SLICE_PATH

SUBSTITUTION_MATCH = $(foreach subst,$(SUBSTITUTIONS),-e "s|__$(subst)__|$($(subst))|")

all: $(TARGETS)

registry node center:
	mkdir $@

%: %.tmpl
	sed $(SUBSTITUTION_MATCH) < $< > $@

clean:
	rm -fr $(TARGETS)
	
