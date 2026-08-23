package com.alextim.song.repository;

import com.alextim.song.persistence.entity.Song;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SongRepository extends CrudRepository<Song, Integer> {
    @Query("select s.id from Song s where s.id in :ids")
    List<Integer> findExistingIds(@Param("ids") List<Integer> ids);
}
