package by.onlinebanking.specifications;

import by.onlinebanking.model.User;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
    private UserSpecifications() {}

    public static Specification<User> hasFullName(String fullName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("fullName"), "%" + fullName + "%");
    }

    public static Specification<User> hasRoles(List<String> roleNames) {
        return (root, query, cb) -> {
            if (roleNames == null || roleNames.isEmpty()) return null;
            return root.join("roles").get("name").in(roleNames);
        };
    }
}