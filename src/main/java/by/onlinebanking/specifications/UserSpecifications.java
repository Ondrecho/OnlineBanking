package by.onlinebanking.specifications;

import by.onlinebanking.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
    private UserSpecifications() {}

    public static Specification<User> hasFullName(String fullName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("fullName"), "%" + fullName + "%");
    }

    public static Specification<User> hasRole(String roleName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.join("roles").get("name"), roleName);
    }
}