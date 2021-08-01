package ar.edu.unq.eperdemic.persistencia.dao.neo4j

import org.neo4j.driver.*

open class Neo4jDAO {
    protected val driver: Driver
    init {
        val env = System.getenv()
        val url = env.getOrDefault("NEO_URL", "bolt://localhost:7687")
        val username = env.getOrDefault("NEO_USER", "neo4j")
        val password = env.getOrDefault("NEO_PASSWORD", "root")

        driver = GraphDatabase.driver(url, AuthTokens.basic(username, password),
            Config.builder().withLogging(Logging.slf4j()).build()


        )
    }
}