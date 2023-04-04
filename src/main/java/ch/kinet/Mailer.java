/*
 * Copyright (C) 2013 - 2022 by Tom Jampen, Stefan Rothe
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

import ch.kinet.http.Data;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public final class Mailer {

    public static final Mailer createMailer(String smtpServer, int port, String from) {
        return new Mailer(smtpServer, port, from);
    }

    private final String from;
    private final String smtpServer;
    private final int port;

    private Mailer(String smtpServer, int port, String from) {
        this.from = from;
        this.smtpServer = smtpServer;
        this.port = port;
    }

    public void sendMail(Mail mail) throws MessagingException {
        Properties props = new Properties();
        //props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", String.valueOf(port));
        Session session = Session.getInstance(props);
        final MimeMessage message = new MimeMessage(session);
        message.setFrom(from);
        for (String recipient : mail.getTo()) {
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        }

        for (String recipient : mail.getCc()) {
            message.addRecipient(Message.RecipientType.CC, new InternetAddress(recipient));
        }

        for (String recipient : mail.getBcc()) {
            message.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
        }

        message.setSubject(mail.getSubject());
        if (mail.attachments.isEmpty()) {
            message.setContent(mail.getBody(), "text/plain; charset=UTF-8");
        }
        else {
            Multipart multipart = new MimeMultipart();
            BodyPart text = new MimeBodyPart();
            text.setText(mail.getBody());
            multipart.addBodyPart(text);
            for (Data attachment : mail.attachments) {
                MimeBodyPart part = new MimeBodyPart();
                part.setDataHandler(new DataHandler(new Adapter(attachment)));
                part.setFileName(attachment.getFileName());
                multipart.addBodyPart(part);
            }

            message.setContent(multipart);
        }

        Transport.send(message);
    }

    private final class Adapter implements DataSource {

        private final Data data;

        public Adapter(Data data) {
            this.data = data;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data.getData().toBytes());
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getContentType() {
            return data.getMimeType();
        }

        @Override
        public String getName() {
            return data.getFileName();
        }
    }
}
