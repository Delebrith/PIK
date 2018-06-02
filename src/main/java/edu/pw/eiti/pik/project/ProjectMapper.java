package edu.pw.eiti.pik.project;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    static ProjectMapper getInstance() {
        return Mappers.getMapper(ProjectMapper.class);
    }

    @Mapping(target = "id", source = "id", ignore = true)
    Project fromDto(ProjectDto dto);

    @Mapping(target = "id", expression="java(entity.getId().toString())")
    ProjectDto toDto(Project entity);
}

