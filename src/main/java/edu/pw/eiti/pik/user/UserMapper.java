package edu.pw.eiti.pik.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    static UserMapper getInstance() {
        return Mappers.getMapper(UserMapper.class);
    }

    @Mapping(target = "id", expression="java(dto.getId() == null ? null : Long.parseLong(dto.getId()))")
    User fromDto(UserDto dto);

    @Mapping(target = "id", expression="java(entity.getId().toString())")
    UserDto toDto(User entity);

    @Mapping(target = "id", expression="java(Long.parseLong(dto.getId()))")
    Authority fromDto(AuthorityDto dto);

    @Mapping(target = "id", expression="java(entity.getId().toString())")
    AuthorityDto toDto(Authority entity);
}
