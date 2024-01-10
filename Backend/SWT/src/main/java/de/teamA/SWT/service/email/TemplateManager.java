package de.teamA.SWT.service.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateManager {

    private final String TEMPLATE_DIR = "templates/emails";
    private final String TEMPLATE_EXT = ".email";

    private final String COMMENTKEY = "#";
    private final String SUBJECTKEY = "SUBJECT:";
    private final String TEXTKEY = "TEXT:";

    private Map<String, EmailTemplate> templates = new HashMap<>();

    private String defaultSignature;
    private String adminAddress;

    public TemplateManager() {
    }

    public void init(String defaultSignature, String adminAddress) {
        this.defaultSignature = defaultSignature;
        this.adminAddress = adminAddress;
        loadTemplates(Paths.get(TEMPLATE_DIR));
    }

    public EmailTemplate getTemplate(String mailType) {
        EmailTemplate template = templates.get(mailType);
        if (template == null) {
            throw new TemplateParsingException(
                    "Couldn't find '" + mailType + "'. Make sure you defined this template in '" + TEMPLATE_DIR + "'!");
        }
        EmailTemplate copy = new EmailTemplate(template.subjectText, template.templateText);
        copy.setSignature(defaultSignature);
        copy.setAdminMail(adminAddress);
        return copy;
    }

    private void loadTemplates(Path directory) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    loadTemplates(path);
                } else {
                    if (path.getFileName().toString().endsWith(TEMPLATE_EXT)) {
                        String mailType = path.getFileName().toString().split(TEMPLATE_EXT)[0];
                        templates.put(mailType, load(path));
                    }
                }
            }
        } catch (IOException e) {
            throw new TemplateParsingException(e);
        }
    }

    private EmailTemplate load(Path templateFile) {

        String templateSubject = "";
        List<String> templateText = new ArrayList<>();

        boolean subjectkeyFound = false;
        boolean textkeyFound = false;

        try (BufferedReader reader = Files.newBufferedReader(templateFile, StandardCharsets.UTF_8)) {
            String line = reader.readLine();
            int lineCount = 1;
            while (line != null) {

                String trimmed = line.trim();

                if (trimmed.startsWith(COMMENTKEY)) {
                    // is a comment, do nothing
                } else if (!subjectkeyFound && trimmed.isEmpty()) {
                    // ignore blank lines before SUBJECTKEY

                } else if (!textkeyFound && trimmed.isEmpty()) {
                    // ignore blank lines before TEXTKEY
                } else {

                    // process template information:
                    if (!subjectkeyFound && trimmed.startsWith(SUBJECTKEY)) {

                        String subject = trimmed.split(SUBJECTKEY, 2)[1];
                        templateSubject = subject.trim();

                        subjectkeyFound = true;

                    } else if (!textkeyFound && trimmed.startsWith(TEXTKEY)) {

                        String charactersAfterTextkey = trimmed.split(TEXTKEY, 2)[1];

                        if (charactersAfterTextkey.trim().isEmpty()) {
                            // Ignore blank space after TEXTKEY
                        } else {
                            templateText.add(charactersAfterTextkey);
                        }

                        textkeyFound = true;

                    } else if (subjectkeyFound && !textkeyFound && !trimmed.isEmpty()) {

                        throw new TemplateParsingException(templateFile + ", line " + lineCount + ": '" + SUBJECTKEY
                                + "' must be in a single line!");

                    } else if (trimmed.startsWith(SUBJECTKEY)) {

                        throw new TemplateParsingException(templateFile + ", line " + lineCount
                                + ": Multiple definitions of '" + SUBJECTKEY + "' are not allowed!");

                    } else if (trimmed.startsWith(TEXTKEY)) {

                        throw new TemplateParsingException(templateFile + ", line " + lineCount
                                + ": Multiple definitions of '" + TEXTKEY + "' are not allowed!");

                    } else {

                        // add whole line to text
                        templateText.add(line);
                    }

                }

                // readNextLine:
                line = reader.readLine();
                ++lineCount;
            }

        } catch (IOException e) {
            throw new TemplateParsingException(e);
        }

        EmailTemplate template = new EmailTemplate(templateSubject, templateText);
        return template;
    }

}
