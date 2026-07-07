package com.example.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "email_sending_message", indexes = {
        @Index(name = "IDX_EMAIL_SENDING_MESSAGE_ID", columnList = "id")
})

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendingMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Integer version;

    private Date createTs;
    private String createdBy;

    private Date updateTs;
    private String updatedBy;

    private Date deleteTs;
    private String deletedBy;

    private String address; // TO
    private String from;
    private String cc;
    private String bcc;

    @Column(length = 500)
    private String subject;

    @Lob
    private String contentText;

    private Integer status;

    private Date dateSent;

    private Date deadline;

    private Integer attemptsLimit;
    private Integer attemptsMade;

    private String attachmentsName;

    private String headers;

    private String bodyContentType;

    private String sysTenantId;

    private Boolean important = false;
}