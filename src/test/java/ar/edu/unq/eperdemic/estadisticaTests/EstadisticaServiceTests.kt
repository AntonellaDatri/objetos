package ar.edu.unq.eperdemic.estadisticaTests

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.TipoDeVector
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.persistencia.dao.mongoDB.MongoDBEventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.services.*
import ar.edu.unq.eperdemic.services.impl.*
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EstadisticaServiceTests {

    private lateinit var especieService: EspecieService
    private lateinit var patogenoService: PatogenoService
    private lateinit var ubicacionService: UbicacionService
    private lateinit var vectorService: VectorService
    private lateinit var estadisticasService: EstadisticasService
    private val dataDAO  = HibernateDataDAO()
    private val ubicacionDAO = HibernateUbicacionDAO()
    private val ubicacionNeo4j = UbicacionNeo4jDAO()
    private val vectorDAO = HibernateVectorDAO()
    private val especieDAO = HibernateEspecieDAO()
    private val patogenoDAO = HibernatePatogenoDAO()
    private val eventoDAO = MongoDBEventoDAO()
    // private val estadisticaDAO = HibernateEstadistic
    private lateinit var china: Ubicacion
    private lateinit var argentina: Ubicacion
    private var virus = Patogeno("virus",10,20,30)

    @BeforeEach
    fun prepare(){
        this.especieService = EspecieServiceImpl(especieDAO, dataDAO,ubicacionDAO,vectorDAO, eventoDAO)
        this.patogenoService = PatogenoServiceImpl(patogenoDAO,dataDAO, eventoDAO)
        this.ubicacionService = UbicacionServiceImpl(ubicacionNeo4j,ubicacionDAO, dataDAO, vectorDAO, eventoDAO)
        this.vectorService = VectorServiceImpl(vectorDAO,dataDAO, especieDAO, ubicacionDAO, eventoDAO)
        this.estadisticasService = EstadisticasServiceImpl(especieDAO,dataDAO,ubicacionDAO)
        china =  ubicacionService.crear("China")
        argentina =  ubicacionService.crear("Argentina")
        virus = patogenoService.crear(virus)
    }


    @Test
    fun seContagianDosPersonasConCoronavirusYUnaConRubiolaLaEspecieLiderEsCorona(){

        val  marta = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val  holga = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)

        vectorService.infectar(marta.id!!, coronavirus.id!!)
        vectorService.infectar(holga.id!!, coronavirus.id!!)

        val  juan = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val rubiola = patogenoService.agregarEspecie(virus.id!!,"Rubiola",china.id!!)
        vectorService.infectar(juan.id!!, rubiola.id!!)

        val especieLider = estadisticasService.especieLider()

        Assert.assertTrue(especieLider.id == coronavirus.id)
    }
    @Test
    fun seContagianDosInsectosConCoronavirusYPersonaConRubiolaLaEspecieLiderEsRubiola(){

        val  pulga = vectorService.crear(TipoDeVector.Insecto,china.id!!)
        val garrapata = vectorService.crear(TipoDeVector.Insecto,china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)

        vectorService.infectar(pulga.id!!, coronavirus.id!!)
        vectorService.infectar(garrapata.id!!, coronavirus.id!!)

        val  juan = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val rubiola = patogenoService.agregarEspecie(virus.id!!,"Rubiola",china.id!!)
        vectorService.infectar(juan.id!!, rubiola.id!!)

        val especieLider = estadisticasService.especieLider()

        Assert.assertTrue(especieLider.id == rubiola.id)
    }
    @Test
    fun seContagiaSoloUnaPersonaConCoronavirusYLaEspecieLiderEsEsa(){
        //se crea un vector persona y se contagia de coronavirus
        val persona = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)
        vectorService.infectar(persona.id!!, coronavirus.id!!)

        val lideres = estadisticasService.lideres()
        val especieLider = estadisticasService.especieLider()

        Assert.assertTrue(especieLider.id == coronavirus.id)
        Assert.assertTrue(lideres.size == 1)
        Assert.assertTrue(lideres.map { lider -> lider.id }.contains(coronavirus.id))

    }
    @Test
    fun seContagianDosPersonasUnaConRubiolaYOtraConCoronavirusAmbasEspeciesSonLideres(){
        //juan se infecta de coronavirus
        val  juan = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)
        vectorService.infectar(juan.id!!, coronavirus.id!!)

        //miguel se infecta de rubiola
        val  miguel = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val rubiola = patogenoService.agregarEspecie(virus.id!!,"rubiola",china.id!!)
        vectorService.infectar(miguel.id!!, rubiola.id!!)

        val lideres = estadisticasService.lideres()

        Assert.assertTrue(lideres.size == 2)
        Assert.assertTrue(lideres.map { lider -> lider.id }.contains(coronavirus.id))
        Assert.assertTrue(lideres.map { lider -> lider.id }.contains(rubiola.id))
    }

    @Test
    fun seContagiaUnaPersonaDeCoronavirusYUniVirusDeRubiolaElLiderEsElCoronavirus(){
        //juan se infecta de coronavirus
        val juan = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)
        vectorService.infectar(juan.id!!, coronavirus.id!!)

        //pulga se infecta de rubiola
        val  pulga = vectorService.crear(TipoDeVector.Insecto,china.id!!)
        val rubiola = patogenoService.agregarEspecie(virus.id!!,"rubiola",china.id!!)
        vectorService.infectar(pulga.id!!, rubiola.id!!)

        val lideres = estadisticasService.lideres()
        Assert.assertTrue(lideres.size == 1)
        Assert.assertTrue(lideres.map { lider -> lider.id }.contains(coronavirus.id))

    }
    @Test
    fun seContagianTresInsectosConCoronaviruslaNoSeObtienenLideres(){
        val  garrapata = vectorService.crear(TipoDeVector.Insecto,china.id!!)
        val  pulga = vectorService.crear(TipoDeVector.Insecto,china.id!!)
        val  mosquito = vectorService.crear(TipoDeVector.Insecto,china.id!!)

        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)
        vectorService.infectar(garrapata.id!!, coronavirus.id!!)
        vectorService.infectar(pulga.id!!, coronavirus.id!!)
        vectorService.infectar(mosquito.id!!, coronavirus.id!!)

        val lideres = estadisticasService.lideres()
        Assert.assertTrue(lideres.isEmpty())
    }

    // se contagian 2 insectos y 1 animal con corona, 2 personas con rubiola , la especie lider es primero rubiola

    @Test
    fun seContagianDosInsectosConCoronavirusYUnAnimalConRubiolaEseEsLaEspecieLider(){
        //Se agregan los insectos y se infectan
        val  garrapata = vectorService.crear(TipoDeVector.Insecto,china.id!!)
        val  pulga = vectorService.crear(TipoDeVector.Insecto,china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)

        vectorService.infectar(garrapata.id!!, coronavirus.id!!)
        vectorService.infectar(pulga.id!!, coronavirus.id!!)
        //se infecta un animal

        val  juan = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val rubiola = patogenoService.agregarEspecie(virus.id!!,"Rubiola",china.id!!)
        vectorService.infectar(juan.id!!, rubiola.id!!)

        val lideres = estadisticasService.lideres()
        Assert.assertTrue(lideres.size == 1)
        Assert.assertTrue(lideres.map { lider -> lider.id }.contains(rubiola.id))
    }

    @Test
    fun seAgregaRubiolaAUnInsectoYUnAnimalCoronavirusAUnAnimalYUnaPersonaEstaEspecieEsPrimeraEnLider(){

        //se infecta con rubiola
        val  garrapata = vectorService.crear(TipoDeVector.Insecto,china.id!!)
        val  perro = vectorService.crear(TipoDeVector.Animal,china.id!!)

        val rubiola = patogenoService.agregarEspecie(virus.id!!,"Rubiola",china.id!!)

        vectorService.infectar(perro.id!!, rubiola.id!!)
        vectorService.infectar(garrapata.id!!, rubiola.id!!)

        //se infecta con coronavirus
        val  juan = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val  gato = vectorService.crear(TipoDeVector.Animal,china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)
        vectorService.infectar(juan.id!!, coronavirus.id!!)
        vectorService.infectar(gato.id!!, coronavirus.id!!)

        val lideres = estadisticasService.lideres()
        val especieLider = estadisticasService.especieLider()

        Assert.assertTrue(lideres.size == 2)
        //verifico que la especie lider sea el coronavirus
        Assert.assertTrue(especieLider.id == coronavirus.id)
        Assert.assertTrue(lideres[0].id == coronavirus.id)
        Assert.assertTrue(lideres[1].id == rubiola.id)
    }

    @Test
    fun devuelveUnReporteDadaUnaUbicacionConCuatroVectoresTresInfectadosYEpecieMasInfeccionsaEsRubiola() {
        val  juan = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"coronavirus",china.id!!)
        vectorService.infectar(juan.id!!, coronavirus.id!!)

        val  garrapata = vectorService.crear(TipoDeVector.Insecto,china.id!!)
        val  perro = vectorService.crear(TipoDeVector.Animal,china.id!!)
        vectorService.crear(TipoDeVector.Animal,china.id!!)

        val rubiola = patogenoService.agregarEspecie(virus.id!!,"Rubiola",china.id!!)
        vectorService.infectar(garrapata.id!!, rubiola.id!!)
        vectorService.infectar(perro.id!!, rubiola.id!!)

        val reporteDeContagio = estadisticasService.reporteDeContagios(china.id!!)

        Assert.assertEquals( reporteDeContagio.vectoresPresentes,4)
        Assert.assertEquals( reporteDeContagio.vectoresInfectados,3)
        Assert.assertEquals(reporteDeContagio.nombreDeEspecieMasInfecciosa, rubiola.nombre)
    }

    @Test
    fun seInfectanDeCoronavirusUnVectorEnArgentinaYDosEnChina() {
        val  juan = vectorService.crear(TipoDeVector.Persona,china.id!!)
        val  gato = vectorService.crear(TipoDeVector.Animal,china.id!!)
        val coronavirus = patogenoService.agregarEspecie(virus.id!!,"Coronavirus",china.id!!)
        vectorService.infectar(juan.id!!, coronavirus.id!!)
        vectorService.infectar(gato.id!!, coronavirus.id!!)

        val  garrapata = vectorService.crear(TipoDeVector.Insecto,argentina.id!!)
        vectorService.infectar(garrapata.id!!, coronavirus.id!!)

        val reporteDeContagioChina = estadisticasService.reporteDeContagios(china.id!!)

        Assert.assertEquals(reporteDeContagioChina.nombreDeEspecieMasInfecciosa, coronavirus.nombre)
        Assert.assertEquals( reporteDeContagioChina.vectoresInfectados,2)
        Assert.assertEquals( reporteDeContagioChina.vectoresPresentes,2)

        val reporteDeContagioArgentina = estadisticasService.reporteDeContagios(argentina.id!!)

        Assert.assertEquals(reporteDeContagioArgentina.nombreDeEspecieMasInfecciosa, coronavirus.nombre)
        Assert.assertEquals( reporteDeContagioArgentina.vectoresInfectados,1)
        Assert.assertEquals( reporteDeContagioArgentina.vectoresPresentes,1)



    }
    @AfterEach
    fun clear() {
        especieService.clear()
    }

}