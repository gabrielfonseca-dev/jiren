package jiren.data.repository.specification

import jiren.data.entity.Database
import jiren.data.entity.Script
import org.springframework.data.jpa.domain.Specification

class ScriptSpecification {

    fun name(name: String): Specification<Script> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("name"),
                "%$name%"
            )
        }
    }

    fun query(query: String): Specification<Script> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("query"),
                "%$query%"
            )
        }
    }

    fun description(description: String): Specification<Script> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("description"),
                "%$description%"
            )
        }
    }

    fun database(db: Database): Specification<Script> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<Database>("database"),
                db
            )
        }
    }

    fun inactive(active: Boolean): Specification<Script> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.notEqual(root.get<Boolean>("active"), active)
        }
    }

}