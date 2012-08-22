FILES = $(wildcard *.xml-tag)
TARGETS = $(patsubst %.xml-tag, %, $(FILES))

all: $(TARGETS)

$(TARGETS):
	rm -fr $@
	mkdir $@
	cp $@.xml-tag $@/$@.xml
	(cd ~/diplomarbeit/svnroot/trunk/code/core/client && ./packageGenerator ~/diplomarbeit/svnroot/trunk/code/extensions/modules/$@/$@.xml)

clean:
	rm -fr $(TARGETS)
