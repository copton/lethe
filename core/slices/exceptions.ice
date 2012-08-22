
#ifndef __EXCEPTIONS_ICE
#define __EXCEPTIONS_ICE

module Comm {
	module Exceptions {
		exception SpecException {
            string error;
        };
		
		exception BuildException {
			string error;
		};
		
		exception SliceError extends BuildException {};
		exception CompileError extends BuildException {};
		exception LinkError extends BuildException {};

        exception ActionNotAllowed {
            string action;
        };

        exception StartupException {
            string reason;
        };
        
		exception RessourceNotFoundException {
			string resource;
		};

		exception UnknownUidException {
			string uid;
		};

		exception AuthenticationFailedException {
			string user;
			string reason;
		};
	};
};
#endif
