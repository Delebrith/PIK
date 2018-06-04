package edu.pw.eiti.pik.user;
import java.beans.PropertyEditorSupport;

public class AuthoritiesConverter extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        String capitalized = text.toUpperCase();
        Authorities authority = Authorities.valueOf(capitalized);
        setValue(authority);
    }
}
