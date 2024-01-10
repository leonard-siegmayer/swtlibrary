package de.teamA.SWT.service.email;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractEmailTemplate {

    protected final String SEP = System.lineSeparator();
    protected final String LISTSEP = ", ";
    protected final String LISTBULLET = "- ";
    protected final Pattern TEMPVARPATTERN = getTemplateVariablePattern();
    // Must correspond to patternString in getTemplateVariablePattern():
    protected final Function<String, String> templateMappingFunc = (variable) -> "$" + variable.toUpperCase();
    protected String recipient;
    protected List<String> cc;
    protected String subjectText;
    protected List<String> templateText;

    protected AbstractEmailTemplate(String subjectText, List<String> templateText) {
        this.subjectText = subjectText;
        this.templateText = templateText;
    }

    private static Pattern getTemplateVariablePattern() {

        List<String> availableFields = new ArrayList<>();

        // Ugly referencing the subclass explicitly, but I don't want to put the pattern
        // logic
        // into EmailTemplate
        for (Field field : EmailTemplate.class.getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            if (fieldType.isAssignableFrom(String.class) || fieldType.isAssignableFrom(List.class)) {
                availableFields.add(field.getName().toUpperCase());
            }
        }
        // Make sure the longest variables are getting replaced first -> no cutoffs
        Collections.sort(availableFields, (s1, s2) -> s2.length() - s1.length());

        // Must match templateMappingFunc:
        String patternString = "\\$(" + String.join("|", availableFields) + ")";

        return Pattern.compile(patternString);

    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public final Email compile() throws TemplateException {

        Map<String, Object> tokens = getTemplateVariableTokens();

        String subject = fillVariablesForSubject(tokens);
        List<String> body = fillVariablesForBody(tokens);

        return new Email(subject, String.join(SEP, body));

    }

    private Map<String, Object> getTemplateVariableTokens() throws TemplateException {

        Map<String, Object> tokens = new HashMap<>();

        try {
            for (Field field : getClass().getDeclaredFields()) {

                Object value = field.get(this);

                if (value instanceof String) {
                    String stringValue = (String) value;
                    tokens.put(templateMappingFunc.apply(field.getName()), stringValue);
                } else if (value instanceof List) {
                    List listValue = (List) value;
                    tokens.put(templateMappingFunc.apply(field.getName()), listValue);
                }
            }
        } catch (IllegalAccessException e) {
            throw new TemplateException(e);
        }

        return tokens;
    }

    private String fillVariablesForSubject(Map<String, Object> tokens) {
        return replaceInLine(this.subjectText, tokens);
    }

    private List<String> fillVariablesForBody(Map<String, Object> tokens) {

        List<String> body = new ArrayList<>();

        for (String line : this.templateText) {

            String filledLine = replaceInLine(line, tokens);

            body.add(filledLine);
        }

        return body;

    }

    private String replaceInLine(String line, Map<String, Object> tokens) {

        Matcher matcher = TEMPVARPATTERN.matcher(line);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            Object value = tokens.get(templateMappingFunc.apply(matcher.group(1)));
            if (value instanceof String) {
                matcher.appendReplacement(sb, (String) value);
            } else if (value instanceof List) {
                boolean listIsInRunningText;

                String textBeforeMatch = line.substring(0, matcher.start());
                String textAfterMatch = line.substring(matcher.end());

                listIsInRunningText = !textBeforeMatch.trim().isEmpty() || !textAfterMatch.trim().isEmpty();
                appendListValues(textBeforeMatch, matcher, sb, (List<String>) value, listIsInRunningText);
            }
        }
        matcher.appendTail(sb);

        return sb.toString();

    }

    private void appendListValues(String textBeforeMatch, Matcher matcher, StringBuffer buffer, List<String> valueList,
            boolean listIsInRunningText) {
        if (listIsInRunningText) {
            matcher.appendReplacement(buffer, String.join(LISTSEP, valueList));
        } else {
            String indentation = "";

            if (textBeforeMatch.trim().isEmpty()) {
                indentation = textBeforeMatch;
            }

            matcher.appendReplacement(buffer, LISTBULLET + valueList.get(0));

            for (int pos = 1; pos < valueList.size(); pos++) {
                buffer.append(SEP + indentation + LISTBULLET + valueList.get(pos));
            }
        }
    }

}
