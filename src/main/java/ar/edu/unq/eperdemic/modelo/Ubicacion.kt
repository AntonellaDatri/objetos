package ar.edu.unq.eperdemic.modelo

import javax.persistence.*

@Entity
class Ubicacion() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long? = null
    @Column(unique = true)
    final var nombre: String? = null

    constructor(nuevoNombre:String):this(){
        this.nombre=nuevoNombre
    }

    @OneToMany(mappedBy = "ubicacion", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var vectores : MutableList<Vector> = mutableListOf()

    @OneToMany(mappedBy = "paisDeOrigen", cascade = [CascadeType.ALL])
    var especies : MutableList<Especie> = mutableListOf()

    fun agregarVector(vector : Vector){
       if  ( !(vectores.map { v -> v.id  }.contains(vector.id))) {
           vectores.add(vector)
       }
        vector.nuevaUbicacion(this)
    }

}