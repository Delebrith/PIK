package edu.pw.eiti.pik.base.mail.template;

public class TemplateException extends RuntimeException {

    public TemplateException() {
        super("Could not use template");
    }
}
