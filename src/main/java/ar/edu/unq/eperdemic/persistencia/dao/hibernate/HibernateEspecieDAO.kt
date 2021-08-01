package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.TipoDeVector
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

open class HibernateEspecieDAO: HibernateDAO<Especie>(Especie::class.java),EspecieDAO {

    override fun cantidadDeInfectados(especieId: Long): Int {
        val especie = recuperar(especieId)
        return especie.vectores.size
    }

    override fun recuperar(id: Long): Especie {
        return super.recuperar(id)
    }

    override fun recuperarPorNombre(nombre: String): Especie {
        val session = TransactionRunner.currentSession
        val hql = "select e from Especie e where e.nombre = :nombre "
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("nombre", nombre)
        query.maxResults = 1
       return query.singleResult
    }


    override fun recuperarTodos(): List<Especie> {
        val session = TransactionRunner.currentSession
        val hql = "select e from Especie e "
        val query = session.createQuery(hql, Especie::class.java)
        return query.resultList
    }

    override fun lider() : Especie {
        val session = TransactionRunner.currentSession
        val hql = "select e from Especie e join e.vectores v WHERE v.tipo = :tipoPersona ORDER BY e.vectores.size DESC "
        val query = session.createQuery(hql, Especie::class.java)
        query.setParameter("tipoPersona", TipoDeVector.Persona)
        return query.list().first()
    }

    override fun lideres() : List<Especie> {
    val session = TransactionRunner.currentSession
    val hql = """
            select especie
            from Especie as especie
            inner join especie.vectores as vector
            where vector.tipo = :tipoPersona or vector.tipo = :tipoAnimal
            group by especie
            order by count(vector) desc
        """
    val query = session.createQuery(hql, Especie::class.java)
    query.setParameter("tipoPersona", TipoDeVector.Persona)
    query.setParameter("tipoAnimal", TipoDeVector.Animal)
    return query.resultList.take(10)
    }

    override fun esPandemia(especieId: Long, ubicaciones: List<Ubicacion>): Boolean {
        val session = TransactionRunner.currentSession

        val query = session.createQuery("""
        select count(vector.ubicacion)
        from Especie as especie
        inner join especie.vectores as vector
        on especie.id = :especieId
        group by vector.ubicacion
    """)
        query.setParameter("especieId", especieId)
        val count = query.resultList.size
        return  count >= ((ubicaciones.size / 2) + (ubicaciones.size % 2))
    }

    override fun actualizar(especie: Especie) {
        super.update(especie)
    }
}