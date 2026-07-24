package com.redmath.user;

import java.util.ArrayList;
import java.util.List;

public class ApiUserMapper {

    public ApiUser toEntity(ApiUserDto dto) {
        if (dto == null) return null;

        ApiUser user = new ApiUser();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        return user;
    }

    public List<ApiUserDto> toResponseDto(List<ApiUser> entities) {
        if (entities == null) return null;
        List<ApiUserDto> dtoList = new ArrayList<>();
        for(ApiUser entity: entities){
            ApiUserDto dto = new ApiUserDto();
            dto.setUsername(entity.getUsername());
            dto.setPassword(entity.getPassword());
            dto.setRole(entity.getRole());
            dtoList.add(dto);
        }
        return dtoList;
    }
}
