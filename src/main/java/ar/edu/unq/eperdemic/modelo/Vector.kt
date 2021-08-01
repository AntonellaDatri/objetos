package ar.edu.unq.eperdemic.modelo
import javax.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
class Vector() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long? = null

    @Column
    lateinit var tipo: TipoDeVector

    @ManyToOne()
    lateinit var ubicacion: Ubicacion

    constructor(tipo: TipoDeVector,ubicacion: Ubicacion) : this() {
        this.tipo = tipo
        this.ubicacion= ubicacion
        ubicacion.vectores.add(this)
    }

    @ManyToMany(mappedBy = "vectores", cascade = [CascadeType.ALL],  fetch = FetchType.EAGER)
    var enfermedades : MutableList<Especie> = mutableListOf()

    fun agregarEnfermedad(especie: Especie){
        enfermedades.add(especie)
        especie.agregarVector(this)
    }

        fun estaInfectado(): Boolean {
        return enfermedades.size > 0
    }

    fun nuevaUbicacion(nuevaUbicacion: Ubicacion) {
        ubicacion = nuevaUbicacion
    }
}

enum class TipoDeVector {
    Persona, Insecto, Animal
}