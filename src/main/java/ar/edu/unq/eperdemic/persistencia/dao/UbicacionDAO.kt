package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion

interface UbicacionDAO {
    fun crear(nombreUbicacion: String): Ubicacion
    fun recuperarTodos(): List<Ubicacion>
    fun recuperar(id: Long): Ubicacion
    fun actualizar(ubicacion: Ubicacion)
    fun cantidadDeVectoresPresentes(ubicacionId : Long) : Int
    fun cantidadDeVectoresInfectados(ubicacionId : Long) : Int
    fun especieQueInfectoAMasVectores(ubicacionId : Long) : String?
    fun especiesEnUbicacion(ubicacionId : Long): List<Especie>
}