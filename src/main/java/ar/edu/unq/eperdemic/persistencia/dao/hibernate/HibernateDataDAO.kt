package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.TipoDeVector
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.NeoUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongoDB.MongoDBEventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

open class HibernateDataDAO : DataDAO {
    override fun crearSetDeDatosIniciales() {
        val ubicacionService : UbicacionService = UbicacionServiceImpl(UbicacionNeo4jDAO(),HibernateUbicacionDAO(), this, HibernateVectorDAO(), MongoDBEventoDAO())
        val vectorService : VectorService = VectorServiceImpl(HibernateVectorDAO(), this, HibernateEspecieDAO(),HibernateUbicacionDAO(), MongoDBEventoDAO())
        val patogenoService : PatogenoService = PatogenoServiceImpl(HibernatePatogenoDAO(), this, MongoDBEventoDAO())
        var ubicacion: Ubicacion =  ubicacionService.crear("China")
        vectorService.crear(TipoDeVector.Persona,ubicacion.id!!)
        val virus : Patogeno = patogenoService.crear(Patogeno("virus",1,1,1))
        patogenoService.agregarEspecie(virus.id!!,"coronavirus",ubicacion.id!!)

    }

    override fun clear() {
        val session = TransactionRunner.currentSession
        val nombreDeTablas = session.createNativeQuery("show tables").resultList
        session.createNativeQuery("SET FOREIGN_KEY_CHECKS=0;").executeUpdate()
        nombreDeTablas.forEach { result ->
            var tabla = ""
            when(result){
                is String -> tabla = result
                is Array<*> -> tabla= result[0].toString()
            }
            session.createNativeQuery("truncate table $tabla").executeUpdate()
        }
        session.createNativeQuery("SET FOREIGN_KEY_CHECKS=1;").executeUpdate()
    }
}