package com.example.sehomallapi.repository.users.userRoles;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RolesRepository extends JpaRepository<Roles, Integer> {
    Roles findByName(String name);
}
