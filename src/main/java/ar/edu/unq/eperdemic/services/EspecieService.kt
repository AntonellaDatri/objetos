package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.Especie

interface EspecieService {

    fun cantidadDeInfectados (especieId: Long) : Int
    fun recuperar(id: Long): Especie
    fun recuperarPorNombre(nombre:String):Especie
    fun recuperarTodos(): List<Especie>

    fun agregarPuntosADN(especieId: Long, puntosADN : Int): Especie
    fun esPandemia (especieId: Long) : Boolean
    fun clear()
}