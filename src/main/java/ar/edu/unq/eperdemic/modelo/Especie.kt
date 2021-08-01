package ar.edu.unq.eperdemic.modelo

import javax.persistence.*

@Entity
class Especie() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Long? = null

    constructor(patogeno: Patogeno,nombre: String,paisDeOrigen:Ubicacion) : this() {
        this.nombre = nombre
        this.patogeno = patogeno
        this.paisDeOrigen = paisDeOrigen
    }

    @Column()
    var puntosADN: Int = 0

    @ManyToMany(mappedBy = "especies", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var mutacionesPrevias: MutableSet<Mutacion> = mutableSetOf()

    @ManyToOne
    final lateinit var patogeno: Patogeno

    @Column(unique = true)
    final var nombre: String? = null

    @ManyToOne
    final var paisDeOrigen: Ubicacion? = null

    @ManyToMany
    var vectores: MutableList<Vector> = mutableListOf()

    private fun checkPuntosADN(){
        var personasContagiadas = vectores.filter { i -> i.tipo == TipoDeVector.Persona }
        if((personasContagiadas.size % 5 )== 0) {
            puntosADN ++
        }
    }

    fun agregarVector(vector: Vector) {
        vectores.add(vector)
        if(vector.tipo == TipoDeVector.Persona){
            checkPuntosADN()
        }
    }

    fun mutar(mutacion: Mutacion): Boolean {
        val puedeMutar = puedeMutar(mutacion)
        if(puedeMutar) {
            mutacionesPrevias.add(mutacion)
            mutacion.especies.add(this)
            puntosADN -= mutacion.getpuntosDeADNRequeridos()
        }
        return puedeMutar
    }

    fun puedeMutar(mutacion: Mutacion): Boolean{
        return puntosADN >= mutacion.getpuntosDeADNRequeridos() &&
                mutacionesPrevias.containsAll(mutacion.getRequisitos())
    }
}