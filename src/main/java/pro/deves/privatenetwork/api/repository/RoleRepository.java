package pro.deves.privatenetwork.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.deves.privatenetwork.api.model.Role;
import pro.deves.privatenetwork.api.model.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName roleName);
}
