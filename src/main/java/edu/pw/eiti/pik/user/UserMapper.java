package edu.pw.eiti.pik.user;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    static UserMapper getInstance() {
        return Mappers.getMapper(UserMapper.class);
    }

    User fromDto(UserDto dto);

    UserDto toDto(User entity);

    Authority fromDto(AuthorityDto dto);

    AuthorityDto toDto(Authority entity);
}
