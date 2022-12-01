package org.gridsuite.filter.server;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final String CATEGORY_BROKER_OUTPUT = FilterService.class.getName() + ".output-broker-messages";
    private static final Logger MESSAGE_OUTPUT_LOGGER = LoggerFactory.getLogger(CATEGORY_BROKER_OUTPUT);

    public static final String HEADER_MODIFIED_BY = "modifiedBy";
    public static final String HEADER_MODIFICATION_DATE = "modificationDate";
    public static final String HEADER_ELEMENT_UUID = "elementUuid";

    @Autowired
    private StreamBridge updatePublisher;

    private void sendElementUpdateMessage(Message<String> message) {
        MESSAGE_OUTPUT_LOGGER.debug("Sending message : {}", message);
        updatePublisher.send("publishElementUpdate-out-0", message);
    }

    public void emitElementUpdated(UUID elementUuid, String modifiedBy) {
        sendElementUpdateMessage(MessageBuilder.withPayload("")
                .setHeader(HEADER_ELEMENT_UUID, elementUuid)
                .setHeader(HEADER_MODIFIED_BY, modifiedBy)
                .setHeader(HEADER_MODIFICATION_DATE, LocalDateTime.now())
                .build()
        );
    }
}
