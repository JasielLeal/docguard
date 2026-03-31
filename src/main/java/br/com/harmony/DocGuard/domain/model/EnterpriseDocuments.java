package br.com.harmony.DocGuard.domain.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "enterprise_documents")
public class EnterpriseDocuments {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String legalName;

    private String type;
    private LocalDateTime regularDate;
    private String documentLink;

    @ManyToOne
    @JoinColumn(name = "enterprise_id")
    private Enterprise enterprise;

    private LocalDateTime lastModified;

    private LocalDateTime createdAt;

}
