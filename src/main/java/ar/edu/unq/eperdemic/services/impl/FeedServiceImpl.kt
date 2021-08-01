package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Evento
import ar.edu.unq.eperdemic.persistencia.dao.EventoDAO
import ar.edu.unq.eperdemic.services.FeedService

class FeedServiceImpl (private val eventoDAO: EventoDAO) : FeedService {
    override fun feedPatogeno(tipoDePatogeno: String): List<Evento> {
        val eventos = mutableListOf<Evento>()
        eventos.addAll(eventoDAO.getEventoMutacionDePatogeno(tipoDePatogeno))
        eventos.addAll(eventoDAO.getEventoContagioPatogeno(tipoDePatogeno))
        eventos.sortWith(compareBy{ it.fechaDeCreacion()})
        return eventos.reversed()
    }

    override fun feedVector(vectorId: Long): List<Evento> {
        val eventos = mutableListOf<Evento>()
        eventos.addAll(eventoDAO.getEventoArriboVector(vectorId))
        eventos.addAll(eventoDAO.getEventosContagiosVector(vectorId))
        eventos.sortWith(compareBy{ it.fechaDeCreacion()})
        return eventos.reversed()
    }

    override fun feedUbicacion(ubicacionId: Long): List<Evento> {
        val eventos = mutableListOf<Evento>()
        eventos.addAll(eventoDAO.getEventoArriboUbicacion(ubicacionId))
        eventos.addAll(eventoDAO.getContagiosEnUbicacion(ubicacionId))
        eventos.sortWith(compareBy{ it.fechaDeCreacion()})
        return eventos.reversed()
    }
}