package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Ubicacion

interface EspecieDAO {
    fun cantidadDeInfectados(especieId: Long): Int
    fun recuperar(id: Long): Especie
    fun recuperarPorNombre(nombre:String):Especie
    fun recuperarTodos(): List<Especie>
    fun actualizar(especie: Especie)
    fun lider():Especie
    fun lideres():List<Especie>
    fun esPandemia(especieId: Long, recuperarTodos: List<Ubicacion>) : Boolean
}