package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

open class HibernateUbicacionDAO:HibernateDAO<Ubicacion>(Ubicacion::class.java), UbicacionDAO {

    override fun recuperar(id: Long): Ubicacion {
        return super.recuperar(id)
    }

    override fun actualizar(ubicacion: Ubicacion) {
        super.update(ubicacion)
    }

    override fun cantidadDeVectoresPresentes(ubicacionId: Long): Int {
        val session = TransactionRunner.currentSession
        val hql = ("""select count(vector) as presentes
            from Vector as vector 
            where vector.ubicacion.id = :id""")
        val query = session.createQuery(hql, java.lang.Long::class.java)
        query.setParameter("id", ubicacionId)
        return query.singleResult.toInt()
    }

    override fun cantidadDeVectoresInfectados(ubicacionId: Long): Int {
        val session = TransactionRunner.currentSession
        val hql = (""" select count(vector) as infectados
                 from Vector as vector
                 inner join vector.enfermedades as especie
                 where vector.ubicacion.id = :id """)
        val query = session.createQuery(hql, java.lang.Long::class.java)
        query.setParameter("id", ubicacionId)
        return query.singleResult.toInt()
    }

    override fun especieQueInfectoAMasVectores(ubicacionId: Long): String? {
        val session = TransactionRunner.currentSession

        val hql = """
            select especie
            from Especie as especie
               inner join especie.vectores as vector
                on vector.ubicacion.id = :id
                group by especie
                order by count(vector)
                desc
        """
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("id", ubicacionId)
        return query.setMaxResults(1).singleResult.nombre
    }

    override fun especiesEnUbicacion(ubicacionId: Long): List<Especie> {
        //TODO si no funciona esta query pegar la de arriba y cambiar el return por el que esta en esta query
        val session = TransactionRunner.currentSession
        val hql = "select e from Ubicacion u inner join u.vectores as v inner join v.enfermedades as e where u.id = :id"
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("id", ubicacionId)
        return query.resultList
    }

    override fun recuperarTodos(): List<Ubicacion> {
        val session = TransactionRunner.currentSession
        val hql = ("select i from Ubicacion i")
        val query = session.createQuery(hql, Ubicacion::class.java)
        return query.resultList
    }

    override fun crear(nombreUbicacion: String): Ubicacion {
        val location =Ubicacion(nombreUbicacion)
        super.guardar(location)
        return location
    }
}

