package core.framework.internal.web.api;

import core.framework.internal.log.LogManager;
import core.framework.internal.web.sys.APIController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author neo
 */
public class APIMessageDefinitionBuilder {
    private final List<APIController.MessagePublish> messages;
    private final APITypeParser parser = new APITypeParser();

    public APIMessageDefinitionBuilder(List<APIController.MessagePublish> messages) {
        this.messages = messages;
    }

    public APIMessageDefinitionResponse build() {
        var response = new APIMessageDefinitionResponse();
        response.app = LogManager.APP_NAME;
        response.topics = new ArrayList<>(messages.size());
        for (APIController.MessagePublish publish : messages) {
            var topic = new APIMessageDefinitionResponse.Topic();
            topic.name = publish.topic;
            topic.messageType = parser.parseBeanType(publish.messageClass);
            response.topics.add(topic);
        }
        response.types = parser.types();
        return response;
    }
}
