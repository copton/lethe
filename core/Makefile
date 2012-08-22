SUBDIRS = generator grid stormlog client manager authservice sourceservice 

include config.mak

.PHONY: all clean $(SUBDIRS) central simulation user sources

all clean:
	for i in $(SUBDIRS); do $(MAKE) -C $$i $@; done

$(SUBDIRS):
	$(MAKE) -C $@

center: grid stormlog manager authservice sourceservice

node: grid generator

user: grid client

sources: grid sourceservice
