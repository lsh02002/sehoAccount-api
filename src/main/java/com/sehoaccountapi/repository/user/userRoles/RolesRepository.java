package com.sehoaccountapi.repository.user.userRoles;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<Roles, Integer> {
    Roles findByName(String name);
}
