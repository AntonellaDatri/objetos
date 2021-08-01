package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.UbicacionResumida
import ar.edu.unq.eperdemic.modelo.Vector

interface NeoUbicacionDAO {
    fun crear(ubicacionId: Long?, nombreUbicacion: String)
    fun mover(caminos: MutableList<String>, ubicacion: Ubicacion, vector: Vector) :Int
    fun conectados(ubicacionId:Long): List<UbicacionResumida>
    fun capacidadDeExpansion(caminos: MutableList<String>, nombreDeUbicacion:String, movimientos:Int): Int
    fun conectar(ubicacion1:Long, ubicacion2:Long, tipoCamino:String)
    fun clear()
}

class UbicacionNoAlcanzable : Exception() {}
