package com.dxvkstatecachebank.dxvkstatecachebank.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Blob;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CacheFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "upload_date",
            nullable = false
    )
    private LocalDateTime uploadDateTime;

    @ManyToOne
    @JoinColumn(
            foreignKey =
            @ForeignKey(name = "uploader_id"),
            nullable = false
    )
    private User uploader;

    @ManyToOne
    @JoinColumn(
            foreignKey =
            @ForeignKey(name = "game_id"),
            nullable = false
    )
    private Game game;

    @Lob
    private Blob data;
}
