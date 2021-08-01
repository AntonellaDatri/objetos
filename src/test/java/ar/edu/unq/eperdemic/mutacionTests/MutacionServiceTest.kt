package ar.edu.unq.eperdemic.mutacionTests

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.persistencia.dao.mongoDB.MongoDBEventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.services.EspecieService
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.impl.*
import org.hibernate.exception.ConstraintViolationException
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MutacionServiceTest {
    private val dataDAO : DataDAO = HibernateDataDAO()
    private val especieDAO = HibernateEspecieDAO()
    private val mutacionDAO = HibernateMutacionDAO()
    private val ubicacionDAO = HibernateUbicacionDAO()
    private val ubicacionNeo4j = UbicacionNeo4jDAO()
    private val vectorDAO = HibernateVectorDAO()
    private val patogenoDAO = HibernatePatogenoDAO()
    private val eventoDAO = MongoDBEventoDAO()

    private lateinit var mutacionService : MutacionServiceImpl
    private lateinit var fiebre : Mutacion
    private lateinit var especie: Especie
    private lateinit var china: Ubicacion
    private lateinit var virus: Patogeno
    private lateinit var persona: Vector
    private val requerimientos : MutableList<Mutacion> = mutableListOf()

    private lateinit var especieService: EspecieService
    private lateinit var patogenoService: PatogenoService
    private lateinit var ubicacionService: UbicacionService
    private lateinit var vectorService : VectorService

    @BeforeEach
    fun prepare() {
        mutacionService = MutacionServiceImpl(mutacionDAO, dataDAO, especieDAO, eventoDAO)
        mutacionService.clear()
        vectorService =  VectorServiceImpl(vectorDAO, dataDAO,especieDAO, ubicacionDAO, eventoDAO)
        ubicacionService = UbicacionServiceImpl(ubicacionNeo4j,ubicacionDAO, dataDAO, vectorDAO, eventoDAO)
        especieService = EspecieServiceImpl(especieDAO, dataDAO,ubicacionDAO,vectorDAO, eventoDAO)
        patogenoService = PatogenoServiceImpl(patogenoDAO, dataDAO, eventoDAO)

        fiebre = mutacionService.crear(Mutacion("Fiebre",requerimientos, 5))
        fiebre = mutacionService.recuperarTodos()[0]

        china =  ubicacionService.crear("China")
        virus = Patogeno("virus",1,1,1)
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)
        especie = especieService.recuperarTodos()[0]

        val argentina = ubicacionService.crear("Argentina")
        persona = vectorService.crear(TipoDeVector.Persona, argentina.id!!)
        persona = vectorService.recuperar(persona.id!!)
    }

    @Test
    fun alGuardarYLuegoRecuperarSeObtieneObjetosSimilares() {
        val fiebreRecuperada = mutacionService.recuperar(fiebre.id!!)
        Assert.assertEquals(fiebre.id, fiebreRecuperada.id)
        Assert.assertEquals(fiebre.nombre, fiebreRecuperada.nombre)
        Assert.assertEquals(fiebre.getRequisitos().size, fiebreRecuperada.getRequisitos().size)
        Assert.assertEquals(fiebre.puntosDeADNRequeridos , fiebreRecuperada.puntosDeADNRequeridos)
        Assert.assertTrue(fiebre !== fiebreRecuperada)
    }

    @Test
    fun puedoRecuperarTodos() {
        val mutacionARecuperar = Mutacion("Tos", requerimientos, 5)
        mutacionService.crear(mutacionARecuperar)
        val mutaciones = mutacionService.recuperarTodos()
        Assert.assertEquals(2,mutaciones.size)
        Assert.assertEquals(mutaciones[0].nombre, "Fiebre")
        Assert.assertEquals(mutaciones[1].nombre, "Tos")
    }

    @Test
    fun aunNoTieneRequerimientos() {
        Assert.assertEquals(0,fiebre.getRequisitos().size)
    }

    @Test
    fun conUnRequerimiento() {
        val requisito = Mutacion("Tos", requerimientos, 5)
        mutacionService.crear(requisito)
        mutacionService.agregarRequisito(fiebre.id!!, requisito)
        fiebre = mutacionService.recuperar(fiebre.id!!)
        Assert.assertEquals(1,fiebre.getRequisitos().size)
        Assert.assertEquals(requisito.nombre, fiebre.getRequisitos()[0].nombre)
    }

    @Test
    fun mutaEspecieConADNSuficienteSinRequisitosPedidos() {
        for (num in 1..30){
            vectorService.infectar(persona.id!!, especie.id!!)
        }
        especie = especieService.recuperar(especie.id!!)
        Assert.assertEquals(6, especie.puntosADN)
        especie = mutacionService.mutar(especie.id!!, fiebre.id!!)
        Assert.assertEquals(1, especie.mutacionesPrevias.size)
        Assert.assertEquals(fiebre.nombre, especie.mutacionesPrevias.first().nombre)
        Assert.assertEquals(1, especie.puntosADN)
    }

    @Test
    fun noSePuedeMutarEspeciePorFaltaDeADN() {
        //se infecta solo una persona, equivale a 0 puntos de ADN
        vectorService.infectar(persona.id!!, especie.id!!)
        especie = especieService.recuperar(especie.id!!)

        Assert.assertEquals(0, especie.puntosADN)

        especie = mutacionService.mutar(especie.id!!, fiebre.id!!)
        Assert.assertEquals(0, especie.mutacionesPrevias.size)
        Assert.assertEquals(0, especie.puntosADN)
    }

    @Test
    fun noSePuedeMutarEspeciePorqueFaltanRequisitos() {
        for (num in 1..30){
            vectorService.infectar(persona.id!!, especie.id!!)
        }
        especie = especieService.recuperar(especie.id!!)

        Assert.assertEquals(6, especie.puntosADN)

        val requisito = Mutacion("Tos", requerimientos, 5)
        mutacionService.crear(requisito)
        mutacionService.agregarRequisito(fiebre.id!!, requisito)
        fiebre = mutacionService.recuperar(fiebre.id!!)
        especie = mutacionService.mutar(especie.id!!, fiebre.id!!)
        Assert.assertEquals(0, especie.mutacionesPrevias.size)
        Assert.assertEquals(6, especie.puntosADN)
    }

    @Test
    fun puedeMutarPorqueTieneRequisitosYADN() {
        for (num in 1..35){
            vectorService.infectar(persona.id!!, especie.id!!)
        }
        especie = especieService.recuperar(especie.id!!)

        Assert.assertEquals(7, especie.puntosADN)

        val tos = Mutacion("Tos", requerimientos, 2)
        mutacionService.crear(tos)
        mutacionService.agregarRequisito(fiebre.id!!, tos)
        fiebre = mutacionService.recuperar(fiebre.id!!)
        especie = mutacionService.mutar(especie.id!!, tos.id!!)
        Assert.assertEquals(1, especie.mutacionesPrevias.size)
        Assert.assertTrue(especie.mutacionesPrevias.map { m-> m.nombre }.contains(tos.nombre))
        Assert.assertEquals(5, especie.puntosADN)
        especie = mutacionService.mutar(especie.id!!, fiebre.id!!)
        Assert.assertEquals(2, especie.mutacionesPrevias.size)
        Assert.assertTrue(especie.mutacionesPrevias.map { m-> m.nombre }.contains(tos.nombre))
        Assert.assertEquals(0, especie.puntosADN)
    }

    @Test
    fun noSePuedeCrearDosMutacionesConElMismoNombre() {
        val fiebre1 = Mutacion("Fiebre", requerimientos,2)
        assertThrows(ConstraintViolationException::class.java) { mutacionService.crear(fiebre1) }
    }

    @AfterEach
    fun clear() {
        mutacionService.clear()
    }
}