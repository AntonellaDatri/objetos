    package ar.edu.unq.eperdemic.persistencia.dao.mongoDB

import ar.edu.unq.eperdemic.modelo.Evento
import ar.edu.unq.eperdemic.persistencia.dao.EventoDAO
import com.mongodb.client.model.Filters.*
import java.time.LocalDateTime

class MongoDBEventoDAO : GenericMongoDAO<Evento>(Evento::class.java), EventoDAO {
    override fun getEventoMutacionDePatogeno(tipoDePatogeno: String) : List<Evento>{
        return find(
                    and(
                        eq("tipoDePatogeno", tipoDePatogeno),
                        eq("tipoDeEvento", "Mutacion")
                    )
                )
    }

    override fun getEventoContagioPatogeno(tipoDePatogeno: String): Collection<Evento> {
        return find(
                    and(
                        eq("tipoDePatogeno", tipoDePatogeno),
                        eq("tipoDeEvento", "Contagio")
                    )
                )
    }

    override fun getEventoArriboVector(vectorID: Long): Collection<Evento> {
        return find(
            and(
                eq("vectorID", vectorID),
                eq("tipoDeEvento", "Arribo")
            )
        )
    }

    override fun getEventosContagiosVector(vectorID: Long): Collection<Evento> {
        return find(
            and(
                eq("vectorID", vectorID),
                eq("tipoDeEvento", "Vector")
            )
        )
    }

    override fun getEventoArriboUbicacion(ubicacionId: Long) : Collection<Evento> {
        return find(
            and(
                eq("ubicacionID", ubicacionId),
                eq("tipoDeEvento", "Arribo")
            )
        )
    }

    override fun getContagiosEnUbicacion(ubicacionId: Long) : Collection<Evento> {
        return find(
            and(
                eq("ubicacionID", ubicacionId),
                eq("tipoDeEvento", "Contagio")
            )
        )
    }

    override fun guardar(evento: Evento) {
        val fecha = LocalDateTime.now()
        evento.fechaDeCreacion = fecha
        save(evento)
    }

    override fun clear() {
        deleteAll()
    }

}