package ar.edu.unq.eperdemic.modelo

import org.bson.codecs.pojo.annotations.BsonDiscriminator
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "Evento")
@BsonDiscriminator
abstract class Evento {
    @BsonProperty("id")
    @BsonId
    var id: ObjectId? = null

    @Column(name = "descripcion", nullable = true)
    var descripcion: String? = null

    @Column(name = "tipoDeEvento", nullable = true)
    var tipoDeEvento: String? = null
    lateinit var fechaDeCreacion : LocalDateTime
    fun fechaDeCreacion() : LocalDateTime{
        return fechaDeCreacion
    }
    fun tipoDeEvento(): String {
        return tipoDeEvento!!
    }

     fun descripcion(): String {
        return descripcion!!
    }

   abstract fun getTipo() : String?

    protected constructor() {
    }
}


class EventoFeedPatogeno : Evento {
    var tipoDePatogeno: String? = null

     override fun getTipo(): String {
        return tipoDePatogeno!!
    }

   // protected constructor() {
   // }
    constructor(tipoDePatogeno: String, descripcion: String, tipoDeEvento: String) {
        this.tipoDePatogeno = tipoDePatogeno
        this.descripcion = descripcion
        this.tipoDeEvento = tipoDeEvento
    }

}

class EventoFeedVector : Evento {
    var vectorID: Long? = null
    var tipoDeVector: TipoDeVector? = null
    var ubicacion : String? = null

     override fun getTipo(): String {
        return tipoDeVector!!.name
    }

 //   protected constructor() {
 //   }
    constructor(vectorID: Long, tipoDeVector:TipoDeVector, descripcion: String, tipoDeEvento: String, ubicacion: String) {
        this.vectorID = vectorID
        this.tipoDeVector = tipoDeVector
        this.descripcion = descripcion
        this.tipoDeEvento = tipoDeEvento
        this.ubicacion = ubicacion
    }
}


class EventoFeedUbicacion : Evento {
    var ubicacionID: Long? = null
    var vectorIDEnUbicacion: Long? = null
    var tipoDeVector: TipoDeVector? = null


   // protected constructor() {
  //  }
    constructor(ubicacionID: Long, vectorID:Long, tipoDeVector : TipoDeVector,descripcion: String, tipoDeEvento: String) {
        this.ubicacionID = ubicacionID
        this.vectorIDEnUbicacion = vectorID
        this.tipoDeVector = tipoDeVector
        this.descripcion = descripcion
        this.tipoDeEvento = tipoDeEvento
    }

    override fun getTipo(): String {
        return tipoDeVector!!.name

    }
}