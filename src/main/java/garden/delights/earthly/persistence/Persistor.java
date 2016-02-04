package garden.delights.earthly.persistence;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import garden.delights.earthly.model.Point;

public class Persistor implements Closeable {

    private final static Logger log = LoggerFactory.getLogger(Persistor.class);
    
    static private EntityManagerFactory emf = null;
    static private EntityManagerFactory emfRemote = null;
    static private EntityManagerFactory emfLocal = null;
    
    static EntityManager getEntityManager() {
        if (emf != null) {
            return emf.createEntityManager();
        } 
        // try remote
        emf = emfRemote;
        try {
            return emf.createEntityManager();
        } catch (Exception e) {
            emf = null;
            log.error("emfRemote.createEntityManager threw an exception and I ignored it" , e);
        }
        // try local
        emf = emfLocal;
        try {
            return emf.createEntityManager();
        } catch (Exception e2) {
            emf = null;
            log.error("emfLocal.createEntityManager threw an exception and I ignored it" , e2);
        }
        return null;
    }
    
    static {
        try {
            InitialContext ctx = new InitialContext();
            {
                DataSource ds = (DataSource) ctx.lookup("java:comp/env/jdbc/DefaultDB");
                Map<Object, Object> properties = new HashMap<>();
                properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
                emfRemote = Persistence.createEntityManagerFactory("image-server", properties);
            }
            emfLocal = Persistence.createEntityManagerFactory("image-server-local");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        
    }
    
    public Persistor() {
    }

    @Override
    public void close() throws IOException {
    }

    public List<Point> get() throws SQLException, IOException {
        EntityManager em = getEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Point> resultList = em.createNamedQuery("AllPoints").getResultList();
            return resultList;
        } finally {
            em.close();
        }
    }

    public void store(long x, long y) throws ServletException, IOException, SQLException {
        EntityManager em = getEntityManager();
        try {
            Point person = new Point();
            person.setX(x);
            person.setY(y);
            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
