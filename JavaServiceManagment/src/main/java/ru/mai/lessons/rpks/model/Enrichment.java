package ru.mai.lessons.rpks.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "enrichment_rules")
public class Enrichment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Min(value = 1)
    private long id;

    @Min(value = 1)
    private long enrichmentId;

    @Min(value = 1)
    private long ruleId;

    @NotBlank
    private String fieldName;

    @NotBlank
    private String fieldNameEnrichment;

    @NotBlank
    private String fieldValue;

    @NotBlank
    private String fieldValueDefault;
}

