package jiren.data.repository

import jiren.data.entity.Parameter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface ParameterRepository : JpaRepository<Parameter, Long>, JpaSpecificationExecutor<Parameter> {
    fun findByCode(code: String): Parameter?
}