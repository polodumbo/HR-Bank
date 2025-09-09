package com.codeit.HRBank.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "change_log_diffs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Change_log_diff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "long_id", nullable = false,
        foreignKey = @ForeignKey(name = "change_log_diffs_change_logs_id_fk"))
    private Change_log log;

    @Column(name = "property_name", nullable = false, length = 50)
    private String property_name;

    @Column(name = "before", nullable = false, length = 100)
    private String beforeValue;

    @Column(name = "after", nullable = false, length = 100)
    private String afterValue;
}
