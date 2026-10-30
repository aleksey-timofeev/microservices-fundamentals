package com.alextim.resource.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ResourceUploadedEvent {
    private Integer resourceId;
}
