package ar.edu.unq.eperdemic.services.runner


import org.hibernate.Session

object TransactionRunner {
    private var session = ThreadLocal<Session>()


    val currentSession: Session
        get() {
            return session.get() ?: throw RuntimeException("No hay ninguna session en el contexto")
        }


    fun <T> runTrx(bloque: ()->T): T {
        if (haySessionAbierta()) {
            bloque()
        }

        session.set(SessionFactoryProvider.instance.createSession())
        currentSession.use {
            val tx =  currentSession!!.beginTransaction()
            try {
                //codigo de negocio
                val resultado = bloque()
                tx!!.commit()
                return resultado
            } catch (e: RuntimeException) {
                tx.rollback()
                throw e
            } finally {
                session.set(null)
            }
        }

    }

     fun haySessionAbierta() = session.get() != null

}