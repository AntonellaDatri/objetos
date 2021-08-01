package ar.edu.unq.eperdemic.persistencia.dao.hibernate;

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Mutacion
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.MutacionDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

class HibernateMutacionDAO: HibernateDAO<Mutacion>(Mutacion::class.java),MutacionDAO {

    override fun crear(mutacion: Mutacion): Mutacion{
        super.guardar(mutacion)
        return mutacion
    }

    override fun recuperar(id: Long): Mutacion{
        return super.recuperar(id)
    }

    override fun recuperarTodos(): List<Mutacion>{
        val session = TransactionRunner.currentSession
        val hql = ("select i from Mutacion i")
        val query = session.createQuery(hql, Mutacion::class.java)
        return query.resultList
    }

    override fun actualizar(mutacion: Mutacion) {
        super.update(mutacion)
    }
}
