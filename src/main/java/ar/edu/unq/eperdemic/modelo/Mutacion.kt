package ar.edu.unq.eperdemic.modelo

import javax.persistence.*

@Entity
class Mutacion() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long? = null

    @Column(unique = true)
    final var nombre: String? = null
    var puntosDeADNRequeridos : Int = 0

    @ManyToMany(cascade= [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name="mutacionesAdesbloquear")
    private var requerimientos : MutableList<Mutacion> = mutableListOf()

    @ManyToMany(mappedBy="requerimientos", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    private var mutacionesAdesbloquear: MutableList<Mutacion> = mutableListOf()

    @ManyToMany
    var especies: MutableList<Especie> = mutableListOf()

    constructor(nuevoNombre:String, requerimientos : MutableList<Mutacion>, puntosDeADNRequeridos: Int):this(){
        this.nombre=nuevoNombre
        this.requerimientos = requerimientos
        this.puntosDeADNRequeridos = puntosDeADNRequeridos
    }

    fun agregarRequisito(requisito : Mutacion){
        requerimientos.add(requisito)
    }

    fun agregarADesbloquear(aDesbloquear: Mutacion){
        mutacionesAdesbloquear.add(aDesbloquear)
    }

    fun getRequisitos() : MutableList<Mutacion>{
        return requerimientos
    }

    fun getpuntosDeADNRequeridos() : Int{
        return puntosDeADNRequeridos
    }

    fun getMutacionesADesbloquear(): MutableList<Mutacion> {
        return mutacionesAdesbloquear
    }
}