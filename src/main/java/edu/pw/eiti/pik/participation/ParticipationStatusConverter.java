package edu.pw.eiti.pik.participation;

import java.beans.PropertyEditorSupport;

public class ParticipationStatusConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String capitalized = text.toUpperCase();
        ParticipationStatus status = ParticipationStatus.valueOf(capitalized);
        setValue(status);
    }
}
