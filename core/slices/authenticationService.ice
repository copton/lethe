
#ifndef __AUTHENTICATIONSERVICE_ICE__
#define __AUTHENTICATIONSERVICE_ICE__

#include <exceptions.ice>

module Comm {
    module AuthenticationService {
		
		sequence<string> StringSeq;

		struct Action {
			string name;
			StringSeq onGroups;
		};
		sequence <Action> ActionSeq;

		struct Permission {
			string name;
			ActionSeq actions;
		};
		sequence <Permission> PermissionSeq;
		
		struct User {
			string name;
			string group;
			int priority;	
	
			PermissionSeq allowed;
			PermissionSeq denied;
		};
		sequence <User> UserSeq;

		struct Group {
			string name;
			string superGroup;
			int priority;

			PermissionSeq allowed;
			PermissionSeq denied;
		};
		sequence <Group> GroupSeq;

        interface PublicInterface {
            string authenticateUser(string user, string password)
                throws ::Comm::Exceptions::AuthenticationFailedException;

			nonmutating void logout(string user);
		};
	
		interface ProtectedInterface {
			nonmutating int getPriority(string user);			
			
			nonmutating bool userExists(string user, string sessionId);
			nonmutating bool hasPermission(string user, string owner, string permission);
        	nonmutating bool hasPermissions(string user, string owner, StringSeq permissions);
		};

		interface UserManagement {
			nonmutating User getUser(string name);
			nonmutating Group getGroup(string name);

			nonmutating UserSeq getAllUsers();
			nonmutating GroupSeq getAllGroups();
			nonmutating PermissionSeq getAllPermissions();

			nonmutating void addUser(User u, string password) 
					throws ::Comm::Exceptions::ActionNotAllowed;
			nonmutating void addGroup(Group g)
					throws ::Comm::Exceptions::ActionNotAllowed;

			nonmutating void setPassword(string user, string password)
					throws ::Comm::Exceptions::ActionNotAllowed;

			nonmutating void deleteUser(string name)
					throws ::Comm::Exceptions::ActionNotAllowed;
			nonmutating void deleteGroup(string name)
					throws ::Comm::Exceptions::ActionNotAllowed;
		
			nonmutating void setUser(string name, User u)
					throws ::Comm::Exceptions::ActionNotAllowed;
			nonmutating void setGroup(string name, Group g) 
					throws ::Comm::Exceptions::ActionNotAllowed;	
		};
	};
};

#endif
