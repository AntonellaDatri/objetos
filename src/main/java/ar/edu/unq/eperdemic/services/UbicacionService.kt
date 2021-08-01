package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.Ubicacion

interface UbicacionService {
    fun mover(vectorId: Long, ubicacionid: Long)
    fun expandir(ubicacionId: Long)
    fun conectados(ubicacionId:Long): List<Ubicacion>
    fun conectar(ubicacion1:Long, ubicacion2:Long, tipoCamino:String)
    fun capacidadDeExpansion(vectorId: Long, nombreDeUbicacion:String, movimientos:Int): Int
    /* Operaciones CRUD*/
    fun recuperar(id:Long): Ubicacion
    fun crear(nombreUbicacion: String): Ubicacion
    fun recuperarTodos(): List<Ubicacion>
    fun clear()


}