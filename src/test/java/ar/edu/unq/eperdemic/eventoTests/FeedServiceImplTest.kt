package ar.edu.unq.eperdemic.eventoTests

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.persistencia.dao.mongoDB.MongoDBEventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.services.*
import ar.edu.unq.eperdemic.services.impl.*
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FeedServiceImplTest {
    private lateinit var especieService: EspecieService
    private lateinit var patogenoService: PatogenoService
    private lateinit var ubicacionService: UbicacionService
    private lateinit var vectorService: VectorService
    private lateinit var estadisticasService: EstadisticasService
    private lateinit var eventoService: FeedService
    private  lateinit var mutacionService : MutacionService

    private val mutacionDAO = HibernateMutacionDAO()
    private val dataDAO  = HibernateDataDAO()
    private val ubicacionDAO = HibernateUbicacionDAO()
    private val ubicacionNeo4j = UbicacionNeo4jDAO()
    private val vectorDAO = HibernateVectorDAO()
    private val especieDAO = HibernateEspecieDAO()
    private val patogenoDAO = HibernatePatogenoDAO()
    private val eventoDAO = MongoDBEventoDAO()
    private val neo4jUbicacionDAO =  UbicacionNeo4jDAO()
    private lateinit var china: Ubicacion
    private lateinit var argentina: Ubicacion
    private var virus = Patogeno("virus",100,100,100)

    @BeforeEach
    fun prepare(){
        this.ubicacionService = UbicacionServiceImpl(ubicacionNeo4j,ubicacionDAO, dataDAO, vectorDAO, eventoDAO)
        ubicacionService.clear()
        neo4jUbicacionDAO.clear()
        eventoDAO.clear()
        this.mutacionService = MutacionServiceImpl(mutacionDAO, dataDAO, especieDAO, eventoDAO)
        this.especieService = EspecieServiceImpl(especieDAO, dataDAO,ubicacionDAO,vectorDAO, eventoDAO)
        this.patogenoService = PatogenoServiceImpl(patogenoDAO,dataDAO, eventoDAO)
        this.vectorService = VectorServiceImpl(vectorDAO,dataDAO, especieDAO, ubicacionDAO, eventoDAO)
        this.estadisticasService = EstadisticasServiceImpl(especieDAO,dataDAO,ubicacionDAO)
        this.eventoService = FeedServiceImpl(eventoDAO)
        china =  ubicacionService.crear("China")
        argentina =  ubicacionService.crear("Argentina")
        virus = patogenoService.crear(virus)
    }
    /*********TESTS de feedPatogeno ************/
    @Test
    fun hayUnEventoDeMutacion() {
        Assert.assertEquals(1, eventoService.feedPatogeno("virus").size)
        Assert.assertEquals("Creacion",eventoService.feedPatogeno("virus")[0].descripcion())
        Assert.assertEquals("Mutacion",eventoService.feedPatogeno("virus")[0].tipoDeEvento())
    }

    @Test
    fun hayUnEventoDeMutacionYDeContagio() {
        val persona = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona2 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona3 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val esterichiaColi = patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)

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

        //Corroboro que sea pandemia
        Assert.assertTrue(especieService.esPandemia(esterichiaColi.id!!))

        //Corroboroo que haya dos eventos
        Assert.assertEquals(2, eventoService.feedPatogeno("virus").size)

        //Corroboro que sea el de pandemia
        Assert.assertEquals("virus",eventoService.feedPatogeno("virus")[0].getTipo())
        Assert.assertEquals("Pandemia",eventoService.feedPatogeno("virus")[0].descripcion())
//        Assert.assertEquals("Contagio",eventoService.feedPatogeno("virus")[0].fechaDeCreacion())
    }

    @Test
    fun hayUnEventoDeMutacionYDosDeContagio() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)
        val persona = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona2 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona3 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona4 = vectorService.crear(TipoDeVector.Persona, argentina.id!!)
        val esterichiaColi = patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)

        val espania = ubicacionService.crear("España")
        ubicacionService.crear("Canada")
        ubicacionService.crear("India")

        vectorService.infectar(persona.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona2.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona3.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona4.id!!,coronavirus.id!!)

        neo4jUbicacionDAO.conectar(china.id!!, argentina.id!!, "Maritimo")
        neo4jUbicacionDAO.conectar(china.id!!, espania.id!!, "Maritimo")
        ubicacionService.mover(persona.id!!, argentina.id!!)
        ubicacionService.mover(persona2.id!!, espania.id!!)

        //Corroboro que sea pandemia
        Assert.assertTrue(especieService.esPandemia(esterichiaColi.id!!))

        //Corroboroo que haya dos eventos
        Assert.assertEquals(5, eventoService.feedPatogeno("virus").size)

        //Corroboro que sea el de Contagio
        Assert.assertTrue(eventoService.feedPatogeno("virus").any { evento -> evento.tipoDeEvento == "Contagio" })

    }

    @Test
    fun hayDosEventosDeMutacionYDosDeContagio() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)
        val persona = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona2 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona3 = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val persona4 = vectorService.crear(TipoDeVector.Persona, argentina.id!!)
        val esterichiaColi = patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",china.id!!)
        var coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)

        val espania = ubicacionService.crear("España")
        ubicacionService.crear("Canada")
        ubicacionService.crear("India")

        vectorService.infectar(persona.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona2.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona3.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona4.id!!,coronavirus.id!!)

        neo4jUbicacionDAO.conectar(china.id!!, argentina.id!!, "Maritimo")
        neo4jUbicacionDAO.conectar(china.id!!, espania.id!!, "Maritimo")
        ubicacionService.mover(persona.id!!, argentina.id!!)
        ubicacionService.mover(persona2.id!!, espania.id!!)

        val requerimientos = mutableListOf<Mutacion>()
        val fiebre = mutacionService.crear(Mutacion("Fiebre",requerimientos, 6))
        coronavirus = especieService.recuperar(coronavirus.id!!)
        especieService.agregarPuntosADN(coronavirus.id!!, 8)
        mutacionService.mutar(coronavirus.id!!, fiebre.id!!)

        //Corroboro que sea pandemia
        Assert.assertTrue(especieService.esPandemia(esterichiaColi.id!!))

        //Corroboroo que haya dos eventos
        Assert.assertEquals(6, eventoService.feedPatogeno("virus").size)
    }
    /*********Fin TESTS de feedPatogeno ************/
    /*********TESTS de feedVector ************/

    @Test
    fun hayUnEventoDeArribo() {
        var persona = vectorService.crear(TipoDeVector.Persona,argentina.id!!)
        Assert.assertEquals(1, eventoService.feedVector(persona.id!!).size)
    }


    /*@Test
    fun hayUnEventoDeContagioyDosDeArribo(){
        var juan = vectorService.crear(TipoDeVector.Persona,argentina.id!!)
        vectorService.crear(TipoDeVector.Persona,china.id!!)
        var viruela = patogenoService.agregarEspecie(1,"viruela",argentina.id!!)
        vectorService.infectar(juan.id!!,viruela.id!!)
        neo4jUbicacionDAO.conectar(argentina.id!!, china.id!!, "Maritimo")
        ubicacionService.mover(juan.id!!, china.id!!)
        Assert.assertEquals(4, eventoService.feedVector(juan.id!!).size)
        Assert.assertEquals("Viajes", eventoService.feedVector(juan.id!!)[0].descripcion())
        Assert.assertEquals("Contagió", eventoService.feedVector(juan.id!!)[1].descripcion())
        Assert.assertEquals("Enfermedades", eventoService.feedVector(juan.id!!)[2].descripcion())
        Assert.assertEquals("Viajes", eventoService.feedVector(juan.id!!)[3].descripcion())
    }
*/
  /* @Test
    fun hayUnEventoDeContagioAlVectorContagiado(){
        var juan = vectorService.crear(TipoDeVector.Persona,argentina.id!!)
        var maria = vectorService.crear(TipoDeVector.Persona,china.id!!)
        var viruela = patogenoService.agregarEspecie(1,"viruela",argentina.id!!)
        vectorService.infectar(juan.id!!,viruela.id!!)
        neo4jUbicacionDAO.conectar(argentina.id!!, china.id!!, "Maritimo")
        ubicacionService.mover(juan.id!!, china.id!!)
        Assert.assertEquals(2, eventoService.feedVector(maria.id!!).size)
        Assert.assertEquals("Enfermedades", eventoService.feedVector(maria.id!!)[0].descripcion())
    }
*/
    /******TESTS de Ubicacion ****/
    @Test
    fun seRealizaronTresViajesAChina() {
        val persona1 = vectorService.crear(TipoDeVector.Persona, argentina.id!!)
        val persona2 = vectorService.crear(TipoDeVector.Persona, argentina.id!!)
        val persona3 = vectorService.crear(TipoDeVector.Persona, argentina.id!!)

        neo4jUbicacionDAO.conectar(argentina.id!!, china.id!!, "Maritimo")
        ubicacionService.mover(persona1.id!!, china.id!!)
        ubicacionService.mover(persona2.id!!, china.id!!)
        ubicacionService.mover(persona3.id!!, china.id!!)

        Assert.assertTrue(eventoService.feedUbicacion(china.id!!).all { i -> i.descripcion == "ViajesUbicacion"})
        Assert.assertEquals(3, eventoService.feedUbicacion(china.id!!).size)
    }

    @Test
    fun hay3ContagiosEnChina() {
        val persona1 = vectorService.crear(TipoDeVector.Persona, china.id!!)
        val persona2 = vectorService.crear(TipoDeVector.Persona, argentina.id!!)
        val persona3 = vectorService.crear(TipoDeVector.Persona, argentina.id!!)

        val esterichiaColi = patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",argentina.id!!)
        var coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",argentina.id!!)

        vectorService.infectar(persona2.id!!,esterichiaColi.id!!)
        vectorService.infectar(persona3.id!!,coronavirus.id!!)

        neo4jUbicacionDAO.conectar(argentina.id!!, china.id!!, "Maritimo")
        ubicacionService.mover(persona2.id!!, china.id!!)
        ubicacionService.mover(persona3.id!!, china.id!!)

        Assert.assertEquals(5, eventoService.feedUbicacion(china.id!!).count { i -> i.descripcion == "ContagioUbicacion" })
    }

    @AfterEach
    fun clear() {
        especieService.clear()
    }
}