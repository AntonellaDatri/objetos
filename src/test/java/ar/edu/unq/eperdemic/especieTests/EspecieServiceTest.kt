package ar.edu.unq.eperdemic.especieTests

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.TipoDeVector
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateDataDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateEspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateVectorDAO
import ar.edu.unq.eperdemic.persistencia.dao.mongoDB.MongoDBEventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.services.EspecieService
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.impl.EspecieServiceImpl
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.persistence.PersistenceException

class EspecieServiceTest {
    private lateinit var especieService: EspecieService
    private lateinit var patogenoService: PatogenoService
    private lateinit var ubicacionService: UbicacionService
    private lateinit var vectorService: VectorService
    private val dataDAO  = HibernateDataDAO()
    private val ubicacionDAO = HibernateUbicacionDAO()
    private val neo4jUbicacionDAO = UbicacionNeo4jDAO()
    private val vectorDAO = HibernateVectorDAO()
    private val especieDAO = HibernateEspecieDAO()
    private val patogenoDAO = HibernatePatogenoDAO()
    private val eventoDAO = MongoDBEventoDAO()
    private lateinit var china: Ubicacion
    private var virus = Patogeno("virus",10,20,30)

    @BeforeEach
    fun prepare(){
        this.especieService = EspecieServiceImpl(especieDAO, dataDAO,ubicacionDAO, vectorDAO, eventoDAO)
        this.patogenoService = PatogenoServiceImpl(patogenoDAO,dataDAO, eventoDAO)
        this.ubicacionService = UbicacionServiceImpl(neo4jUbicacionDAO,ubicacionDAO, dataDAO, vectorDAO, eventoDAO)
        this.vectorService = VectorServiceImpl(vectorDAO,dataDAO, especieDAO, ubicacionDAO, eventoDAO)
        china =  ubicacionService.crear("China")
        virus = patogenoService.crear(virus)
    }
    @Test
    fun alGuardarYLuegoRecuperarSeObtieneEspeciesSimilares() {

        patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)
        val todosVirus = especieService.recuperarTodos()
        val coronavirus = especieService.recuperarPorNombre("coronavirus")

        Assert.assertEquals("coronavirus",todosVirus.first().nombre)
        Assert.assertEquals("coronavirus",coronavirus.nombre)
        Assert.assertEquals("virus",coronavirus.patogeno.tipo)

    }
  /*  @Test
    fun alGuardarDosEspeciesLuegoSeLasRecuperan() {

        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)
        patogenoService.agregarEspecie(virus.id!!,"viruela",china.id!!)

        val viruela = especieService.recuperarPorNombre("viruela")
        val esterichia = especieService.recuperarPorNombre("esterichiaColi")
        val todasEspecies =  especieService.recuperarTodos()

        Assert.assertEquals(todasEspecies.size,2)
        Assert.assertEquals("esterichiaColi",esterichia.nombre)
        Assert.assertEquals("viruela",viruela.nombre)
    }
*/
    @Test
    fun laEsterichiaColiTieneDosInfectados() {

        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)

        val esterichiaColi = especieService.recuperarPorNombre("esterichiaColi")

        val persona = vectorService.crear(TipoDeVector.Persona,china.id!!)
        vectorService.infectar(persona.id!!,esterichiaColi.id!!)

        val cantidadInfectados =  especieService.cantidadDeInfectados(esterichiaColi.id!!)

        Assert.assertEquals(cantidadInfectados,1)

        val animal = vectorService.crear(TipoDeVector.Animal,china.id!!)
        vectorService.infectar(animal.id!!,esterichiaColi.id!!)

        Assert.assertEquals(especieService.cantidadDeInfectados(esterichiaColi.id!!),2)
    }

    @Test
    fun noSePermitenAgregarDosEspeciesConIgualNombre() {
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)
        assertThrows(PersistenceException::class.java){  patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)}
    }

    @Test
    fun unaEspecieQueSoloEstaEn2UbicacionesDe5RegistradaNoEsPandemia() {
        val persona = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val esterichiaColi = patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)

        val argentina = ubicacionService.crear("Argentina")
        ubicacionService.crear("España")
        ubicacionService.crear("Canada")
        ubicacionService.crear("India")

        vectorService.infectar(persona.id!!,esterichiaColi.id!!)
        neo4jUbicacionDAO.conectar(china.id!!, argentina.id!!, "Maritimo")
        ubicacionService.mover(persona.id!!, argentina.id!!)
        Assert.assertFalse(especieService.esPandemia(esterichiaColi.id!!))
    }

    @Test
    fun unaEspecieQueEstaEn3UbicacionesDe5RegistradaEsPandemia() {
        val persona = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona2 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona3 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val esterichiaColi = patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)

        val argentina = ubicacionService.crear("Argentina")
        val espania = ubicacionService.crear("España")
        ubicacionService.crear("Canada")
        ubicacionService.crear("India")

        vectorService.infectar(persona.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona2.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona3.id!!,esterichiaColi.id!!)

        neo4jUbicacionDAO.conectar(china.id!!, argentina.id!!, "Maritimo")
        neo4jUbicacionDAO.conectar(china.id!!, espania.id!!, "Maritimo")
        ubicacionService.mover(persona.id!!, argentina.id!!)
        ubicacionService.mover(persona2.id!!, espania.id!!)

        Assert.assertTrue(especieService.esPandemia(esterichiaColi.id!!))
    }
    @Test
    fun unaEspecieQueSoloEstaEn2UbicacionesDe5RegistradaPeroCon3VectoresNoEsPandemia() {
        val persona = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona2 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona3 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val esterichiaColi = patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)

        val argentina = ubicacionService.crear("Argentina")
        ubicacionService.crear("España")
        ubicacionService.crear("Canada")
        ubicacionService.crear("India")

        vectorService.infectar(persona.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona2.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona3.id!!,esterichiaColi.id!!)
        neo4jUbicacionDAO.conectar(china.id!!, argentina.id!!, "Maritimo")
        ubicacionService.mover(persona.id!!, argentina.id!!)
        Assert.assertFalse(especieService.esPandemia(esterichiaColi.id!!))
    }

    @AfterEach
    fun clear() {
        especieService.clear()
        neo4jUbicacionDAO.clear()
    }
}