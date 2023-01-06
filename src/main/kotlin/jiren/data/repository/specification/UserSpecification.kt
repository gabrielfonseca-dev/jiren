package jiren.data.repository.specification

import jiren.data.entity.User
import org.springframework.data.jpa.domain.Specification

class UserSpecification {
    //@org.springframework.cache.annotation.Cacheable("userList")

    fun name(name: String): Specification<User> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("name"),
                "%$name%"
            )
        }
    }

    fun email(email: String): Specification<User> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("email"),
                "%$email%"
            )
        }
    }

    fun document(document: String): Specification<User> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("document"),
                "%$document%"
            )
        }
    }

    fun login(username: String): Specification<User> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("username"),
                "%$username%"
            )
        }
    }

    fun inactive(boolean: Boolean): Specification<User> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.notEqual(
                root.get<Boolean>("enabled"),
                boolean
            )
        }
    }

}