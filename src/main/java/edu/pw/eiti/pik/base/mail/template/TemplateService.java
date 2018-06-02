package edu.pw.eiti.pik.base.mail.template;

import java.util.Map;

public interface TemplateService {
    String merge(String templateName, Map<String, Object> model);
}
