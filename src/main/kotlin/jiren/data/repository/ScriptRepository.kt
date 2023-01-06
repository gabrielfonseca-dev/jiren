package jiren.data.repository

import jiren.data.entity.Script
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ScriptRepository : JpaRepository<Script, Long>, JpaSpecificationExecutor<Script> {
    fun findByName(name: String): Script?
    @Query("select s from script s where s.active = 1")
    fun findByScriptActiveTrue(): List<Script>
}