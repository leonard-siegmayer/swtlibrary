package de.teamA.SWT.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import de.teamA.SWT.entities.converters.MetaInfoConverter;
import de.teamA.SWT.entities.converters.StringListConverter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Represents a database Entry the EmailService uses to check whether an Email
 * has already been sent.
 */
@Entity
@Table(name = "sent_mails")
public class EmailEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "cc")
    @Convert(converter = StringListConverter.class)
    private List<String> cc;

    @Column(name = "type")
    private String type;

    @Column(name = "date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate date;

    @Column(name = "meta_info")
    @Convert(converter = MetaInfoConverter.class)
    private Map<String, List<String>> metaInfo;

    public EmailEntry() {
    }

    public EmailEntry(String recipient, List<String> cc, String type, LocalDate date,
            Map<String, List<String>> metaInfo) {
        this.recipient = recipient;
        this.cc = cc;
        this.type = type;
        this.date = date;
        this.metaInfo = metaInfo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Map<String, List<String>> getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(Map<String, List<String>> metaInfo) {
        this.metaInfo = metaInfo;
    }

}
