package garden.delights.earthly.model;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "T_POINT")
@NamedQueries({
    @NamedQuery(name  = "AllPoints", 
                query = "SELECT p FROM Point p"),
    @NamedQuery(name  = "PointsCount", 
                query = "SELECT COUNT(p) FROM Point p"),
    @NamedQuery(name  = "DeleteAllPoints", 
                query = "DELETE FROM Point"),
}) 
@JsonIgnoreProperties
public class Point {
    
    @Id
    @GeneratedValue
    @JsonProperty
    private Long id;
    @Basic
    @JsonProperty
    private long x;
    @Basic
    @JsonProperty
    private long y;

    public Point() {
    }

    public Point(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getX() {
        return x;
    }

    public void setX(long x) {
        this.x = x;
    }

    public long getY() {
        return y;
    }

    public void setY(long y) {
        this.y = y;
    }

}
