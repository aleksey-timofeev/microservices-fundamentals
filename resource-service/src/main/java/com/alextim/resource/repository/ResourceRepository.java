package com.alextim.resource.repository;

import com.alextim.resource.persistence.entity.Resource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends CrudRepository<Resource, Integer> {
    @Query("select r.id from Resource r where r.id in :ids")
    List<Integer> findExistingIds(@Param("ids") List<Integer> ids);
}
