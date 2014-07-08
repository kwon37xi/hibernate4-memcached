package kr.pe.kwonnam.hibernate4memcached.example.entity;

import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KwonNam Son (kwon37xi@gmail.com)
 */
@Entity
@Table(name = "employees")
@Cache(region = "employees", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Employee implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", length = 32, nullable = false)
    private String name;

    @Column(name = "position", length = 32, nullable = false)
    private String position;

    @OneToMany
    @JoinColumn(name = "boss_id", referencedColumnName = "id", nullable = true)
    @org.hibernate.annotations.OrderBy(clause = "name")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private List<Employee> workers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<Employee> getWorkers() {
        return workers;
    }

    public void setWorkers(List<Employee> workers) {
        this.workers = workers;
    }

    public void addWorker(Employee worker) {
        if (workers == null) {
            workers = new ArrayList<Employee>();
        }

        workers.add(worker);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", position='" + position + '\'' +
                ", workers=" + workers +
                '}';
    }
}
