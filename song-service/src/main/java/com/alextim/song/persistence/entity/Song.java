package com.alextim.song.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Song {
    @Id
    private Integer id;

    private String name;
    private String artist;
    private String album;
    private String duration;
    private String year;
}