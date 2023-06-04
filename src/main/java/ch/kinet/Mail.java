/*
 * Copyright (C) 2014 - 2023 by Stefan Rothe, Sebastian Forster
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.kinet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Mail {

    final List<Data> attachments = new ArrayList<>();
    private final List<String> bcc = new ArrayList<>();
    private final List<String> to = new ArrayList<>();
    private final List<String> cc = new ArrayList<>();
    private StringBuilder body = new StringBuilder();
    private StringBuilder bodyHtml = new StringBuilder();
    private String subject;

    public static Mail create() {
        return new Mail();
    }

    private Mail() {
    }

    public void addAttachment(Data attachment) {
        attachments.add(attachment);
    }

    public void addBcc(String emailAddress) {
        bcc.add(emailAddress);
    }

    public void addCc(String emailAddress) {
        cc.add(emailAddress);
    }

    public void addLine(String line) {
        if (body.length() > 0) {
            body.append('\n');
        }

        body.append(line);
    }

    public void addLineHtml(String line) {
        bodyHtml.append(line);
    }

    public void addTo(String emailAddress) {
        to.add(emailAddress);
    }

    public List<String> getBcc() {
        return Collections.unmodifiableList(bcc);
    }

    public String getBody() {
        return body.toString();
    }

    public String getBodyHtml() {
        return bodyHtml.toString();
    }

    public List<String> getCc() {
        return Collections.unmodifiableList(cc);
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getTo() {
        return Collections.unmodifiableList(to);
    }

    public void setBody(String body) {
        if (body != null) {
            this.body = new StringBuilder(body);
        }
    }

    public void setBodyHtml(String body) {
        this.bodyHtml = new StringBuilder(body);
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
