package com.alextim.song.controller;

import com.alextim.song.request.SongRequest;
import com.alextim.song.response.IdResponse;
import com.alextim.song.response.IdsResponse;
import com.alextim.song.response.SongResponse;
import com.alextim.song.service.SongService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {
    private final SongService songService;

    @PostMapping
    public ResponseEntity<IdResponse> createSongMetadata(@Valid @RequestBody SongRequest request) {
        songService.saveSongMetadata(request);
        return ResponseEntity.ok(
                IdResponse.builder()
                        .id(request.getId())
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> getSongMetadata(@PathVariable @Positive Integer id) {
        SongResponse songResponse = songService.getSongMetadata(id);
        return ResponseEntity.ok().body(songResponse);
    }

    @DeleteMapping
    public ResponseEntity<IdsResponse> deleteSongsMetadata(@RequestParam("id") String ids) {
        List<Integer> resourceIds = songService.deleteSongMetadata(ids);
        return ResponseEntity.ok().body(IdsResponse.builder()
                .ids(resourceIds)
                .build());
    }
}
