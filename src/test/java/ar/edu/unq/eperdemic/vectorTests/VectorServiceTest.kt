package ar.edu.unq.eperdemic.vectorTests

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.*
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.persistencia.dao.mongoDB.MongoDBEventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.services.*
import ar.edu.unq.eperdemic.services.impl.*
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VectorServiceTest {
    private val dataDAO : DataDAO = HibernateDataDAO()
    private val ubicacionDAO = HibernateUbicacionDAO()
    private val ubicacionNeo4j = UbicacionNeo4jDAO()
    private val vectorDAO = HibernateVectorDAO()
    private val especieDAO = HibernateEspecieDAO()
    private val patogenoDAO = HibernatePatogenoDAO()
    private val eventoDAO = MongoDBEventoDAO()

    private lateinit var persona : Vector
    private lateinit var especie : Especie
    private lateinit var virus : Patogeno
    private lateinit var china : Ubicacion
    private lateinit var argentina : Ubicacion

    private lateinit var especieService: EspecieService
    private lateinit var patogenoService: PatogenoService
    private lateinit var ubicacionService: UbicacionService
    private lateinit var vectorService : VectorService

    @BeforeEach
    fun prepare(){
        vectorService =  VectorServiceImpl(vectorDAO, dataDAO,especieDAO, ubicacionDAO, eventoDAO)
        ubicacionService = UbicacionServiceImpl(ubicacionNeo4j,ubicacionDAO, dataDAO, vectorDAO,eventoDAO)
        especieService = EspecieServiceImpl(especieDAO, dataDAO,ubicacionDAO,vectorDAO,eventoDAO)
        patogenoService = PatogenoServiceImpl(patogenoDAO, dataDAO,eventoDAO)

        argentina = ubicacionService.crear("Argentina")
        china =  ubicacionService.crear("China")

        persona = vectorService.crear(TipoDeVector.Persona,argentina.id!!)
        persona = vectorService.recuperar(persona.id!!)

        virus = Patogeno("virus",1,1,1)
    }

    @Test
    fun alGuardarYLuegoRecuperarSeObtieneObjetosSimilares() {
        val otroVector = vectorService.recuperar(persona.id!!)
        Assert.assertEquals(persona.id, otroVector.id)
        Assert.assertEquals(persona.tipo, otroVector.tipo)
        Assert.assertEquals(persona.ubicacion.id!!, otroVector.ubicacion.id!!)
        Assert.assertTrue(persona !== otroVector)
    }

    @Test
    fun noSePuedeGuardarUnVectorConUnaUbicacionSinPersistir() {
        val chile = Ubicacion("Chile")
        chile.id = 3
        assertThrows<NullPointerException> {persona = vectorService.crear(TipoDeVector.Animal,chile.id!!)  }
    }

    @Test
    fun pudoRecuperarTodos() {
        vectorService.crear(TipoDeVector.Animal,argentina.id!!)
        val vectores = vectorService.recuperarTodos()
        Assert.assertEquals(2,vectores.size)
        Assert.assertEquals(vectores[0].tipo, TipoDeVector.Persona)
        Assert.assertEquals(vectores[1].tipo, TipoDeVector.Animal)
    }

    @Test
    fun aunNoTieneEnfermedades() {
        Assert.assertEquals(0,persona.enfermedades.size)
        Assert.assertFalse(persona.estaInfectado())
    }

    @Test
    fun conUnaEnfermedad() {
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)
        especie = especieService.recuperarTodos()[0]
        vectorService.infectar(persona.id!!, especie.id!!)
        persona = vectorService.recuperar(persona.id!!)
        Assert.assertEquals(1,persona.enfermedades.size)
    }

    @Test
    fun estaInfectado() {
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)
        especie = especieService.recuperarTodos()[0]
        vectorService.infectar(persona.id!!, especie.id!!)
        persona = vectorService.recuperar(persona.id!!)
        Assert.assertTrue(persona.estaInfectado())
    }

    @Test
    fun tieneMasDeUnaEnfermedad() {
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)
        patogenoService.agregarEspecie(virus.id!!,"viruela",china.id!!)
        especie = especieService.recuperarTodos()[0]
        vectorService.infectar(persona.id!!, especie.id!!)
        especie = especieService.recuperarTodos()[1]
        vectorService.infectar(persona.id!!, especie.id!!)
        persona = vectorService.recuperar(persona.id!!)
        Assert.assertEquals(2,persona.enfermedades.size)
        Assert.assertTrue(persona.estaInfectado())
    }

    @AfterEach
    fun clear() {
        vectorService.clear()
        ubicacionNeo4j.clear()
    }

}