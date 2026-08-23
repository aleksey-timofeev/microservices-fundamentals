package com.alextim.song.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IdsResponse {
    private List<Integer> ids;
}
