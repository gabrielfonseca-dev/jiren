package jiren.data.repository.specification

import jiren.data.entity.Automation
import jiren.data.entity.Database
import jiren.data.enum.StatusAutomation
import org.springframework.data.jpa.domain.Specification

class AutomationSpecification {

    fun name(name: String): Specification<Automation> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("name"),
                "%$name%"
            )
        }
    }

    fun inactive(active: Boolean): Specification<Automation> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.notEqual(root.get<Boolean>("active"), active)
        }
    }

    fun query(query: String): Specification<Automation> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("query"),
                "%$query%"
            )
        }
    }

    fun database(db: Database): Specification<Automation> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<Database>("database"),
                db
            )
        }
    }

    fun status(statusAutomation: StatusAutomation?): Specification<Automation> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<StatusAutomation>("status"),
                statusAutomation
            )
        }
    }
}