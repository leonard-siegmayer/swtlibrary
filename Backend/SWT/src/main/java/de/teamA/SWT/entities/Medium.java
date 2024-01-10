package de.teamA.SWT.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Entity
@Table(name = "medium")
@JsonInclude(Include.NON_NULL)
public class Medium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(cascade = javax.persistence.CascadeType.ALL, mappedBy = "medium", fetch = FetchType.EAGER)
    private Set<Note> notes = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "author_medium", joinColumns = @JoinColumn(name = "medium_id", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "person_id", referencedColumnName = "ID"))
    private Set<Person> authors = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "editor_medium", joinColumns = @JoinColumn(name = "medium_id", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "person_id", referencedColumnName = "ID"))
    private Set<Person> editors = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "tags_medium", joinColumns = @JoinColumn(name = "medium_id", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "ID"))
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "keywords_medium", joinColumns = @JoinColumn(name = "medium_id", referencedColumnName = "ID"), inverseJoinColumns = @JoinColumn(name = "keyword_id", referencedColumnName = "ID"))
    private Set<Keyword> keywords = new HashSet<>();

    @OneToMany(cascade = javax.persistence.CascadeType.ALL, mappedBy = "medium", fetch = FetchType.EAGER)
    private Set<PhysicalMedium> physicals = new HashSet<>();

    @Column(name = "date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate creationDate = LocalDate.now();

    @Column(name = "booktitle", length = 1000)
    private String booktitle;

    @Column(name = "title")
    private String title;

    @JsonProperty("abstract")
    @Lob
    @Column(name = "abstract", length = 65535)
    private String annote;

    @Column(name = "address")
    private String address;

    @Column(name = "chapter")
    private String chapter;

    @Column(name = "edition")
    private String edition;

    @Column(name = "how_Published")
    private String howPublished;

    @Column(name = "institution")
    private String institution;

    @Column(name = "number")
    private String number;

    @Column(name = "organization")
    private String organization;

    @Column(name = "pages")
    private String pages;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "series")
    private String series;

    @Column(name = "type")
    private String type;

    @Column(name = "volume")
    private String volume;

    @Column(name = "year")
    private String year;

    @Column(name = "doi")
    private String doi;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "language")
    private String language;

    @Column(name = "cover_url")
    private String coverURL;

    @Column(name = "rvk_signature")
    private String rvkSignature;

    @Column(name = "ean")
    private String ean;

    @Transient
    @Column(name = "handapparat")
    private boolean handapparat;

    @Transient
    private User owner;

    @Transient
    @Column(name = "location")
    private String location = "";

    @Transient
    @Column(name = "department")
    private String department = "";

    @Transient
    @Column(name = "room")
    private String room = "";

    @OneToMany(cascade = javax.persistence.CascadeType.ALL, mappedBy = "physicalID", fetch = FetchType.EAGER)
    @Column(name = "reservation")
    private Set<Reservation> reservation = new HashSet<>();

    @Column(name = "resDate")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @ElementCollection(targetClass = LocalDate.class)
    private List<LocalDate> resDate = new ArrayList<>();

    @Column(name = "borrowDate")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @ElementCollection(targetClass = LocalDate.class)
    private List<LocalDate> borrowDate = new ArrayList<>();

    @Column(name = "last_edited")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastEdited;

    public Medium() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Note> getNotes() {
        return notes;
    }

    public void setNotes(Set<Note> notes) {
        this.notes = notes;
    }

    public Set<Person> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Person> authors) {
        this.authors = authors;
    }

    public Set<Person> getEditors() {
        return editors;
    }

    public void setEditors(Set<Person> editors) {
        this.editors = editors;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<Keyword> keywords) {
        this.keywords = keywords;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getBooktitle() {
        return booktitle;
    }

    public void setBooktitle(String booktitle) {
        this.booktitle = booktitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnnote() {
        return annote;
    }

    public void setAnnote(String annote) {
        this.annote = annote;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getHowPublished() {
        return howPublished;
    }

    public void setHowPublished(String howPublished) {
        this.howPublished = howPublished;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCoverURL() {
        return coverURL;
    }

    public void setCoverURL(String coverURL) {
        this.coverURL = coverURL;
    }

    public String getRvkSignature() {
        return rvkSignature;
    }

    public void setRvkSignature(String rvkSignature) {
        this.rvkSignature = rvkSignature;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public boolean isHandapparat() {
        return handapparat;
    }

    public void setHandapparat(boolean handapparat) {
        this.handapparat = handapparat;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(LocalDateTime lastEdited) {
        this.lastEdited = lastEdited;
    }

    public void addPhysical(PhysicalMedium physicalMedium) {
        physicals.add(physicalMedium);
    }

    @Transient
    public User getOwner() {
        return owner;
    }

    public void setOwner(User user) {
        owner = user;
    }

    public Set<PhysicalMedium> getPhysicals() {
        return physicals;
    }

    public void setPhysicals(Set<PhysicalMedium> physicals) {
        this.physicals = physicals;
    }

    public Set<Reservation> getReservation() {
        if (reservation == null) {
            return new HashSet<>();
        }
        return reservation;
    }

    public void setReservation(Set<Reservation> reservation) {
        if (reservation == null) {
            this.reservation = new HashSet<>();
        } else {
            this.reservation = reservation;
        }
    }

    public List<LocalDate> getResDate() {
        if (resDate == null) {
            this.resDate = new ArrayList<LocalDate>();
        }
        return resDate;
    }

    public void setResDate(List<LocalDate> resDate) {
        if (resDate == null) {
            this.resDate = new ArrayList<LocalDate>();
        } else {
            this.resDate = resDate;
        }
    }

    public List<LocalDate> getBorrowDate() {
        if (borrowDate == null) {
            this.borrowDate = new ArrayList<LocalDate>();
        }
        return borrowDate;
    }

    public void setBorrowDate(List<LocalDate> borrowDate) {
        if (borrowDate == null) {
            this.borrowDate = new ArrayList<LocalDate>();
        } else {
            this.borrowDate = borrowDate;
        }
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

    @JsonIgnore
    public String getShortInfo() {
        String shortInfo = booktitle;
        if (title != null && !title.isEmpty()) {
            shortInfo = shortInfo + ": " + title;
        }
        return shortInfo;
    }

}
