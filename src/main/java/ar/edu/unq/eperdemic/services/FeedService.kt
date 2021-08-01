package ar.edu.unq.eperdemic.services

import ar.edu.unq.eperdemic.modelo.*

interface FeedService {
    fun feedPatogeno(tipoDePatogeno: String) : List<Evento>
    fun feedVector(vectorId: Long) : List<Evento>
    fun feedUbicacion(ubicacionId: Long) : List<Evento>
}