package ar.edu.unq.eperdemic.modelo

import java.io.Serializable
import javax.persistence.*

@Entity
class Patogeno() : Serializable {

    constructor( tipo: String, factHumano : Int, factAnimal : Int, factInsecto : Int): this(){
        this.tipo=tipo
        this.factorContagioHumano = factHumano
        this.factorContagioAnimal = factAnimal
        this.factorContagioInsecto = factInsecto
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id : Long? = null

    @Column(unique = true)
    final var tipo: String? = null

    var defensa: Int = 0
    var letalidad: Int = 0

    @OneToMany(mappedBy = "patogeno", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    var especies : MutableList<Especie> = mutableListOf()

    var cantidadDeEspecies: Int = 0
    var factorContagioHumano: Int = 1
    var factorContagioAnimal: Int = 1
    var factorContagioInsecto: Int = 1

    override fun toString(): String {
        return this.tipo!!
    }

    fun crearEspecie(nombreEspecie: String, paisDeOrigen: Ubicacion) : Especie {
        val especie = Especie(this, nombreEspecie,paisDeOrigen)
        this.especies.add(especie)
        cantidadDeEspecies ++
        return especie
    }
}