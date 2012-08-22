# allgemein
export NAMING_HOST = 134.60.124.198 
export NAMING_PORT = 10000

export ENDPOINTS = 134.60.124.198 

export JAVA_FLAGS = -source 1.4
export PATH_TO_LIBS = ../../../libs

# Slice-Installation
export SLICE_PATH = /usr/share/slice
export SLICE_FILES = ../slices/*.ice $(SLICE_PATH)/IcePatch2/*.ice
export SLICE_COMPILER = /usr/bin/slice2java

# Client
export PATH_TO_XML = ../xml
export PATH_TO_MODULES = ../../extensions/modules
export PATH_TO_TYPES = ../../extensions/types
export PATH_TO_JOBS = ../../../jobs
export PLUGIN_DIR = plugins
export TEMP_DIR = tmp

# Manager
export TIME_SLICE = 3600
export SORTING_ALGORITHM = SainteLague

# Authentifizierungsdienst
export CONFIGURATION_FILE = authentication.config

# Breeze
export SYSTEM_LOG = /tmp/breeze

# Generator
export WORKING_DIR = ../generator

# Codeverteilung
export SOURCE_PORT = 10000
export SOURCE_PORT2 = 10001
export SOURCE_TEMP_DIR = tmp

export SVN_CMD = svn
export SVN_PATH_TO_REPOSITORY=
export SVN_PATH_IN_REPOSITORY=extensions
export SVN_USER= 
export SVN_PASSWORD=

export FILE_PATH=/NFS/home/phia/lethe/trunk/code 

# nicht ändern!
export SLICE_FLAGS = --ice -I$(SLICE_PATH) -I ../slices
export FREEZE_J = /usr/bin/slice2freezej 

empty =
export SPACE = $(empty) $(empty)

