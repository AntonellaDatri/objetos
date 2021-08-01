package ar.edu.unq.eperdemic.persistencia.dao.hibernate

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.services.runner.TransactionRunner

open class HibernatePatogenoDAO : HibernateDAO<Patogeno>(Patogeno::class.java), PatogenoDAO {

    override fun crear(patogeno: Patogeno): Patogeno {
        super.guardar(patogeno)
        return patogeno
    }

    override fun recuperar(idDelPatogeno: Long): Patogeno {
        return super.recuperar(idDelPatogeno)
    }

    override fun recuperarPorTipo(tipo: String): Patogeno {
        val session = TransactionRunner.currentSession
        val hql = ("select p from Patogeno p where tipo= $tipo")
        val query = session.createQuery(hql, Patogeno::class.java)
        return query.singleResult
    }

    override fun recuperarATodos(): List<Patogeno> {
        val session = TransactionRunner.currentSession
        val hql = ("select p from Patogeno p")
        val query = session.createQuery(hql, Patogeno::class.java)
        return query.resultList
    }

    override fun actualizar(patogeno: Patogeno) {
        super.update(patogeno)
    }

}