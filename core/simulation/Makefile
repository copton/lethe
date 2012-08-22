include config.mak
include gen/config.mak

TARGETS = comm core modules types gen 

.PHONY: all clean $(TARGETS) link fix-broken-dep

ifeq "$(origin MAKECMDGOALS)" "undefined"
GOAL = all
else
GOAL = $(MAKECMDGOALS)
endif

all: fix-broken-dep $(TARGETS)

clean: $(TARGETS) 

fix-broken-dep:
	rm -f gen/main.o

comm core gen link: MAKEFILES = $(GLOBAL_CONFIG) $(CURDIR)/Makefile.$@

comm:
	@$(MAKE) -C $@ ROOT=$(CURDIR) SLICE_PATH=$(SLICE_PATH) $(GOAL)

core:
	@$(MAKE) -C $@ ROOT=$(CURDIR) $(GOAL)
	
gen:
	@$(MAKE) -C $@ ROOT=$(CURDIR) EXT_PATH=$(EXT_PATH) $(GOAL)
	
link:
	@$(MAKE) -C $@ ROOT=$(CURDIR) EXT_PATH=$(EXT_PATH) EXE_FILE=$(EXE_FILE) modules_LIST="$(modules_LIST)" types_LIST="$(types_LIST)" $(GOAL)

modules types: MAKEFILES = $(GLOBAL_CONFIG) $(CURDIR)/Makefile.extensions
modules types:
	@for i in $(${@}_LIST); do $(MAKE) -C $(EXT_PATH)/$@/$$i ROOT=$(CURDIR) $(GOAL); done


