package edu.pw.eiti.pik.user;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorityMapper {

    static AuthorityMapper getInstance() {
        return Mappers.getMapper(AuthorityMapper.class);
    }

    Authority fromDto(AuthorityDto dto);

    AuthorityDto toDto(Authority entity);
}
