package com.alextim.resource.controller;

import com.alextim.resource.response.IdResponse;
import com.alextim.resource.response.IdsResponse;
import com.alextim.resource.service.ResourceService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @PostMapping
    public ResponseEntity<IdResponse> uploadResource(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody byte[] file
    ) {
        Integer resourceId = resourceService.uploadResource(file, contentType);
        return ResponseEntity.ok(
                IdResponse.builder()
                        .id(resourceId)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable @Positive Integer id) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resourceService.getResource(id));
    }

    @DeleteMapping
    public ResponseEntity<IdsResponse> deleteResources(@RequestParam("id") String ids) {
        List<Integer> resourceIds = resourceService.deleteResources(ids);
        return ResponseEntity.ok().body(IdsResponse.builder()
                .ids(resourceIds)
                .build());
    }
}
