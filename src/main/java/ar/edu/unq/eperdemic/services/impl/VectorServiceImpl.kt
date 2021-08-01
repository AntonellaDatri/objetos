package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.*
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class VectorServiceImpl(private val vectorDAO: VectorDAO,private val dataDAO : DataDAO,private val especieDAO:EspecieDAO, private val ubicacionDAO : UbicacionDAO, private val eventoDAO: EventoDAO): VectorService {

    override fun infectar(vectorId: Long, especieId: Long) {
        runTrx {
            val especie = especieDAO.recuperar(especieId)
            val vector = vectorDAO.recuperar(vectorId)
            vector.agregarEnfermedad(especie)
            val eventoEnfermar = EventoFeedVector(vectorId,vector.tipo, "Enfermedades", "Vector", vector.ubicacion.nombre!!)
            eventoDAO.guardar(eventoEnfermar)
            vectorDAO.actualizar(vector)
        }
    }

    override fun enfermedades(vectorId: Long): List<Especie> {
        return runTrx{
            val vector = vectorDAO.recuperar(vectorId)
            vector.enfermedades.toList()
        }
    }

    override fun crear(tipo: TipoDeVector, ubicacionId: Long): Vector {
        return runTrx{
            val ubicacion = ubicacionDAO.recuperar(ubicacionId)
            var vector = Vector(tipo, ubicacion)
            vector = vectorDAO.crear(vector)
            val evento = EventoFeedVector(vector.id!!, vector.tipo, "Viajes", "Arribo", ubicacion.nombre!!)
            eventoDAO.guardar(evento)
            vector
        }
    }

    override fun recuperar(vectorId: Long): Vector {
        return runTrx{vectorDAO.recuperar(vectorId)}
    }

    override fun recuperarTodos(): List<Vector> {
        return runTrx{vectorDAO.recuperarTodos()}
    }

    override fun clear() {
        runTrx { dataDAO.clear() }
    }
}