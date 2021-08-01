package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.EventoFeedPatogeno
import ar.edu.unq.eperdemic.persistencia.dao.*
import ar.edu.unq.eperdemic.services.EspecieService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class EspecieServiceImpl(
    private val especieDAO: EspecieDAO,
    private val dataDAO : DataDAO,
    private val ubicacionDAO: UbicacionDAO,
    private val vectorDAO: VectorDAO,
    private val eventoDAO: EventoDAO) : EspecieService {

    override fun cantidadDeInfectados(especieId: Long): Int {
        return  runTrx{especieDAO.cantidadDeInfectados(especieId)}
    }

    override fun recuperar(id: Long): Especie {
        return  runTrx{especieDAO.recuperar(id)}
    }

    override fun recuperarPorNombre(nombre: String): Especie {
        return  runTrx{especieDAO.recuperarPorNombre(nombre)}
    }

    override fun recuperarTodos(): List<Especie> {
        return  runTrx{especieDAO.recuperarTodos()}
    }

    override fun agregarPuntosADN(especieId: Long, puntosADN: Int): Especie {
        return runTrx {
            var especie = especieDAO.recuperar(especieId)
            especie.puntosADN = puntosADN
            especieDAO.actualizar(especie)
            especieDAO.recuperar(especieId)
        }
    }

    //puede que las ubicaciones no tengan las enfermedades que esten en esa ubicacion y sea solo las enfermedades que nacieron ahi
    override fun esPandemia(especieId: Long): Boolean {
        return  runTrx {
            var esPandemia = especieDAO.esPandemia(especieId, ubicacionDAO.recuperarTodos())
                if (esPandemia) {
                    var evento = EventoFeedPatogeno(
                        especieDAO.recuperar(especieId).patogeno.tipo!!,
                        "Pandemia",
                        "Contagio")
                    eventoDAO.guardar(evento)
                }
            esPandemia
        }
    }

    override fun clear() {
        runTrx {dataDAO.clear()}
    }
}