package garden.delights.earthly.persistence;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
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
    
    final private EntityManagerFactory emf;
    
    public Persistor() {
        this.emf = getEntityManagerFactory();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void close() throws IOException {
        if (emf != null && emf.isOpen()) {
            this.emf.close();
        }
    }

    public List<Point> get() throws SQLException, IOException {
        EntityManager em = emf.createEntityManager();
        try {
            @SuppressWarnings("unchecked")
            List<Point> resultList = em.createNamedQuery("AllPoints").getResultList();
            return resultList;
        } finally {
            em.close();
        }
    }

    public long getCount() {
        EntityManager em = emf.createEntityManager();
        try {
            Object pointCount = em.createNamedQuery("PointsCount").getSingleResult();
            if (pointCount instanceof Number) {
                return ((Number)pointCount).longValue();
            } else {
                return Long.valueOf(pointCount.toString());
            }
        } finally {
            em.close();
        }
    }
    
    public void store(long x, long y) throws ServletException, IOException, SQLException {
        EntityManager em = emf.createEntityManager();
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
    
    static private EntityManagerFactory getEntityManagerFactory() {

        // try remote
        {
            EntityManagerFactory emfRemote  = null;
            EntityManager        em         = null;
            try {
                InitialContext      ctx         = new InitialContext();
                DataSource          ds          = (DataSource)ctx.lookup("java:comp/env/jdbc/DefaultDB");
                Map<Object, Object> properties  = new HashMap<>();
                
                properties.put(PersistenceUnitProperties.NON_JTA_DATASOURCE, ds);
                emfRemote = Persistence.createEntityManagerFactory("image-server", properties);

                em = emfRemote.createEntityManager();
                
                return emfRemote;
            } catch (Exception e) {
                log.error("emfRemote.createEntityManager threw this exception and I ignored it" , e);
            } finally {
                if (em != null) {
                    em.close();
                }
            }
        }

        // try local
        {
            EntityManagerFactory    emfLocal    = null;
            EntityManager           em          = null;
            try {
                emfLocal = Persistence.createEntityManagerFactory("image-server-local");
                em = emfLocal.createEntityManager();
                return emfLocal;
            } catch (Exception e) {
                log.error("emfLocal.createEntityManager threw this exception and I ignored it" , e);
            } finally {
                if (em != null) {
                    em.close();
                }
            }
        }

       return null;
    }

}
