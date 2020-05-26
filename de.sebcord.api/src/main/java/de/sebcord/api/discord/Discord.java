package de.sebcord.api.discord;

import java.util.List;

public interface Discord {

	List<Role> getRoles();
	List<Permission> getPermissions();
	List<Member> getMembers();
	List<Category> getCategories();

	
}
