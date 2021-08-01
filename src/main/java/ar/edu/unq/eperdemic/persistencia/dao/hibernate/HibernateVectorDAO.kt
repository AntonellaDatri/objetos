package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.VectorDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

open class HibernateVectorDAO : HibernateDAO<Vector>(Vector::class.java), VectorDAO {

    override fun crear(vector: Vector): Vector {
        super.guardar(vector)
        return vector
    }

    override fun recuperar(vectorId: Long): Vector {
        return super.recuperar(vectorId)
    }

    override fun actualizar(vector: Vector) {
        super.update(vector)
    }

    override fun recuperarTodos(): List<Vector> {
        val session = TransactionRunner.currentSession
        val hql = ("select v from Vector v")
        val query = session.createQuery(hql, Vector::class.java)
        return query.resultList
    }
}