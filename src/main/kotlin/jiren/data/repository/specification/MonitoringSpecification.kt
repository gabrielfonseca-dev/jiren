package jiren.data.repository.specification

import jiren.data.entity.Database
import jiren.data.entity.Monitoring
import jiren.data.enum.MonitoringType
import jiren.data.enum.StatusMonitoring
import org.springframework.data.jpa.domain.Specification

class MonitoringSpecification {

    fun name(name: String): Specification<Monitoring> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("name"),
                "%$name%"
            )
        }
    }

    fun command1(command1: String): Specification<Monitoring> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.like(
                root.get("queryOne"),
                "%$command1%"
            )
        }
    }

    fun inactive(active: Boolean): Specification<Monitoring> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.notEqual(root.get<Boolean>("enabled"), active)
        }
    }

    fun system(db: Database?): Specification<Monitoring> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<Database>("databaseOne"),
                db
            )
        }
    }

    fun status(status: StatusMonitoring?): Specification<Monitoring> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<StatusMonitoring>("status"),
                status
            )
        }
    }

    fun type(type: MonitoringType?): Specification<Monitoring> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<MonitoringType>("type"),
                type
            )
        }
    }

}