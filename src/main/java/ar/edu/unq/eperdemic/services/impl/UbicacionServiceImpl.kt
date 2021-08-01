package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.*
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx

class UbicacionServiceImpl(
    private val neo4jUbicacionDAO: NeoUbicacionDAO,
    private val ubicacionDAO: UbicacionDAO,
    private val dataDAO: DataDAO,
    private val vectorDAO: VectorDAO,
    private val eventoDAO: EventoDAO): UbicacionService {

    override fun conectados(ubicacionId: Long): List<Ubicacion> {
        return runTrx{
            if (ubicacionDAO.recuperar(ubicacionId) != null) {
                val ubicacionesNeo = neo4jUbicacionDAO.conectados(ubicacionId)
                val ubicaciones = mutableListOf<Ubicacion>()
                ubicacionesNeo.forEach{ubicacion -> ubicaciones.add(ubicacionDAO.recuperar(ubicacion.id))}
                ubicaciones.toList()
            } else {
                listOf()
            }
        }
    }

    override fun capacidadDeExpansion(vectorId: Long, nombreDeUbicacion: String, movimientos: Int): Int {
        return runTrx {
            val tipoVector = vectorDAO.recuperar(vectorId).tipo
            val caminos = caminosPorLosQueSePuedeMover(tipoVector)
            neo4jUbicacionDAO.capacidadDeExpansion(caminos, nombreDeUbicacion, movimientos)
        }
    }

    private fun caminosPorLosQueSePuedeMover(tipoVector: TipoDeVector):MutableList<String>{
        val lista = mutableListOf<String>()
        when (tipoVector) {
            TipoDeVector.Persona -> {
                lista.add("Terrestre")
                lista.add("Maritimo")

            }
            TipoDeVector.Animal -> {
                lista.add("Terrestre")
                lista.add("Maritimo")
                lista.add("Aereo")
            }
            TipoDeVector.Insecto -> {
                lista.add("Terrestre")
                lista.add("Aereo")
            }
        }
        return lista
    }

    override fun conectar(ubicacion1: Long, ubicacion2: Long, tipoCamino: String) {
        runTrx { neo4jUbicacionDAO.conectar(ubicacion1, ubicacion2, tipoCamino) }
    }

    override fun mover(vectorId: Long, ubicacionid: Long) {
        runTrx {
            val vectorContagiado = vectorDAO.recuperar(vectorId)
            val numeroRandom = RandomNumber.rango(0,10)
            val ubicacion = ubicacionDAO.recuperar(ubicacionid)
            val caminos = caminosPorLosQueSePuedeMover(vectorContagiado.tipo)
            neo4jUbicacionDAO.mover(caminos, ubicacion, vectorContagiado)
            if(vectorContagiado.estaInfectado()) {
                contagiarAVecinos(vectorContagiado, ubicacion, numeroRandom)
            }
            ubicacion.agregarVector(vectorContagiado)
            ubicacionDAO.actualizar(ubicacion)
            val eventoVector = EventoFeedVector(vectorContagiado.id!!, vectorContagiado.tipo, "Viajes", "Arribo", ubicacion.nombre!!)
            val eventoUbicacion = EventoFeedUbicacion(ubicacion.id!!, vectorContagiado.id!!, vectorContagiado.tipo,"ViajesUbicacion", "Arribo")
            eventoDAO.guardar(eventoVector)
            eventoDAO.guardar(eventoUbicacion)
        }
    }

    override fun expandir(ubicacionId: Long) {
        runTrx {
            val ubicacion = ubicacionDAO.recuperar(ubicacionId)
            val vectoresInfectadosDeUbicacion = ubicacion.vectores.filter { vector -> vector.estaInfectado() }
            if (vectoresInfectadosDeUbicacion.isNotEmpty()){
                val index = RandomNumber.rango(0, vectoresInfectadosDeUbicacion.size-1)
                val vectorRamdom = vectoresInfectadosDeUbicacion[index]
                contagiarAVecinos(vectorRamdom, ubicacion, RandomNumber.rango(1,10))
            }
        }
    }

    override fun crear(nombreUbicacion: String): Ubicacion {
       return runTrx {
            val ubicacion = ubicacionDAO.crear(nombreUbicacion)
            neo4jUbicacionDAO.crear(ubicacion.id, nombreUbicacion)
            ubicacion
       }
    }

    override fun recuperarTodos(): List<Ubicacion> {
        return runTrx{ubicacionDAO.recuperarTodos()}
    }

    override fun recuperar(id: Long): Ubicacion {
        return runTrx{ ubicacionDAO.recuperar(id)}
    }

    override fun clear() {
        return runTrx { dataDAO.clear() }
    }

    private fun contagiarAVecinos(vector : Vector, ubicacion: Ubicacion, number: Int){
        val primeraAparicionDeEspecieEnUbicacion = ubicacionDAO.especiesEnUbicacion(ubicacion.id!!)
        val especies = vector.enfermedades
        for (especie in especies) {
            for (vectorAEnfermar in ubicacion.vectores) {
                if (vectorAEnfermar.id !== vector.id) {
                    ContagiarVectorStrategy().contagiarVector(vectorAEnfermar, vector, especie, number,primeraAparicionDeEspecieEnUbicacion,eventoDAO)
                }
            }
        }
    }
}