package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.EventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.MutacionDAO
import ar.edu.unq.eperdemic.services.MutacionService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class MutacionServiceImpl(
    private val mutacionDAO: MutacionDAO,
    private val dataDAO : DataDAO,
    private val especieDAO: EspecieDAO,
    private val eventoDAO: EventoDAO) : MutacionService {

    override fun mutar(especieId: Long, mutacionId: Long): Especie {
        return  runTrx {
             val especie = especieDAO.recuperar(especieId)
             val mutacion = mutacionDAO.recuperar(mutacionId)
             val mutó = especie.mutar(mutacion)
            //TODO falta comprar si mutó <-- creo que ya esta, falta probar
             if(mutó){
                 var evento = EventoFeedPatogeno(especie.patogeno.tipo!!,"Mutacion","Mutacion")
                 eventoDAO.guardar(evento)
             }
             especieDAO.actualizar(especie)
             especie
         }
    }

    override fun crear(mutacion : Mutacion): Mutacion {
        return runTrx { mutacionDAO.crear(mutacion) }
    }

    override fun recuperar(mutacionId: Long): Mutacion {
        return runTrx { mutacionDAO.recuperar(mutacionId) }
    }

    override fun recuperarTodos(): List<Mutacion> {
        return runTrx { mutacionDAO.recuperarTodos() }
    }
    override fun clear() {
        runTrx { dataDAO.clear() }
    }

    fun agregarRequisito(mutacionId : Long, requisito : Mutacion) {
        runTrx {
            val mutacion = mutacionDAO.recuperar(mutacionId)
            mutacion.agregarRequisito(requisito)
            mutacionDAO.actualizar(mutacion)
        }
    }
}