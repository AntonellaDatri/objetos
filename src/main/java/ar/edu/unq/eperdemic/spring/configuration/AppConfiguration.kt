package ar.edu.unq.eperdemic.spring.configuration

import ar.edu.unq.eperdemic.persistencia.dao.*
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernateMutacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.HibernatePatogenoDAO
import ar.edu.unq.eperdemic.services.*
import ar.edu.unq.eperdemic.services.impl.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfiguration {

    @Bean
    fun groupName() : String {
        val groupName :String?  = System.getenv()["GROUP_NAME"]
        return groupName!!
    }

    @Bean
    fun patogenoDAO(): PatogenoDAO {
        return HibernatePatogenoDAO()
    }

    @Bean
    fun patogenoService(patogenoDAO: PatogenoDAO,ubicacionDAO: UbicacionDAO, dataDAO: DataDAO, eventoDAO: EventoDAO): PatogenoService {
        return PatogenoServiceImpl(patogenoDAO, dataDAO, eventoDAO)
    }

    @Bean
    fun estadisticasServices(dataDAO: DataDAO, especieDAO: EspecieDAO, ubicacionDAO: UbicacionDAO): EstadisticasService {
        return EstadisticasServiceImpl( especieDAO, dataDAO, ubicacionDAO)
    }

    @Bean
    fun mutacionService( mutacionDAO: MutacionDAO , dataDAO: DataDAO, especieDAO: EspecieDAO, eventoDAO: EventoDAO): MutacionService {
        return MutacionServiceImpl(mutacionDAO, dataDAO, especieDAO, eventoDAO)
    }

    fun ubicacionService(ubicacionNeO: NeoUbicacionDAO,ubicacionDAO: UbicacionDAO, dataDAO: DataDAO, vectorDAO : VectorDAO, especieDAO: EspecieDAO, eventoDAO: EventoDAO): UbicacionService {
        return UbicacionServiceImpl(ubicacionNeO,ubicacionDAO, dataDAO, vectorDAO, eventoDAO)
    }

    @Bean
    fun vectorService(vectorDAO: VectorDAO, dataDAO: DataDAO, especieDAO: EspecieDAO,ubicacionDAO:UbicacionDAO, eventoDAO: EventoDAO): VectorService {
        return VectorServiceImpl(vectorDAO, dataDAO, especieDAO,ubicacionDAO, eventoDAO)
    }

    @Bean
    fun especieService(especieDAO: EspecieDAO, dataDAO: DataDAO, ubicacionDAO: UbicacionDAO, vectorDAO: VectorDAO, eventoDAO: EventoDAO): EspecieService {
        return EspecieServiceImpl(especieDAO, dataDAO, ubicacionDAO, vectorDAO, eventoDAO)
    }

}
