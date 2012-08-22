CC = g++
LD = $(CC)
SLICEC = /usr/bin/slice2cpp
AR = ar
RANLIB = ranlib

DA_PATH = /usr

GLOBAL_SLICEC_FLAGS = --stream -I.
GLOBAL_CCFLAGS = -O0 -g 
# GLOBAL_CCFLAGS = -O1 -DNDEBUG
GLOBAL_CCFLAGS += -Wall -Werror -I$(DA_PATH)/include
GLOBAL_LDFLAGS = -L$(DA_PATH)/lib -lIce

OBJECTS = $(patsubst %.cpp,%.o,$(wildcard *.cpp))

%.cpp %.h: %.ice
	$(SLICEC) $(SLICEC_FLAGS) $<

%.o: %.cpp
	$(CC) $(CCFLAGS) -c $<
