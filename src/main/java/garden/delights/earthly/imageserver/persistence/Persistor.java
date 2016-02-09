package garden.delights.earthly.imageserver.persistence;

import static net.aequologica.neo.geppaequo.persistence.DbUtils.getEntityManagerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Persistor implements Closeable {

    @SuppressWarnings("unused")
    private final static Logger log = LoggerFactory.getLogger(Persistor.class);
    
    final private EntityManagerFactory emf;
    
    public Persistor() {
        this.emf = getEntityManagerFactory("image-server");
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
    
    public int deleteAll() throws SQLException, IOException {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            int theNumberOfEntitiesDeleted = em.createNamedQuery("DeleteAllPoints").executeUpdate();
            em.getTransaction().commit();
            return theNumberOfEntitiesDeleted;
        } finally {
            em.close();
        }
    }

    
    
}
