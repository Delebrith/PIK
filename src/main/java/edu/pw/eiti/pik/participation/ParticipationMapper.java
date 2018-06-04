package edu.pw.eiti.pik.participation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ParticipationMapper {
    static ParticipationMapper getInstance() {
        return Mappers.getMapper(ParticipationMapper.class);
    }

    @Mapping(target = "id", expression="java(Long.parseLong(dto.getId()))")
    Participation fromDto(ParticipationDto dto);
    
    @Mapping(target = "id", expression="java(entity.getId().toString())")
    ParticipationDto toDto(Participation entity);
}
