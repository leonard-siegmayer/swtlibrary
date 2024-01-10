package de.teamA.SWT.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;

@Entity
@Table(name = "person")
public class Person implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    public Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    @Override
    public boolean isNew() {
        return getId() == null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if ((o == null) || !(o instanceof Person))
            return false;

        final Person p = (Person) o;

        if (id != null && p.getId() != null) {
            return id.equals(p.getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", \n isNew: " + isNew();
    }
}
