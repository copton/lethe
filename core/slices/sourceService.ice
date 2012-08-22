
#ifndef __SOURCESERVICE_ICE__
#define __SOURCESERVICE_ICE__

#include <exceptions.ice>
#include <callbacks.ice>
#include <IcePatch2/FileServer.ice>
#include <Ice/Identity.ice>

module Comm {
    module SourceService {
        class SourceDesc {};

		enum Filter {XML, CODE, ALL, CORE};
		sequence<string> StringSeq;

        interface PublicInterface {
            idempotent void announce(string jobId, StringSeq files, Filter f, SourceDesc source)
                throws ::Comm::Exceptions::RessourceNotFoundException;

			idempotent ProtectedInterface* getInterface();

			idempotent StringSeq getAvailableModules(SourceDesc source);
			string syncFiles(SourceDesc source, StringSeq files);
			nonmutating void synched(string localProxy);
		};


		struct PatchServerWrapper {
			int counter;
			string jobId;
			::Ice::Identity identity;
			::IcePatch2::FileServer* server;		
		};

        class SvnSource extends SourceDesc {
            int revision;
		};

        class LocalSource extends SourceDesc {
            string path;
        };
    };
};

#endif
