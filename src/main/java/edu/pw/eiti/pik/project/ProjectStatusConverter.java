package edu.pw.eiti.pik.project;

import java.beans.PropertyEditorSupport;

public class ProjectStatusConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String capitalized = text.toUpperCase();
        ProjectStatus status = ProjectStatus.STARTED.valueOf(capitalized);
        setValue(status);
    }
}
