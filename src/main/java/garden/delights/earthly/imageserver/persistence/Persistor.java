package garden.delights.earthly.imageserver.persistence;

import static garden.delights.earthly.imageserver.persistence.Persistor.ImageServerFactory.IMAGESERVER;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.aequologica.neo.geppaequo.persistence.DbHelpers.AbstractFactory;
import net.aequologica.neo.geppaequo.persistence.DbHelpers.CloseableEntityManager;
import net.aequologica.neo.geppaequo.persistence.DbHelpers.CloseableEntityTransaction;
import net.aequologica.neo.geppaequo.persistence.DbHelpers.Factory;

public class Persistor {

    @SuppressWarnings("unused")
    private final static Logger log = LoggerFactory.getLogger(Persistor.class);
    
    private final Factory factory;
    
    public Persistor() {
        this.factory = IMAGESERVER;
    }

    public List<Point> get() throws IOException {
        try (CloseableEntityManager cem = CloseableEntityManager.create(this.factory)) {
            @SuppressWarnings("unchecked")
            List<Point> resultList = cem.em.createNamedQuery("AllPoints").getResultList();
            return resultList;
        }
    }

    public long getCount() throws IOException {
        try (CloseableEntityManager cem = CloseableEntityManager.create(this.factory)) {
            Object pointCount = cem.em.createNamedQuery("PointsCount").getSingleResult();
            if (pointCount instanceof Number) {
                return ((Number)pointCount).longValue();
            } else {
                return Long.valueOf(pointCount.toString());
            }
        }
    }
    
    public void store(long x, long y) throws ServletException, IOException, SQLException {
        try (CloseableEntityManager cem = CloseableEntityManager.create(this.factory)) {
            Point person = new Point();
            person.setX(x);
            person.setY(y);
            cem.em.getTransaction().begin();
            cem.em.persist(person);
            cem.em.getTransaction().commit();
        }
    }
    
    public int deleteAll() throws SQLException, IOException {
        try (CloseableEntityManager cem = CloseableEntityManager.create(this.factory)) {
            try (CloseableEntityTransaction cet = CloseableEntityTransaction.create(cem.em)) {
                int theNumberOfEntitiesDeleted = cem.em.createNamedQuery("DeleteAllPoints").executeUpdate();
                cet.t.commit();
                return theNumberOfEntitiesDeleted;
            }
        }
    }

    public enum ImageServerFactory implements net.aequologica.neo.geppaequo.persistence.DbHelpers.Factory {
        
        IMAGESERVER;
        
        AbstractFactory abstractFactory = new AbstractFactory() {

            @Override
            protected String getPersistenceUnitName() {
                return "image-server";
            }
            
        };

        @Override
        public EntityManagerFactory getEntityManagerFactory() {
            return abstractFactory.getEntityManagerFactory();
        }

        @Override
        public Exception getException() {
            return abstractFactory.getException();
        }

        @Override
        public EntityManagerFactory load() {
            return abstractFactory.load();
        }
    }
    
}
