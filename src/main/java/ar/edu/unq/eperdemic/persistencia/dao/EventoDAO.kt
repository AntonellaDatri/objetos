package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.*

interface EventoDAO {
    fun getEventoMutacionDePatogeno(tipoDePatogeno: String) : List<Evento>
    fun getEventoContagioPatogeno(tipoDePatogeno: String): Collection<Evento>
    fun getEventoArriboVector(vectorID : Long) : Collection<Evento>
    fun getEventosContagiosVector(vectorID : Long) : Collection<Evento>
    fun getEventoArriboUbicacion(ubicacionId : Long) : Collection<Evento>
    fun getContagiosEnUbicacion(ubicacionId : Long) : Collection<Evento>
    fun guardar(evento: Evento)
    fun clear()
}