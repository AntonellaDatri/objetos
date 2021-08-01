package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.EventoFeedPatogeno
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.EventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.PatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class PatogenoServiceImpl(
    var patogenoDAO: PatogenoDAO,
    private  val dataDAO: DataDAO,
    private val eventoDAO: EventoDAO) : PatogenoService {


    override fun crear(patogeno: Patogeno): Patogeno {
        return runTrx{
            var evento = EventoFeedPatogeno(patogeno.tipo!!, "Creacion", "Mutacion")
            eventoDAO.guardar(evento)
            patogenoDAO.crear(patogeno)
        }
    }

    override fun recuperar(id: Long): Patogeno {
        return runTrx { patogenoDAO.recuperar(id)  }
    }

    override fun recuperarTodos(): List<Patogeno> {
        return runTrx { patogenoDAO.recuperarATodos() }
    }

    override fun agregarEspecie(id: Long, nombre: String, ubicacionId: Long): Especie {
        return runTrx {
            val patogeno = patogenoDAO.recuperar(id)
            val paisDeOrigen = HibernateUbicacionDAO().recuperar(ubicacionId)
            val especie = patogeno.crearEspecie(nombre, paisDeOrigen)
            patogenoDAO.actualizar(patogeno)
            especie
        }
    }
  
   override fun especiesDePatogeno(patogenoId: Long): List<Especie> {
       return recuperar(patogenoId).especies
    }

    override fun clear(){
        return runTrx{ dataDAO.clear() }
    }
}