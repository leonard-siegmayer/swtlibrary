package de.teamA.SWT.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "physical")
@JsonInclude(Include.NON_NULL)
public class PhysicalMedium {

    @ManyToOne
    @JoinColumn(name = "medium_id")
    protected Medium medium;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "owner")
    private User owner;

    @Column(name = "date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate creationDate = LocalDate.now();

    @Column(name = "location")
    private String location = "";

    @Column(name = "resCount")
    private Integer resCount = 0;

    @Column(name = "department")
    private String department = "";

    @Column(name = "room")
    private String room = "";

    @Column(name = "handapparat")
    private boolean handapparat;

    @Column(name = "rvk_signature")
    private String rvkSignature;

    @Enumerated(EnumType.STRING)
    private PhysicalStatus status = PhysicalStatus.AVAILABLE;

    public PhysicalMedium() {
        this.status = PhysicalStatus.AVAILABLE;
    }

    public PhysicalMedium(User owner, String location, String department, String room, boolean handapparat,
            String rvkSignature, Medium logicalMedium) {
        this.owner = owner;
        this.location = location;
        this.department = department;
        this.room = room;
        this.handapparat = handapparat;
        this.medium = logicalMedium;
        this.rvkSignature = rvkSignature;
        this.status = PhysicalStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public Medium getMedium() {
        return medium;
    }

    public void setMedium(Medium medium) {
        this.medium = medium;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isHandapparat() {
        return handapparat;
    }

    public void setHandapparat(boolean handapparat) {
        this.handapparat = handapparat;
    }

    public String getRvkSignature() {
        return rvkSignature;
    }

    public void setRvkSignature(String rvkSignature) {
        this.rvkSignature = rvkSignature;
    }

    public long getMediumId() {
        return medium.getId();
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PhysicalMedium that = (PhysicalMedium) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public PhysicalStatus getStatus() {
        return status;
    }

    public void setStatus(PhysicalStatus status) {
        if (status == null) {
            this.status = PhysicalStatus.AVAILABLE;
        }
        this.status = status;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(medium.getBooktitle());

        if (medium.getTitle() != null && !medium.getTitle().isEmpty()) {
            sb.append(": ");
            sb.append(medium.getTitle());
        }
        if (location != null && !location.isEmpty()) {
            sb.append("(" + location + ")");
        }
        sb.append(". ");

        if (owner != null) {
            sb.append("Owned by: " + owner.getName() + " ");
        }

        sb.append("Physical ID: " + id);

        return sb.toString();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Integer getResCount() {
        return resCount;
    }

    public void setResCount(Integer resCount) {
        this.resCount = resCount;
    }

    public void decrementResCount() {
        this.setResCount(this.getResCount() - 1);
    }
}
